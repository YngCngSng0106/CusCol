-- 已有数据库执行一次；新库由 schema.sql 直接创建，无需执行本文件。
ALTER TABLE `customer`
  ADD INDEX `idx_contact_name` (`contact_name`),
  ADD INDEX `idx_contact_phone` (`contact_phone`);

ALTER TABLE `contract`
  ADD INDEX `idx_user_sign_date` (`user_id`, `sign_date`),
  ADD INDEX `idx_user_payment` (`user_id`, `payment_status`);
