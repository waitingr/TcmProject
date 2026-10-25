-- =============================================================================
-- 智辨歧黄 - 中医智能问诊平台 数据库初始化脚本
-- 适用于 MySQL 8.0+
-- 执行方式：mysql -u root -p < sql/init_database.sql
-- =============================================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `tcm_platform`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `tcm_platform`;

-- 2. 创建用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名，唯一',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（建议加密存储，如 BCrypt）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `height` DOUBLE DEFAULT NULL COMMENT '身高(cm)',
    `weight` DOUBLE DEFAULT NULL COMMENT '体重(kg)',
    `blood_type` VARCHAR(10) DEFAULT NULL COMMENT '血型',
    `genetic_history` VARCHAR(500) DEFAULT NULL COMMENT '遗传病史',
    `bmi` DOUBLE DEFAULT NULL COMMENT 'BMI指数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 3. 创建问诊记录表 (JPA 关联版，对应 ConsultationRecord 实体)
CREATE TABLE IF NOT EXISTS `consultation_records` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
    `symptoms` VARCHAR(1000) DEFAULT NULL COMMENT '主诉症状',
    `diagnosis_type` VARCHAR(100) DEFAULT NULL COMMENT '证型判定',
    `features` VARCHAR(500) DEFAULT NULL COMMENT '体质特征',
    `risks` VARCHAR(500) DEFAULT NULL COMMENT '健康风险',
    `advice` TEXT DEFAULT NULL COMMENT '调理建议',
    `consult_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '问诊时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_consult_time` (`consult_time`),
    CONSTRAINT `fk_consultation_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问诊记录表(JPA关联版)';

-- 4. 创建问诊记录表 (轻量版，对应 ConsultRecord 实体)
CREATE TABLE IF NOT EXISTS `consult_records` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
    `consult_time` VARCHAR(50) NOT NULL COMMENT '问诊时间字符串(yyyy-MM-dd HH:mm:ss)',
    `diag_type` VARCHAR(100) DEFAULT NULL COMMENT '证型判定',
    `feature` VARCHAR(500) DEFAULT NULL COMMENT '体质特征',
    `risk` VARCHAR(500) DEFAULT NULL COMMENT '健康风险',
    `advice` TEXT DEFAULT NULL COMMENT '调理建议',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_consult_time` (`consult_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问诊记录表(轻量版)';

-- 5. 插入测试数据 (可选，开发环境使用)
-- 密码为 BCrypt 加密的 "123456"：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa
INSERT IGNORE INTO `users` (`username`, `password`, `nickname`, `height`, `weight`, `blood_type`, `genetic_history`, `bmi`) VALUES
('demo_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '演示用户', 170.0, 65.0, 'O型', '无', 22.5),
('test_tcm', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '测试中医', 165.0, 58.0, 'A型', '高血压家族史', 21.3);

-- 6. 插入示例问诊记录 (演示数据)
INSERT IGNORE INTO `consultation_records` (`user_id`, `symptoms`, `diagnosis_type`, `features`, `risks`, `advice`) VALUES
(1, '头晕乏力、面色苍白、心悸失眠、食少腹胀', '气血两虚证', '气虚质兼血虚质', '易感冒、免疫力低下、心脑血管疾病风险', '1. 饮食调理：多食补益气血食物，如红枣、桂圆、党参、黄芪、黑芝麻、猪肝等。\n2. 起居调理：保证充足睡眠，避免熬夜，适度运动如八段锦、太极拳。\n3. 情志调理：保持心情舒畅，避免过度思虑伤脾耗气。\n4. 穴位按摩：足三里、气海、关元、血海、三阴交，每日各按揉3-5分钟。\n5. 中成药参考：归脾丸、八珍丸、十全大补丸(需医师辨证指导)。'),
(1, '腰膝酸软、畏寒肢冷、夜尿频多、精神不振', '肾阳虚证', '阳虚质', '泌尿系统疾病、骨质疏松、性功能减退风险', '1. 饮食调理：温补肾阳，食用羊肉、韭菜、核桃、黑豆、枸杞等。\n2. 起居调理：注意腰部、足部保暖，冬季适当晒太阳(背部、足底)。\n3. 运动调理：练习提肛运动、搓腰眼、拍打八髎穴。\n4. 穴位按摩：命门、肾俞、关元、涌泉、太溪，艾灸效果更佳。\n5. 中成药参考：金匮肾气丸、右归丸、桂附地黄丸(需医师辨证指导)。');

INSERT IGNORE INTO `consult_records` (`user_id`, `consult_time`, `diag_type`, `feature`, `risk`, `advice`) VALUES
(1, '2024-01-15 10:30:00', '气血两虚证', '气虚质兼血虚质', '易感冒、免疫力低下', '建议食用红枣桂圆茶、八珍汤，坚持练习八段锦，按摩足三里、血海穴。'),
(2, '2024-02-20 14:45:00', '肝气郁结证', '气郁质', '乳腺增生、甲状腺结节风险', '疏肝理气，饮食清淡，多食萝卜、佛手、玫瑰花茶，练习六字诀"嘘"字诀。');

-- 7. 创建索引优化查询性能 (如有需要可追加)
-- ALTER TABLE `consultation_records` ADD INDEX `idx_diagnosis_type` (`diagnosis_type`);
-- ALTER TABLE `consult_records` ADD INDEX `idx_diag_type` (`diag_type`);

-- =============================================================================
-- 使用说明：
-- 1. 开发环境：直接执行本脚本，包含测试数据
-- 2. 生产环境：建议仅执行建表语句(1-4)，不执行测试数据插入(5-6)
-- 3. JPA 配置建议：
--    - 开发：spring.jpa.hibernate.ddl-auto=update
--    - 生产：spring.jpa.hibernate.ddl-auto=validate (需手动管理表结构变更)
-- =============================================================================