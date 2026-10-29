package com.zlyd.customercollection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("visit_reminder_rule")
public class VisitReminderRule {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleName;
    private Integer overdueMonths;
    private Integer reminderLevel;
    private Integer repeatCycle;
    private Integer repeatInterval;
    private String contentTemplate;
    private Integer izEnabled;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
