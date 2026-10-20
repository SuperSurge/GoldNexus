-- 测试库: user 表(积分服务需要 selectUserById 做实名校验)
DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(10)  NOT NULL,
    password       VARCHAR(100) NOT NULL,
    phone          VARCHAR(20)  NOT NULL,
    score          INT          NOT NULL,
    name           VARCHAR(20)  NULL,
    authentication INT          NULL,
    gender         INT          NULL,
    age            DATE         NULL,
    address        VARCHAR(20)  NULL,
    marriage       INT          NULL,
    job            VARCHAR(20)  NULL,
    education      VARCHAR(20)  NULL,
    earnings       INT          NULL,
    property       INT          NULL,
    bad_rate       FLOAT        NULL,
    addressLevel   INT          NULL
);
