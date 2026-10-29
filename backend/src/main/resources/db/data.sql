INSERT IGNORE INTO `visit_reminder_rule`
(`rule_name`, `overdue_months`, `reminder_level`, `repeat_cycle`, `repeat_interval`, `content_template`) VALUES
('3个月未拜访提醒', 3, 1, 1, 1, '客户{customerName}已{overdueDays}天未拜访，请及时跟进'),
('5个月未拜访预警', 5, 2, 2, 1, '客户{customerName}已{overdueDays}天未拜访，请尽快安排拜访');

INSERT IGNORE INTO `sys_config` (`config_key`, `config_value`, `config_name`) VALUES
('visit.reminder.enabled', '1', '拜访提醒开关'),
('visit.reminder.mergeUnread', '1', '同客户同规则只保留一条未读'),
('message.retain.days', '365', '消息保留天数');
