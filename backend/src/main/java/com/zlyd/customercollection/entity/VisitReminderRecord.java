package com.zlyd.customercollection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("visit_reminder_record")
public class VisitReminderRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ruleId;
    private Long customerId;
    private Long userId;
    private LocalDateTime lastVisitTime;
    private Integer overdueDays;
    private Integer overdueMonths;
    private Integer reminderLevel;
    private Long messageId;
    private LocalDate pushDate;
    private Integer izHandled;
    private LocalDateTime handledTime;
    private LocalDateTime createTime;
}
