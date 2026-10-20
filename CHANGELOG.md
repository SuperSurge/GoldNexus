# 改动说明（相对原 GoldNexus 项目的修改）

本文档记录在原 GoldNexus 微服务项目（user/loan/content/gateway/common 五模块）基础上，新增的「积分商城」功能，以及对现有代码的每一处改动及原因。

---

## 一、新增模块

### points-service（积分商城，端口 8084）
- **账本模型**：积分账户 `points_account`、批次 `points_batch`（带有效期）、流水 `points_flow`（只增不改）
- **发放引擎** `GrantService`：注册 / 实名 / 签到 / 结清还款 / 管理员调账 / 内部发放的统一入口，含幂等键 + 单日上限 + 余额上限
- **兑换引擎** `ExchangeService`：临期优先扣减（FIFO）、requestId 幂等、库存条件扣减防超卖、虚拟商品兑换码
- **风控**：单日发放上限、账户余额上限、条件 UPDATE 原子扣减、唯一索引防重
- **管理端**：调账（留痕）、冻结/解冻/清零、发货、核销、规则维护、商品管理、调价公告
- **定时任务**：积分过期、过期提醒
- **对账** `ReconcileService`：余额 = Σ批次剩余 = Σ流水
- 建表脚本：`points-service/src/main/resources/db/schema.sql`

### 其他新增
- `sql/goldnexus-tables.sql`：原库 8 张业务表建表脚本（从项目外拷入，方便他人一键建库）
- `README.md`：环境要求、建库、启动、配置说明

---

## 二、对现有服务的改动

### 1. 父 pom.xml
| 改动 | 原因 |
|------|------|
| 新增 `<module>points-service</module>` | 聚合积分服务模块 |

### 2. gateway（application.yml）
| 改动 | 原因 |
|------|------|
| 新增积分服务路由 `/goldnexus/points/**`、`/goldnexus/mall/**`、`/goldnexus/admin/points/**` | 网关转发到积分服务 |

### 3. user-service
| 文件 | 改动 | 原因 |
|------|------|------|
| `UserService.java` | 注册/实名后回调积分服务发分（`grantPoints`） | 注册、实名联动送积分 |
| `UserService.java` | 移除 `authentication()` 的 `@Transactional` | 修复实名送分丢失（事务未提交就回调） |
| `UserService.java` | 回调分值不再硬编码 20/50，改传 null | 让 `points_rule` 表驱动固定分值 |
| `UserService.java` | 注入 `@LoadBalanced RestTemplate` | 按服务名调用积分服务（不再写死 localhost） |
| `pom.xml` | 加 `spring-cloud-starter-loadbalancer` | 服务间负载均衡 |
| `RestTemplateConfig.java`（新增） | `@LoadBalanced RestTemplate` Bean | 同上 |
| `AdminUserMapper.java` | `selectUserByName` 改查 `username` 列 | 修复查错列（原查实名 name） |
| `UserMapper.java` | `updateUserInfo` 加注释 | 说明 name 为实名、仅认证写入 |
| `application.yml` | 密码外置 `${MYSQL_USERNAME}/${MYSQL_PASSWORD}` | 可移植（环境变量覆盖） |

### 4. loan-service
| 文件 | 改动 | 原因 |
|------|------|------|
| `UserRepayService.java` | 结清还款后回调发分（`grantPoints`，`floor(本金×0.001)`） | 结清还款联动送积分 |
| `UserRepayService.java` | 全部还清后更新 `repayStatus=1` | 修复结清状态不更新 |
| `UserRepayMapper.java` | 新增 `updateRepayStatus` | 同上 |
| `UserLoanService.java` | 删除死代码 `calculatePenalty` | 罚息率口径与 `check_overdue` 不一致 |
| `pom.xml` | 加 `spring-cloud-starter-loadbalancer` | 服务发现 |
| `RestTemplateConfig.java`（新增） | `@LoadBalanced RestTemplate` Bean | 同上 |
| `application.yml` | 密码外置 | 可移植 |

### 5. common
| 文件 | 改动 | 原因 |
|------|------|------|
| `GlobalExceptionHandler.java`（新增） | 统一异常处理返回 `Res(500)` | 修复未捕获异常被 Spring Security 误转 401 |

### 6. content-service
| 文件 | 改动 | 原因 |
|------|------|------|
| `application.yml` | 密码外置 | 可移植 |

---

## 三、Bug 修复汇总

1. **实名送分丢失**：`authentication()` 的 `@Transactional` 导致回调时事务未提交 → 移除
2. **过期积分仍可消费**：`selectAvailableForUpdate` 未过滤已过期批次 → 加 `expiresAt >= now()`
3. **累计消费 totalSpent 被污染**：过期/调账扣减误用了 `deductSpent` → 新增 `deductBalance`（只减余额不计消费）
4. **规则分值不生效**：发放分值由调用方硬编码 → `GrantService` 回退到 `rule.points`
5. **selectUserByName 查错列**：原查 `name`（实名）→ 改查 `username`（登录名）
6. **异常统一变 401**：缺全局异常处理 → common 加 `GlobalExceptionHandler`
7. **结清 repayStatus 不更新**：还款后未置「已还完」→ 加 all-paid 判断
8. **罚息率口径不一致**：死代码 `calculatePenalty`（÷100）与 `check_overdue`（不÷100）冲突 → 删除死代码

---

## 四、可移植性改进

1. 原库建表脚本拷入项目（`sql/goldnexus-tables.sql`），clone 即可建库
2. 数据库账号密码外置为环境变量（`MYSQL_USERNAME` / `MYSQL_PASSWORD`）
3. 服务间回调改为 Nacos 服务发现（`@LoadBalanced RestTemplate`），不写死 IP/端口
4. `README.md` 提供完整启动说明
5. `.gitignore` 补充日志文件排除

---

## 五、测试与质量

- **单元测试**：`GrantServiceTest`、`ExchangeServiceTest`（Mockito）
- **集成测试**：`PointsServiceIntegrationTest`（Testcontainers 拉起真实 MySQL 容器 + 真实 HTTP，18 个用例，覆盖发放/兑换/签到/调账/冻结/清零/发货/规则/商品/过期/提醒/对账/账单/商城）
- **覆盖率**：JaCoCo 行覆盖 **85.3%**
- **负载测试**：JMeter（读 1.9 万 TPS、写 27ms、0 错误）
