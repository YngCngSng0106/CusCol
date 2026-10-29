package com.zlyd.customercollection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zlyd.customercollection.config.AppProperties;
import com.zlyd.customercollection.dto.CustomerOverdueDTO;
import com.zlyd.customercollection.entity.Message;
import com.zlyd.customercollection.entity.VisitReminderRecord;
import com.zlyd.customercollection.entity.VisitReminderRule;
import com.zlyd.customercollection.mapper.CustomerMapper;
import com.zlyd.customercollection.mapper.MessageMapper;
import com.zlyd.customercollection.mapper.VisitReminderRecordMapper;
import com.zlyd.customercollection.mapper.VisitReminderRuleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final VisitReminderRuleMapper ruleMapper;
    private final VisitReminderRecordMapper recordMapper;
    private final CustomerMapper customerMapper;
    private final MessageMapper messageMapper;
    private final ConfigService configService;
    private final AppProperties properties;
    private final PlatformTransactionManager transactionManager;

    @Scheduled(cron = "${app.reminder.cron:0 0 8 * * *}")
    public void scheduledRun() {
        if (!properties.getReminder().isEnabled()) {
            return;
        }
        try {
            int count = run();
            log.info("拜访超期提醒任务执行完成，生成消息 {} 条", count);
        } catch (Exception e) {
            log.error("拜访超期提醒任务执行失败", e);
        }
    }

    public int run() {
        if (!properties.getReminder().isEnabled()
                || !configService.getBoolean("visit.reminder.enabled", true)) {
            return 0;
        }
        List<VisitReminderRule> rules = ruleMapper.selectList(Wrappers.<VisitReminderRule>lambdaQuery()
                .eq(VisitReminderRule::getIzEnabled, 1)
                .orderByAsc(VisitReminderRule::getOverdueMonths));
        int count = 0;
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        for (VisitReminderRule rule : rules) {
            LocalDateTime threshold = LocalDateTime.now().minusMonths(rule.getOverdueMonths());
            List<CustomerOverdueDTO> customers = customerMapper.selectOverdue(threshold);
            for (CustomerOverdueDTO customer : customers) {
                try {
                    Boolean pushed = transactionTemplate.execute(status -> pushOne(rule, customer));
                    if (Boolean.TRUE.equals(pushed)) {
                        count++;
                    }
                } catch (DuplicateKeyException e) {
                    log.debug("客户 {} 规则 {} 今日已推送，跳过", customer.getId(), rule.getId());
                } catch (Exception e) {
                    log.error("客户 {} 规则 {} 推送失败", customer.getId(), rule.getId(), e);
                }
            }
        }
        return count;
    }

    private boolean pushOne(VisitReminderRule rule, CustomerOverdueDTO customer) {
        LocalDate today = LocalDate.now();
        VisitReminderRecord latest = recordMapper.selectOne(Wrappers.<VisitReminderRecord>lambdaQuery()
                .eq(VisitReminderRecord::getCustomerId, customer.getId())
                .eq(VisitReminderRecord::getRuleId, rule.getId())
                .orderByDesc(VisitReminderRecord::getPushDate)
                .orderByDesc(VisitReminderRecord::getId)
                .last("LIMIT 1"));
        int repeatInterval = rule.getRepeatInterval() == null ? 1 : rule.getRepeatInterval();
        int intervalDays = rule.getRepeatCycle() != null && rule.getRepeatCycle() == 2
                ? repeatInterval * 7
                : repeatInterval;
        if (latest != null && latest.getPushDate() != null
                && latest.getPushDate().plusDays(Math.max(intervalDays, 1)).isAfter(today)) {
            return false;
        }

        int overdueDays = customer.getOverdueDays() == null ? 0 : customer.getOverdueDays();
        String content = rule.getContentTemplate()
                .replace("{customerName}", customer.getCustomerName())
                .replace("{overdueDays}", String.valueOf(overdueDays));

        if (configService.getBoolean("visit.reminder.mergeUnread", true)) {
            messageMapper.update(null, Wrappers.<Message>lambdaUpdate()
                    .eq(Message::getUserId, customer.getUserId())
                    .eq(Message::getBizType, 1)
                    .eq(Message::getBizId, customer.getId())
                    .eq(Message::getRuleId, rule.getId())
                    .eq(Message::getIzRead, 0)
                    .set(Message::getIzRead, 1)
                    .set(Message::getReadTime, LocalDateTime.now()));
        }

        Message message = new Message();
        message.setUserId(customer.getUserId());
        message.setMessageType(1);
        message.setTitle("客户拜访超期提醒");
        message.setContent(content);
        message.setBizType(1);
        message.setBizId(customer.getId());
        message.setRuleId(rule.getId());
        message.setIzRead(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);

        VisitReminderRecord record = new VisitReminderRecord();
        record.setRuleId(rule.getId());
        record.setCustomerId(customer.getId());
        record.setUserId(customer.getUserId());
        record.setLastVisitTime(customer.getLastVisitTime());
        record.setOverdueDays(overdueDays);
        record.setOverdueMonths(rule.getOverdueMonths());
        record.setReminderLevel(rule.getReminderLevel());
        record.setMessageId(message.getId());
        record.setPushDate(today);
        record.setIzHandled(0);
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);
        return true;
    }
}
