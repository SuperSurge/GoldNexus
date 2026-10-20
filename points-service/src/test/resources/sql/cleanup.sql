-- 每个测试方法前重置可变数据(种子规则/商品也还原, 保证测试隔离)
DELETE FROM points_order;
DELETE FROM points_code;
DELETE FROM points_flow;
DELETE FROM points_batch;
DELETE FROM points_sign;
DELETE FROM points_adjust;
DELETE FROM points_daily_quota;
DELETE FROM points_account;
DELETE FROM points_announcement;
DELETE FROM points_message;
UPDATE points_product SET stock = 100, pointsPrice = 500 WHERE id = 1;
UPDATE points_product SET stock = -1, pointsPrice = 300 WHERE id = 2;
UPDATE points_rule SET points = 20 WHERE eventType = 'REGISTER';
UPDATE points_rule SET points = 50 WHERE eventType = 'REAL_NAME';
UPDATE points_rule SET points = 5  WHERE eventType = 'SIGN_IN';
UPDATE points_rule SET points = 0  WHERE eventType = 'REPAY_SETTLE';
DELETE FROM points_product WHERE id > 2;
