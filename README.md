# GoldNexus 金纽贷 —— 微服务后端（含积分商城）

Spring Cloud 微服务项目，包含用户、贷款、内容（文章/客服）、积分商城四大业务域。对外统一通过网关暴露 `/goldnexus/**` 接口，供前端 / 安卓端调用。

## 技术栈

- Java 21（**必须 21**，JDK 24 会导致 Lombok 编译失败）
- Spring Boot 3.5.9 / Spring Cloud 2025.0.0 / Spring Cloud Alibaba 2025.0.0.0（Nacos）
- MyBatis（注解式）、MySQL、Spring Security + JWT、Lombok、Drools（贷款规则）

## 服务与端口

| 服务 | 端口 | 说明 |
|------|------|------|
| gateway | 8080 | 统一网关（对外入口，前缀 `/goldnexus/**`） |
| user-service | 8081 | 用户/管理员认证、用户信息、信用分、人脸识别 |
| loan-service | 8082 | 贷款产品、申请、审核、还款计划、罚息 |
| content-service | 8083 | 文章、AI 客服、人工客服 |
| points-service | 8084 | 积分商城（发放/兑换/调账/过期/对账） |

## 环境要求

| 依赖 | 版本/说明 |
|------|-----------|
| JDK | 21 |
| MySQL | 8.0+ / 9.x |
| Nacos | standalone（8848/9848） |
| Maven | 3.9+（或直接用 IDE 构建运行） |

## 快速启动

### 1. 初始化数据库

```bash
# 建库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS goldnexus DEFAULT CHARACTER SET utf8mb4;"

# 导入原业务表（8 张）
mysql -u root -p goldnexus < sql/goldnexus-tables.sql

# 导入积分商城表（12 张 + 种子数据：积分规则/商品）
mysql -u root -p goldnexus < points-service/src/main/resources/db/schema.sql
```

> 两个 SQL 脚本都应在「全新库」上各执行一次。`schema.sql` 含 `DROP TABLE IF EXISTS`，重复执行会清空积分数据。

### 2. 配置数据库账号

默认 `root / 05240314lzx`。若你的 MySQL 账号不同，用环境变量覆盖即可（无需改代码）：

- `MYSQL_USERNAME`（默认 `root`）
- `MYSQL_PASSWORD`（默认 `05240314lzx`）

Windows（PowerShell）示例：

```powershell
$env:MYSQL_PASSWORD = "你的密码"
```

或直接修改各服务 `src/main/resources/application.yml` 里的 `spring.datasource`。

### 3. 启动 Nacos

```bash
# Windows
startup.cmd -m standalone
```

等待 8848 / 9848 端口就绪（约 30–60 秒）后再启动服务。

### 4. 启动服务

用 IDE 依次启动各服务的 Application 类，或 Maven 打包后运行：

```bash
mvn install -DskipTests

java -jar user-service/target/user-service-0.0.1-SNAPSHOT.jar
java -jar loan-service/target/loan-service-0.0.1-SNAPSHOT.jar
java -jar content-service/target/content-service-0.0.1-SNAPSHOT.jar
java -jar points-service/target/points-service-0.0.1-SNAPSHOT.jar
java -jar gateway/target/gateway-0.0.1-SNAPSHOT.jar
```

### 5. 验证

```bash
curl http://localhost:8084/goldnexus/points/ping
```

## 配置说明

- **数据库**：各服务默认连 `localhost:3306/goldnexus`，账号可用 `MYSQL_USERNAME` / `MYSQL_PASSWORD` 覆盖。
- **服务间调用**：通过 Nacos 服务发现（`@LoadBalanced RestTemplate`，按服务名如 `points-service` 调用），**不写死 IP/端口**，服务跑在同一台或多台机器上均可。
- **积分业务参数**（单日上限、有效期、余额上限、兑换起付线）在 `points-service/src/main/resources/application.yml` 的 `points.*`。

## 目录结构

```
goldnexus-cloud
├── common           公共模块（实体、Res、JWT、鉴权过滤器）
├── gateway          网关（路由 + 跨域）
├── user-service     用户域
├── loan-service     贷款域
├── content-service  内容域
├── points-service   积分商城域
│   └── src/main/resources/db/schema.sql   # 积分表结构与种子数据
└── sql
    └── goldnexus-tables.sql               # 原业务表结构
```

## 积分商城功能速览

- **发放**：注册送分、实名认证送分、签到（连签加成）、贷款结清送分、管理员调账、内部发放接口。
- **消费**：积分兑换商品（下单即扣分）、临期优先扣减、虚拟商品兑换码。
- **风控**：单日发放上限、账户余额上限、幂等防重、条件 UPDATE 原子扣减。
- **运维**：积分批次/流水账本、过期与提醒定时任务、余额=批次=流水三方对账。
