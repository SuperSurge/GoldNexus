-- GoldNexus 现有业务表 schema（用户提供）
-- auto-generated definition
create table admin
(
    id       int auto_increment
        primary key,
    name     varchar(10)  null,
    password varchar(100) null
);

-- auto-generated definition
create table ai_chat_message
(
    id           int auto_increment comment '消息ID'
        primary key,
    user_id      int                                not null comment '用户ID',
    role         varchar(16)                        not null comment '角色: user=用户 / assistant=AI',
    content      text                               not null comment '消息内容',
    created_time datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment 'AI智能客服消息表';

create index idx_user_id
    on ai_chat_message (user_id);

create index idx_user_time
    on ai_chat_message (user_id, created_time);

-- auto-generated definition
create table article
(
    articleId   int auto_increment comment '文章ID（自增主键）'
        primary key,
    title       varchar(255)                       not null comment '文章标题',
    content     text                               not null comment '文章正文',
    summary     varchar(500)                       null comment '文章摘要',
    coverImage  varchar(500)                       null comment '封面图片URL',
    isPublished int      default 0                 null comment '发布状态：0-草稿，1-已发布',
    author      varchar(100)                       null comment '作者（管理员用户名）',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '文章表';

-- auto-generated definition
create table customer_service_message
(
    id           int auto_increment comment '消息主键'
        primary key,
    user_id      int                                not null comment '用户ID',
    admin_id     int                                null comment '回复的管理员ID，用户发送时为NULL',
    sender_role  varchar(10)                        not null comment '发送者角色: USER / ADMIN',
    content      text                               not null comment '消息内容',
    is_read      tinyint  default 0                 not null comment '是否已读: 0-未读 1-已读',
    created_time datetime default CURRENT_TIMESTAMP not null comment '发送时间'
)
    comment '智能客服消息表';

create index idx_user_id
    on customer_service_message (user_id);

create index idx_user_read
    on customer_service_message (user_id, is_read);

-- auto-generated definition
create table loan_application
(
    applicationId     int auto_increment
        primary key,
    productId         int                       null,
    name              varchar(20)               null comment '贷款者名字',
    userId            int                       null,
    amount            decimal(10, 2)            null comment '贷款金额',
    interestWay       int                       null comment '计利方式：1等额本息，2等额本金',
    applyTime         timestamp default (now()) null comment '申请时间',
    useWay            varchar(20)               null comment '用途',
    applicationStatus int                       null comment '申请状态：0未通过，1等待审核，2已通过，3审核失败',
    repayStatus       int                       null comment '0未还完，1已还完',
    message           varchar(30)               null
)
    comment '借贷订单信息';

-- auto-generated definition
create table loan_product
(
    productId    int auto_increment
        primary key,
    productName  varchar(20)    not null,
    isRelease    int            not null comment '0未发行，1已发行',
    maxLimit     decimal(10, 2) not null comment '最高额度',
    minLimit     decimal(10, 2) not null comment '最低额度',
    maxThreshold int            not null comment '审核高阈值',
    minThreshold int            not null comment '审核低阈值',
    penalty      decimal(10, 2) not null comment '罚息',
    period int not null comment '分期',
    interest     decimal(10, 2) not null comment '利率'
);

-- auto-generated definition
create table repayment_plan
(
    planId            int auto_increment
        primary key,
    applicationId     int                                      not null,
    periodNumber      int                                      not null comment '第几期',
    dueDate           date                                     not null comment '应还款日期',
    amount            decimal(10, 2)                           not null comment '本期应还金额（本金+利息）',
    principal         decimal(10, 2)                           not null comment '本期本金',
    status            tinyint        default 0                 null comment '0-待还款 1-已还款 2-逾期',
    actualPaymentDate date                                     null comment '实际还款日期',
    paidAmount        decimal(10, 2) default 0.00              null comment '已还金额',
    penaltyAmount     decimal(10, 2)                           null,
    createdTime       timestamp      default CURRENT_TIMESTAMP null,
    updatedTime       timestamp      default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint repayment_plan_ibfk_1
        foreign key (applicationId) references loan_application (applicationId)
            on update cascade on delete cascade
);

create index idx_due_date
    on repayment_plan (dueDate);

create index idx_status
    on repayment_plan (status);

-- auto-generated definition
create table user
(
    id             int auto_increment
        primary key,
    username       varchar(10)  not null,
    password       varchar(100) not null,
    phone          varchar(20)  not null,
    score          int          not null,
    name           varchar(20)  null,
    authentication int          null comment '0未认证，1已认证',
    gender         int          null comment '0-男，1-女',
    age            date         null,
    address        varchar(20)  null,
    marriage       int          null comment '0未婚，1已婚',
    job            varchar(20)  null,
    education      varchar(20)  null,
    earnings       int          null,
    property       int          null comment '资产',
    bad_rate       float        null,
    addressLevel   int          null
);
