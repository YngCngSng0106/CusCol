package com.zlyd.customercollection.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zlyd.customercollection.common.BusinessException;
import com.zlyd.customercollection.common.PageResult;
import com.zlyd.customercollection.common.Result;
import com.zlyd.customercollection.dto.VisitFormRequest;
import com.zlyd.customercollection.dto.VisitVO;
import com.zlyd.customercollection.entity.Customer;
import com.zlyd.customercollection.entity.VisitRecord;
import com.zlyd.customercollection.entity.VisitReminderRecord;
import com.zlyd.customercollection.mapper.CustomerMapper;
import com.zlyd.customercollection.mapper.VisitRecordMapper;
import com.zlyd.customercollection.mapper.VisitReminderRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
public class VisitService {

    private final VisitRecordMapper visitRecordMapper;
    private final CustomerMapper customerMapper;
    private final VisitReminderRecordMapper reminderRecordMapper;
    private final CustomerService customerService;
    private final AttachmentService attachmentService;

    public PageResult<VisitVO> page(Long userId, Long customerId, String customerName,
                                    LocalDate visitDateStart, LocalDate visitDateEnd,
                                    long pageNo, long pageSize) {
        if (customerId != null) {
            customerService.getOwned(customerId, userId);
        }
        var wrapper = Wrappers.<VisitRecord>lambdaQuery()
                .eq(VisitRecord::getUserId, userId)
                .eq(customerId != null, VisitRecord::getCustomerId, customerId)
                .ge(visitDateStart != null, VisitRecord::getVisitTime,
                        visitDateStart == null ? null : visitDateStart.atStartOfDay())
                .lt(visitDateEnd != null, VisitRecord::getVisitTime,
                        visitDateEnd == null ? null : visitDateEnd.plusDays(1).atStartOfDay());
        if (StringUtils.hasText(customerName)) {
            List<Long> customerIds = customerService.findOwnedIdsByName(userId, customerName);
            if (customerIds.isEmpty()) {
                return PageResult.of(List.of(), 0, pageNo, pageSize);
            }
            wrapper.in(VisitRecord::getCustomerId, customerIds);
        }
        wrapper.orderByDesc(VisitRecord::getVisitTime).orderByDesc(VisitRecord::getId);
        IPage<VisitRecord> page = visitRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<VisitVO> records = toVOList(page.getRecords());
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public List<VisitVO> listByCustomer(Long customerId, Long userId) {
        customerService.getOwned(customerId, userId);
        List<VisitRecord> records = visitRecordMapper.selectList(Wrappers.<VisitRecord>lambdaQuery()
                .eq(VisitRecord::getCustomerId, customerId)
                .orderByDesc(VisitRecord::getVisitTime)
                .orderByDesc(VisitRecord::getId));
        return toVOList(records);
    }

    public VisitVO detail(Long id, Long userId) {
        VisitRecord record = getOwned(id, userId);
        VisitVO vo = toVO(record, customerService.customerName(record.getCustomerId()));
        vo.setAttachments(attachmentService.listByBiz(AttachmentService.BIZ_VISIT, id));
        return vo;
    }

    @Transactional
    public VisitVO create(Long userId, VisitFormRequest request) {
        Customer customer = customerService.getOwned(request.getCustomerId(), userId);
        VisitRecord record = new VisitRecord();
        copyRequest(request, record);
        record.setUserId(userId);
        record.setIzDeleted(0);
        record.setCreateBy(userId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateBy(userId);
        record.setUpdateTime(LocalDateTime.now());
        visitRecordMapper.insert(record);
        attachmentService.bind(request.getAttachmentIds(), AttachmentService.BIZ_VISIT, record.getId(), userId);
        customerService.refreshLastVisitTime(customer.getId(), record.getVisitTime());
        markRemindersHandled(customer.getId());
        return detail(record.getId(), userId);
    }

    @Transactional
    public VisitVO update(Long id, Long userId, VisitFormRequest request) {
        VisitRecord record = getOwned(id, userId);
        if (!Objects.equals(record.getCustomerId(), request.getCustomerId())) {
            throw new BusinessException("不支持修改拜访记录所属客户");
        }
        copyRequest(request, record);
        record.setUpdateBy(userId);
        record.setUpdateTime(LocalDateTime.now());
        visitRecordMapper.updateById(record);
        attachmentService.bind(request.getAttachmentIds(), AttachmentService.BIZ_VISIT, id, userId);
        recalculateLastVisitTime(record.getCustomerId());
        markRemindersHandled(record.getCustomerId());
        return detail(id, userId);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        VisitRecord record = getOwned(id, userId);
        visitRecordMapper.deleteById(id);
        attachmentService.deleteByBiz(AttachmentService.BIZ_VISIT, id);
        recalculateLastVisitTime(record.getCustomerId());
    }

    private VisitRecord getOwned(Long id, Long userId) {
        VisitRecord record = visitRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("拜访记录不存在或已删除");
        }
        if (!Objects.equals(record.getUserId(), userId)) {
            throw new BusinessException(Result.FORBIDDEN, "无权访问该拜访记录");
        }
        return record;
    }

    private List<VisitVO> toVOList(List<VisitRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        Set<Long> customerIds = records.stream().map(VisitRecord::getCustomerId).collect(Collectors.toSet());
        Map<Long, Customer> customerMap = customerMapper.selectBatchIds(customerIds).stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity()));
        return records.stream().map(record -> {
            Customer customer = customerMap.get(record.getCustomerId());
            return toVO(record, customer == null ? null : customer.getCustomerName());
        }).toList();
    }

    private VisitVO toVO(VisitRecord record, String customerName) {
        VisitVO vo = new VisitVO();
        BeanUtils.copyProperties(record, vo);
        vo.setCustomerName(customerName);
        return vo;
    }

    private void copyRequest(VisitFormRequest request, VisitRecord record) {
        record.setCustomerId(request.getCustomerId());
        record.setVisitType(request.getVisitType());
        record.setVisitTime(request.getVisitTime());
        record.setVisitLocation(request.getVisitLocation());
        record.setVisitPurpose(request.getVisitPurpose());
        record.setVisitContent(request.getVisitContent());
        record.setVisitResult(request.getVisitResult());
        record.setNextVisitTime(request.getNextVisitTime());
    }

    private void recalculateLastVisitTime(Long customerId) {
        VisitRecord latest = visitRecordMapper.selectOne(Wrappers.<VisitRecord>lambdaQuery()
                .eq(VisitRecord::getCustomerId, customerId)
                .orderByDesc(VisitRecord::getVisitTime)
                .last("LIMIT 1"));
        LocalDateTime lastVisitTime = latest == null ? null : latest.getVisitTime();
        customerMapper.update(null, Wrappers.<Customer>lambdaUpdate()
                .eq(Customer::getId, customerId)
                .set(Customer::getLastVisitTime, lastVisitTime)
                .set(Customer::getUpdateTime, LocalDateTime.now()));
    }

    private void markRemindersHandled(Long customerId) {
        reminderRecordMapper.update(null, Wrappers.<VisitReminderRecord>lambdaUpdate()
                .eq(VisitReminderRecord::getCustomerId, customerId)
                .eq(VisitReminderRecord::getIzHandled, 0)
                .set(VisitReminderRecord::getIzHandled, 1)
                .set(VisitReminderRecord::getHandledTime, LocalDateTime.now()));
    }
}
