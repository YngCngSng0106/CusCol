package com.zlyd.customercollection.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zlyd.customercollection.common.BusinessException;
import com.zlyd.customercollection.common.PageResult;
import com.zlyd.customercollection.common.Result;
import com.zlyd.customercollection.dto.ContractVO;
import com.zlyd.customercollection.dto.CustomerDetailVO;
import com.zlyd.customercollection.dto.CustomerFormRequest;
import com.zlyd.customercollection.dto.ProjectFormRequest;
import com.zlyd.customercollection.dto.VisitVO;
import com.zlyd.customercollection.entity.Contract;
import com.zlyd.customercollection.entity.Customer;
import com.zlyd.customercollection.entity.CustomerProject;
import com.zlyd.customercollection.entity.VisitRecord;
import com.zlyd.customercollection.mapper.ContractMapper;
import com.zlyd.customercollection.mapper.CustomerMapper;
import com.zlyd.customercollection.mapper.CustomerProjectMapper;
import com.zlyd.customercollection.mapper.VisitRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerProjectMapper projectMapper;
    private final VisitRecordMapper visitRecordMapper;
    private final ContractMapper contractMapper;
    private final AttachmentService attachmentService;

    public PageResult<Customer> page(Long userId, String keyword, String customerName, String contactName,
                                     String contactPhone, Integer status, Integer izValid,
                                     long pageNo, long pageSize) {
        var wrapper = Wrappers.<Customer>lambdaQuery()
                .eq(Customer::getUserId, userId)
                .eq(status != null, Customer::getStatus, status)
                .eq(izValid != null, Customer::getIzValid, izValid)
                .like(StringUtils.hasText(customerName), Customer::getCustomerName,
                        customerName == null ? null : customerName.trim())
                .like(StringUtils.hasText(contactName), Customer::getContactName,
                        contactName == null ? null : contactName.trim())
                .like(StringUtils.hasText(contactPhone), Customer::getContactPhone,
                        contactPhone == null ? null : contactPhone.trim());
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            wrapper.and(w -> w.like(Customer::getCustomerName, value)
                    .or().like(Customer::getContactName, value)
                    .or().like(Customer::getContactPhone, value));
        }
        wrapper.orderByDesc(Customer::getUpdateTime).orderByDesc(Customer::getId);
        IPage<Customer> page = customerMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(page);
    }

    public List<Long> findOwnedIdsByName(Long userId, String customerName) {
        if (!StringUtils.hasText(customerName)) {
            return List.of();
        }
        return customerMapper.selectList(Wrappers.<Customer>lambdaQuery()
                        .select(Customer::getId)
                        .eq(Customer::getUserId, userId)
                        .like(Customer::getCustomerName, customerName.trim()))
                .stream()
                .map(Customer::getId)
                .toList();
    }

    public Customer getOwned(Long id, Long userId) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException("客户不存在或已删除");
        }
        if (!Objects.equals(customer.getUserId(), userId)) {
            throw new BusinessException(Result.FORBIDDEN, "无权访问该客户");
        }
        return customer;
    }

    public CustomerDetailVO detail(Long id, Long userId) {
        Customer customer = getOwned(id, userId);
        CustomerDetailVO detail = new CustomerDetailVO();
        detail.setCustomer(customer);
        detail.setProjects(projectMapper.selectList(Wrappers.<CustomerProject>lambdaQuery()
                .eq(CustomerProject::getCustomerId, id)
                .orderByDesc(CustomerProject::getId)));
        List<VisitRecord> visits = visitRecordMapper.selectList(Wrappers.<VisitRecord>lambdaQuery()
                .eq(VisitRecord::getCustomerId, id)
                .orderByDesc(VisitRecord::getVisitTime)
                .last("LIMIT 10"));
        detail.setVisits(visits.stream().map(visit -> toVisitVO(visit, customer.getCustomerName())).toList());
        List<Contract> contracts = contractMapper.selectList(Wrappers.<Contract>lambdaQuery()
                .eq(Contract::getCustomerId, id)
                .orderByDesc(Contract::getId)
                .last("LIMIT 10"));
        detail.setContracts(contracts.stream().map(contract -> toContractVO(contract, customer.getCustomerName())).toList());
        detail.setAttachments(attachmentService.listByBiz(AttachmentService.BIZ_CUSTOMER, id));
        return detail;
    }

    @Transactional
    public Customer create(Long userId, CustomerFormRequest request) {
        Customer customer = new Customer();
        copyRequest(request, customer);
        customer.setUserId(userId);
        customer.setIzValid(1);
        customer.setIzDeleted(0);
        customer.setCreateBy(userId);
        customer.setCreateTime(LocalDateTime.now());
        customer.setUpdateBy(userId);
        customer.setUpdateTime(LocalDateTime.now());
        customerMapper.insert(customer);
        attachmentService.bind(request.getAttachmentIds(), AttachmentService.BIZ_CUSTOMER, customer.getId(), userId);
        return customer;
    }

    @Transactional
    public Customer update(Long id, Long userId, CustomerFormRequest request) {
        Customer customer = getOwned(id, userId);
        copyRequest(request, customer);
        customer.setUpdateBy(userId);
        customer.setUpdateTime(LocalDateTime.now());
        customerMapper.updateById(customer);
        attachmentService.bind(request.getAttachmentIds(), AttachmentService.BIZ_CUSTOMER, id, userId);
        return customer;
    }

    @Transactional
    public void setValid(Long id, Long userId, Integer izValid) {
        Customer customer = getOwned(id, userId);
        customer.setIzValid(izValid != null && izValid == 1 ? 1 : 0);
        customer.setUpdateBy(userId);
        customer.setUpdateTime(LocalDateTime.now());
        customerMapper.updateById(customer);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        getOwned(id, userId);
        customerMapper.deleteById(id);
        attachmentService.deleteByBiz(AttachmentService.BIZ_CUSTOMER, id);
    }

    public List<CustomerProject> listProjects(Long customerId, Long userId) {
        getOwned(customerId, userId);
        return projectMapper.selectList(Wrappers.<CustomerProject>lambdaQuery()
                .eq(CustomerProject::getCustomerId, customerId)
                .orderByDesc(CustomerProject::getId));
    }

    @Transactional
    public CustomerProject addProject(Long customerId, Long userId, ProjectFormRequest request) {
        getOwned(customerId, userId);
        CustomerProject project = new CustomerProject();
        project.setCustomerId(customerId);
        project.setUserId(userId);
        project.setProjectName(request.getProjectName());
        project.setProjectType(request.getProjectType());
        project.setProjectAmount(request.getProjectAmount());
        project.setProjectStatus(request.getProjectStatus() == null ? 1 : request.getProjectStatus());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setProjectDesc(request.getProjectDesc());
        project.setIzDeleted(0);
        project.setCreateBy(userId);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateBy(userId);
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.insert(project);
        attachmentService.bind(request.getAttachmentIds(), AttachmentService.BIZ_PROJECT, project.getId(), userId);
        return project;
    }

    public void refreshLastVisitTime(Long customerId, LocalDateTime visitTime) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            return;
        }
        if (customer.getLastVisitTime() == null || customer.getLastVisitTime().isBefore(visitTime)) {
            customer.setLastVisitTime(visitTime);
            customer.setUpdateTime(LocalDateTime.now());
            customerMapper.updateById(customer);
        }
    }

    public List<Customer> listAllOwned(Long userId) {
        return customerMapper.selectList(Wrappers.<Customer>lambdaQuery()
                .eq(Customer::getUserId, userId)
                .orderByAsc(Customer::getCustomerName));
    }

    public String customerName(Long customerId) {
        Customer customer = customerMapper.selectById(customerId);
        return customer == null ? null : customer.getCustomerName();
    }

    public VisitVO toVisitVO(VisitRecord visit, String customerName) {
        VisitVO vo = new VisitVO();
        BeanUtils.copyProperties(visit, vo);
        vo.setCustomerName(customerName);
        vo.setAttachments(Collections.emptyList());
        return vo;
    }

    public ContractVO toContractVO(Contract contract, String customerName) {
        ContractVO vo = new ContractVO();
        BeanUtils.copyProperties(contract, vo);
        vo.setCustomerName(customerName);
        vo.setAttachments(Collections.emptyList());
        return vo;
    }

    private void copyRequest(CustomerFormRequest request, Customer customer) {
        customer.setCustomerName(request.getCustomerName());
        customer.setCustomerLevel(request.getCustomerLevel());
        customer.setIndustry(request.getIndustry());
        customer.setCity(request.getCity());
        customer.setAddress(request.getAddress());
        customer.setContactName(request.getContactName());
        customer.setContactPhone(request.getContactPhone());
        customer.setSource(request.getSource());
        customer.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        customer.setRemark(request.getRemark());
    }
}
