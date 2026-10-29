package com.zlyd.customercollection.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zlyd.customercollection.common.BusinessException;
import com.zlyd.customercollection.common.PageResult;
import com.zlyd.customercollection.common.Result;
import com.zlyd.customercollection.dto.ContractFormRequest;
import com.zlyd.customercollection.dto.ContractVO;
import com.zlyd.customercollection.entity.Contract;
import com.zlyd.customercollection.entity.Customer;
import com.zlyd.customercollection.mapper.ContractMapper;
import com.zlyd.customercollection.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractMapper contractMapper;
    private final CustomerMapper customerMapper;
    private final CustomerService customerService;
    private final AttachmentService attachmentService;

    public PageResult<ContractVO> page(Long userId, Long customerId, String keyword, String customerName,
                                       LocalDate signDateStart, LocalDate signDateEnd, Integer paymentStatus,
                                       long pageNo, long pageSize) {
        if (customerId != null) {
            customerService.getOwned(customerId, userId);
        }
        if (StringUtils.hasText(customerName)) {
            List<Long> customerIds = customerService.findOwnedIdsByName(userId, customerName);
            if (customerIds.isEmpty()) {
                return PageResult.of(List.of(), 0, pageNo, pageSize);
            }
            var wrapper = Wrappers.<Contract>lambdaQuery()
                    .eq(Contract::getUserId, userId)
                    .eq(customerId != null, Contract::getCustomerId, customerId)
                    .in(Contract::getCustomerId, customerIds)
                    .ge(signDateStart != null, Contract::getSignDate, signDateStart)
                    .le(signDateEnd != null, Contract::getSignDate, signDateEnd)
                    .eq(paymentStatus != null, Contract::getPaymentStatus, paymentStatus);
            appendKeyword(wrapper, keyword);
            return pageQuery(wrapper, pageNo, pageSize);
        }
        var wrapper = Wrappers.<Contract>lambdaQuery()
                .eq(Contract::getUserId, userId)
                .eq(customerId != null, Contract::getCustomerId, customerId)
                .ge(signDateStart != null, Contract::getSignDate, signDateStart)
                .le(signDateEnd != null, Contract::getSignDate, signDateEnd)
                .eq(paymentStatus != null, Contract::getPaymentStatus, paymentStatus);
        appendKeyword(wrapper, keyword);
        return pageQuery(wrapper, pageNo, pageSize);
    }

    private void appendKeyword(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Contract> wrapper,
                               String keyword) {
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            wrapper.and(w -> w.like(Contract::getContractName, value)
                    .or().like(Contract::getContractNo, value));
        }
    }

    private PageResult<ContractVO> pageQuery(
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Contract> wrapper,
            long pageNo, long pageSize) {
        wrapper.orderByDesc(Contract::getCreateTime).orderByDesc(Contract::getId);
        IPage<Contract> page = contractMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(toVOList(page.getRecords()), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public List<ContractVO> listByCustomer(Long customerId, Long userId) {
        customerService.getOwned(customerId, userId);
        List<Contract> records = contractMapper.selectList(Wrappers.<Contract>lambdaQuery()
                .eq(Contract::getCustomerId, customerId)
                .orderByDesc(Contract::getCreateTime)
                .orderByDesc(Contract::getId));
        return toVOList(records);
    }

    public ContractVO detail(Long id, Long userId) {
        Contract contract = getOwned(id, userId);
        ContractVO vo = toVO(contract, customerService.customerName(contract.getCustomerId()));
        vo.setAttachments(attachmentService.listByBiz(AttachmentService.BIZ_CONTRACT, id));
        return vo;
    }

    @Transactional
    public ContractVO create(Long userId, ContractFormRequest request) {
        Customer customer = customerService.getOwned(request.getCustomerId(), userId);
        ensureContractNoUnique(request.getContractNo(), null);
        Contract contract = new Contract();
        copyRequest(request, contract);
        contract.setUserId(userId);
        contract.setIzDeleted(0);
        contract.setCreateBy(userId);
        contract.setCreateTime(LocalDateTime.now());
        contract.setUpdateBy(userId);
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.insert(contract);
        attachmentService.bind(request.getAttachmentIds(), AttachmentService.BIZ_CONTRACT, contract.getId(), userId);
        return detail(contract.getId(), userId);
    }

    @Transactional
    public ContractVO update(Long id, Long userId, ContractFormRequest request) {
        Contract contract = getOwned(id, userId);
        if (!Objects.equals(contract.getCustomerId(), request.getCustomerId())) {
            throw new BusinessException("不支持修改合同所属客户");
        }
        ensureContractNoUnique(request.getContractNo(), id);
        copyRequest(request, contract);
        contract.setUpdateBy(userId);
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.updateById(contract);
        attachmentService.bind(request.getAttachmentIds(), AttachmentService.BIZ_CONTRACT, id, userId);
        return detail(id, userId);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        getOwned(id, userId);
        contractMapper.deleteById(id);
        attachmentService.deleteByBiz(AttachmentService.BIZ_CONTRACT, id);
    }

    private Contract getOwned(Long id, Long userId) {
        Contract contract = contractMapper.selectById(id);
        if (contract == null) {
            throw new BusinessException("合同不存在或已删除");
        }
        if (!Objects.equals(contract.getUserId(), userId)) {
            throw new BusinessException(Result.FORBIDDEN, "无权访问该合同");
        }
        return contract;
    }

    private void ensureContractNoUnique(String contractNo, Long excludeId) {
        var wrapper = Wrappers.<Contract>lambdaQuery().eq(Contract::getContractNo, contractNo);
        if (excludeId != null) {
            wrapper.ne(Contract::getId, excludeId);
        }
        if (contractMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("合同编号已存在");
        }
    }

    private List<ContractVO> toVOList(List<Contract> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        Set<Long> customerIds = records.stream().map(Contract::getCustomerId).collect(Collectors.toSet());
        Map<Long, Customer> customerMap = customerMapper.selectBatchIds(customerIds).stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity()));
        return records.stream().map(record -> {
            Customer customer = customerMap.get(record.getCustomerId());
            return toVO(record, customer == null ? null : customer.getCustomerName());
        }).toList();
    }

    private ContractVO toVO(Contract contract, String customerName) {
        ContractVO vo = new ContractVO();
        BeanUtils.copyProperties(contract, vo);
        vo.setCustomerName(customerName);
        return vo;
    }

    private void copyRequest(ContractFormRequest request, Contract contract) {
        contract.setCustomerId(request.getCustomerId());
        contract.setContractNo(request.getContractNo());
        contract.setContractName(request.getContractName());
        contract.setContractAmount(request.getContractAmount());
        contract.setSignDate(request.getSignDate());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setContractStatus(request.getContractStatus() == null ? 1 : request.getContractStatus());
        contract.setPaymentStatus(request.getPaymentStatus() == null ? 0 : request.getPaymentStatus());
        contract.setPaidAmount(request.getPaidAmount() == null ? BigDecimal.ZERO : request.getPaidAmount());
        contract.setRemark(request.getRemark());
    }
}
