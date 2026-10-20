-- ============================================================
-- 积分商城建表脚本 (points-service)  —— 修订版
-- 目标库: goldnexus (共享库)
-- 用法:   mysql -u root -p goldnexus < schema.sql
-- 说明:   可重复执行(会重建积分相关表,不影响其它现有表)
-- 规范:   InnoDB / utf8mb4 / camelCase 列名(与项目一致,MyBatis 未开下划线转驼峰) / INT 主键 / 唯一索引做幂等
-- ============================================================

SET NAMES utf8mb4;

-- ---------- 1. 积分账户 ----------
DROP TABLE IF EXISTS points_account;
CREATE TABLE points_account (
  id          INT      AUTO_INCREMENT PRIMARY KEY,
  userId      INT      NOT NULL COMMENT '用户ID(与现有user.id一致,INT)',
  status      TINYINT  NOT NULL DEFAULT 1 COMMENT '1正常 2冻结 3注销清零',
  balance     INT      NOT NULL DEFAULT 0 COMMENT '可用积分(镜像,对账依据是流水)',
  totalEarned INT      NOT NULL DEFAULT 0 COMMENT '累计获得',
  totalSpent  INT      NOT NULL DEFAULT 0 COMMENT '累计消耗',
  createTime  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user (userId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分账户';

-- ---------- 2. 积分批次 ----------
DROP TABLE IF EXISTS points_batch;
CREATE TABLE points_batch (
  id             INT         AUTO_INCREMENT PRIMARY KEY,
  userId         INT         NOT NULL,
  sourceType     VARCHAR(32) NOT NULL COMMENT '来源事件,同points_rule.eventType',
  sourceBizNo    VARCHAR(64) NOT NULL COMMENT '业务单号(幂等键),如还款repayment_id',
  pointsEarned   INT         NOT NULL COMMENT '本批获得积分',
  pointsRemaining INT        NOT NULL COMMENT '本批剩余可用(扣减只减这里)',
  earnedAt       DATETIME    NOT NULL,
  expiresAt      DATETIME    NOT NULL COMMENT '= earnedAt + 365天',
  status         TINYINT     NOT NULL DEFAULT 1 COMMENT '1可用 2已过期 3已撤销',
  createTime     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_source (userId, sourceType, sourceBizNo),
  KEY idx_expire (userId, status, expiresAt),
  KEY idx_expire_scan (status, expiresAt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分批次';

-- ---------- 3. 积分流水 ----------
DROP TABLE IF EXISTS points_flow;
CREATE TABLE points_flow (
  id         INT         AUTO_INCREMENT PRIMARY KEY,
  userId     INT         NOT NULL,
  batchId    INT         NULL COMMENT '关联批次(过期/消费时非空)',
  flowType   VARCHAR(16) NOT NULL COMMENT 'EARN/SPEND/EXPIRE/REVOKE/ADJUST_ADD/ADJUST_SUB',
  bizType    VARCHAR(32) NOT NULL COMMENT '业务类型(幂等键1)',
  bizNo      VARCHAR(64) NOT NULL COMMENT '业务单号(幂等键2)',
  amount     INT         NOT NULL COMMENT '正数=获得,负数=消耗/过期/撤销',
  remark     VARCHAR(255),
  operatorId INT         NULL COMMENT '管理员操作时记录,用户行为为NULL',
  createTime DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_biz (bizType, bizNo),
  KEY idx_user_time (userId, createTime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水(只增不改)';

-- ---------- 4. 积分规则 ----------
DROP TABLE IF EXISTS points_rule;
CREATE TABLE points_rule (
  id              INT         AUTO_INCREMENT PRIMARY KEY,
  eventType       VARCHAR(32) NOT NULL COMMENT 'REGISTER/REAL_NAME/SIGN_IN/REPAY_SETTLE',
  name            VARCHAR(64) NOT NULL COMMENT '规则名称',
  points          INT         NOT NULL COMMENT '基础分值',
  requireVerified TINYINT     NOT NULL DEFAULT 1 COMMENT '是否需实名 1是 0否',
  dailyCap        INT         NULL COMMENT '单日发放次数上限(NULL=不限)',
  totalCap        INT         NULL COMMENT '终身次数上限(NULL=不限)',
  enabled         TINYINT     NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  remark          VARCHAR(255),
  createTime      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_event (eventType)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分规则(表驱动)';

-- ---------- 5. 签到记录 ----------
DROP TABLE IF EXISTS points_sign;
CREATE TABLE points_sign (
  id          INT      AUTO_INCREMENT PRIMARY KEY,
  userId      INT      NOT NULL,
  signDate    DATE     NOT NULL COMMENT '签到日期',
  consecutive INT      NOT NULL DEFAULT 1 COMMENT '连续签到天数',
  points      INT      NOT NULL COMMENT '本次获得积分',
  createTime  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sign (userId, signDate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录';

-- ---------- 6. 每日发放额度 ----------
DROP TABLE IF EXISTS points_daily_quota;
CREATE TABLE points_daily_quota (
  id         INT      AUTO_INCREMENT PRIMARY KEY,
  userId     INT      NOT NULL,
  quotaDate  DATE     NOT NULL,
  used       INT      NOT NULL DEFAULT 0 COMMENT '当日已发',
  dailyLimit INT      NOT NULL DEFAULT 100 COMMENT '单日上限(可配)',
  createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_date (userId, quotaDate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分每日发放额度';

-- ---------- 7. 积分商品 ----------
DROP TABLE IF EXISTS points_product;
CREATE TABLE points_product (
  id          INT          AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(128) NOT NULL,
  itemType    VARCHAR(16)  NOT NULL COMMENT 'PHYSICAL实体 / VIRTUAL_CODE兑换码 / VIP权益',
  pointsPrice INT          NOT NULL COMMENT '所需积分',
  stock       INT          NOT NULL DEFAULT 0 COMMENT '实体库存;-1=不限',
  coverUrl    VARCHAR(255),
  detail      TEXT,
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
  createTime  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商品';

-- ---------- 8. 兑换订单 ----------
DROP TABLE IF EXISTS points_order;
CREATE TABLE points_order (
  id          INT          AUTO_INCREMENT PRIMARY KEY,
  orderNo     VARCHAR(40)  NOT NULL COMMENT '对外订单号',
  userId      INT          NOT NULL,
  productId   INT          NOT NULL,
  productName VARCHAR(128) NOT NULL COMMENT '下单时快照,防商品改名影响历史',
  pointsCost  INT          NOT NULL COMMENT '下单时积分快照',
  itemType    VARCHAR(16)  NOT NULL,
  status      VARCHAR(16)  NOT NULL COMMENT 'CREATED/PAID/CANCELLED/COMPLETED/REFUNDED',
  addressInfo VARCHAR(500) COMMENT '实体:收货人/电话/地址JSON',
  logisticsNo VARCHAR(64)  COMMENT '实体:物流单号',
  codeValue   VARCHAR(64)  COMMENT '虚拟:兑换码',
  requestId   VARCHAR(64)  NOT NULL COMMENT '幂等键,同一requestId重复提交只生成一单',
  createTime  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_request (userId, requestId),
  UNIQUE KEY uk_order_no (orderNo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换订单';

-- ---------- 9. 兑换码 ----------
DROP TABLE IF EXISTS points_code;
CREATE TABLE points_code (
  id         INT         AUTO_INCREMENT PRIMARY KEY,
  productId  INT         NOT NULL,
  codeValue  VARCHAR(64) NOT NULL COMMENT '随机码,不可枚举',
  status     TINYINT     NOT NULL DEFAULT 0 COMMENT '0未用 1已锁定 2已核销',
  orderNo    VARCHAR(40) NULL,
  userId     INT         NULL,
  expireTime DATETIME    NULL,
  UNIQUE KEY uk_code (codeValue),
  KEY idx_use (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换码';

-- ---------- 10. 调账单 ----------
DROP TABLE IF EXISTS points_adjust;
CREATE TABLE points_adjust (
  id             INT          AUTO_INCREMENT PRIMARY KEY,
  adminId        INT          NOT NULL COMMENT '发起调账的管理员',
  userId         INT          NOT NULL,
  changeType     VARCHAR(16)  NOT NULL COMMENT 'ADD/SUB 增/减',
  amount         INT          NOT NULL COMMENT '调账积分(正数)',
  reason         VARCHAR(255) NOT NULL COMMENT '调账原因(必填)',
  status         VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  approveAdminId INT          NULL COMMENT '审核管理员',
  createTime     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_user (userId),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分调账单';

-- ---------- 11. 站内消息(积分商城专用) ----------
DROP TABLE IF EXISTS points_message;
CREATE TABLE points_message (
  id         INT          AUTO_INCREMENT PRIMARY KEY,
  userId     INT          NOT NULL,
  type       VARCHAR(32)  NOT NULL COMMENT 'POINT_EXPIRING/POINT_ANNOUNCE 等',
  content    VARCHAR(500) NOT NULL,
  isRead     TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  createTime DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_user_read (userId, isRead)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息(积分)';

-- ---------- 12. 调价公告 ----------
DROP TABLE IF EXISTS points_announcement;
CREATE TABLE points_announcement (
  id            INT      AUTO_INCREMENT PRIMARY KEY,
  productId     INT      NOT NULL,
  oldPrice      INT      NOT NULL,
  newPrice      INT      NOT NULL,
  effectiveTime DATETIME NOT NULL COMMENT '生效时间',
  status        TINYINT  NOT NULL DEFAULT 0 COMMENT '0待生效 1已生效 2已取消',
  createTime    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updateTime    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_status_time (status, effectiveTime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商品调价公告';

-- ============================================================
-- 种子数据
-- ============================================================

-- 积分规则（4 条）
INSERT INTO points_rule (eventType, name, points, requireVerified, dailyCap, totalCap, enabled, remark) VALUES
('REGISTER',     '注册奖励',     20, 0, NULL, 1,   1, '注册成功发20分,终身1次,幂等键=userId'),
('REAL_NAME',    '首次实名认证', 50, 1, NULL, 1,   1, '首次实名通过发50分,终身1次,幂等键=userId+认证单号'),
('SIGN_IN',      '每日签到',     5,  1, 1,    NULL,1, '每日签到5分,连签+2/天封顶30,幂等键=userId+日期'),
('REPAY_SETTLE', '结清当期奖励', 0,  1, NULL, NULL,1, '动态计算floor(本期已还本金×0.001),幂等键=userId+repaymentId');

-- 示例商品（实体 1 个 + 虚拟码 1 个）
INSERT INTO points_product (name, itemType, pointsPrice, stock, detail, status) VALUES
('定制保温杯',   'PHYSICAL',     500, 100, '示例实体商品,兑换后填写收货地址', 1),
('视频会员月卡', 'VIRTUAL_CODE', 300,  -1, '示例虚拟商品,兑换后发放兑换码',   1);
