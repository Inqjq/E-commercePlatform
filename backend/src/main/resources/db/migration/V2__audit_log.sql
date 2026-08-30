-- 渡风电商平台 V2：管理端操作审计日志
CREATE TABLE t_audit_log (
    id            BIGINT       NOT NULL COMMENT '主键',
    operator_id   BIGINT       DEFAULT NULL COMMENT '操作人用户ID',
    operator_name VARCHAR(64)  DEFAULT NULL COMMENT '操作人账号',
    module        VARCHAR(32)  NOT NULL COMMENT '业务模块',
    action        VARCHAR(64)  NOT NULL COMMENT '操作动作',
    target        VARCHAR(128) DEFAULT NULL COMMENT '操作对象标识',
    detail        VARCHAR(1000) DEFAULT NULL COMMENT '操作详情(参数摘要)',
    ip            VARCHAR(64)  DEFAULT NULL COMMENT '操作来源IP',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_create_time (create_time),
    KEY idx_operator (operator_id),
    KEY idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理端操作审计日志';
