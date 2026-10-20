package com.goldnexusbackend;

import com.goldnexusbackend.entity.Admin;
import com.goldnexusbackend.entity.User;
import com.goldnexusbackend.service.ReconcileService;
import com.goldnexusbackend.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 积分服务集成测试：Testcontainers 拉起真实 MySQL 容器 + 真实 Spring 上下文 + 真实 HTTP。
 * 覆盖核心发放/兑换/签到/查询/对账 + 管理端调账/冻结/清零/发货/规则/商品/过期任务。
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PointsServiceIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("goldnexus_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> mysql.getJdbcUrl() + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8");
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @Autowired TestRestTemplate restTemplate;
    @Autowired JwtUtil jwtUtil;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired ReconcileService reconcileService;

    // ---------- 核心 ----------

    @Test
    void ping() {
        ResponseEntity<String> r = restTemplate.getForEntity("/goldnexus/points/ping", String.class);
        assertEquals(200, r.getStatusCode().value());
    }

    @Test
    void internalGrantExplicitPointsAndIdempotency() {
        grant("it-biz-1", 1, 20);
        assertEquals(20, balance(1));
        grant("it-biz-1", 1, 20); // 幂等
        assertEquals(20, balance(1));
    }

    @Test
    void internalGrantNullPointsUsesRule() {
        grant("it-biz-2", 1, null); // 回退规则 REGISTER=20
        assertEquals(20, balance(1));
    }

    @Test
    void exchangeDeductsBalance() {
        grant("it-biz-3", 1, 1000);
        ResponseEntity<Map> r = postUser("/goldnexus/mall/order/create", Map.of(
                "requestId", "ex-req-1", "productId", 1, "addressInfo", "Beijing"));
        assertEquals(200, r.getBody().get("code"));
        assertEquals(500, balance(1));
        assertEquals(1, count("select count(*) from points_order where userId = 1"));
    }

    // ---------- 用户端: 签到 / 查询 ----------

    @Test
    void signAndSummary() {
        grant("it-biz-4", 1, 100);
        // 签到 +5
        ResponseEntity<Map> r = postUser("/goldnexus/points/sign", new HashMap<>());
        assertEquals(200, r.getBody().get("code"));
        assertEquals(105, balance(1));
        // 重复签到拦截
        r = postUser("/goldnexus/points/sign", new HashMap<>());
        assertEquals(500, r.getBody().get("code"));
        // 账户总览
        ResponseEntity<Map> s = get("/goldnexus/points/account/summary");
        assertEquals(200, s.getStatusCode().value());
        assertEquals(105, ((Map) s.getBody().get("data")).get("balance"));
    }

    @Test
    void rulesListHasFourRules() {
        ResponseEntity<Map> r = restTemplate.exchange(
                "/goldnexus/points/rules", HttpMethod.GET,
                new HttpEntity<>(authHeader(userToken(1))), Map.class);
        assertEquals(200, r.getStatusCode().value());
        assertEquals(4, ((List) r.getBody().get("data")).size());
    }

    @Test
    void reconcileIsBalanced() {
        grant("it-biz-5", 1, 30);
        assertTrue(reconcileService.reconcile(1));
    }

    // ---------- 管理端: 调账 / 冻结 / 清零 / 发货 ----------

    @Test
    void adminAdjustAddAndSub() {
        grant("it-biz-6", 1, 100);
        ResponseEntity<Map> r = postAdmin("/goldnexus/admin/points/adjust",
                Map.of("userId", 1, "changeType", "ADD", "amount", 100, "reason", "t"));
        assertEquals(200, r.getBody().get("code"));
        assertEquals(200, balance(1));
        r = postAdmin("/goldnexus/admin/points/adjust",
                Map.of("userId", 1, "changeType", "SUB", "amount", 50, "reason", "t"));
        assertEquals(200, r.getBody().get("code"));
        assertEquals(150, balance(1));
    }

    @Test
    void adminFreezeUnfreezeClear() {
        grant("it-biz-7", 1, 500);
        postAdmin("/goldnexus/admin/points/freeze", Map.of("userId", 1));
        // 冻结后兑换被拦
        ResponseEntity<Map> r = postUser("/goldnexus/mall/order/create", Map.of(
                "requestId", "ex-7", "productId", 1, "addressInfo", "x"));
        assertEquals(500, r.getBody().get("code"));
        postAdmin("/goldnexus/admin/points/unfreeze", Map.of("userId", 1));
        postAdmin("/goldnexus/admin/points/clear", Map.of("userId", 1));
        assertEquals(0, balance(1));
    }

    @Test
    void adminShipOrder() {
        grant("it-biz-8", 1, 1000);
        ResponseEntity<Map> ex = postUser("/goldnexus/mall/order/create", Map.of(
                "requestId", "ex-8", "productId", 1, "addressInfo", "x"));
        String orderNo = (String) ((Map) ex.getBody().get("data")).get("orderNo");
        ResponseEntity<Map> r = postAdmin("/goldnexus/admin/points/order/ship",
                Map.of("orderNo", orderNo, "logisticsNo", "SF123"));
        assertEquals(200, r.getBody().get("code"));
        assertEquals("COMPLETED",
                jdbcTemplate.queryForObject("select status from points_order where orderNo = '" + orderNo + "'", String.class));
    }

    // ---------- 管理端: 规则 / 商品 / 过期任务 ----------

    @Test
    void adminRuleUpdate() {
        ResponseEntity<Map> r = postAdmin("/goldnexus/admin/points/rule/update",
                Map.of("id", 1, "points", 30, "requireVerified", 0, "totalCap", 1, "enabled", 1));
        assertEquals(200, r.getBody().get("code"));
        grant("it-biz-9", 1, null); // 现在规则 REGISTER=30
        assertEquals(30, balance(1));
    }

    @Test
    void adminProductPriceCreatesAnnouncement() {
        ResponseEntity<Map> r = postAdmin("/goldnexus/admin/points/product/price",
                Map.of("productId", 1, "newPrice", 520));
        assertEquals(200, r.getBody().get("code"));
        assertEquals(1, count("select count(*) from points_announcement where productId = 1"));
    }

    @Test
    void expireTaskReducesBalance() {
        grant("it-biz-10", 1, 50);
        // 把批次置为已过期
        jdbcTemplate.update("update points_batch set expiresAt = date_sub(now(), interval 1 day) where userId = 1");
        ResponseEntity<Map> r = postAdmin("/goldnexus/admin/points/ops/expire", new HashMap<>());
        assertEquals(200, r.getBody().get("code"));
        assertEquals(0, balance(1));
    }

    // ---------- 商城: 商品列表/详情 订单列表/详情 ----------

    @Test
    void productListAndDetail() {
        ResponseEntity<Map> r = restTemplate.exchange("/goldnexus/mall/products", HttpMethod.GET,
                new HttpEntity<>(authHeader(userToken(1))), Map.class);
        assertEquals(200, r.getStatusCode().value());
        assertEquals(2, ((Map) r.getBody().get("data")).get("total"));
        r = restTemplate.exchange("/goldnexus/mall/products/1", HttpMethod.GET,
                new HttpEntity<>(authHeader(userToken(1))), Map.class);
        assertEquals(200, r.getStatusCode().value());
    }

    @Test
    void orderListAndDetail() {
        grant("it-biz-11", 1, 1000);
        postUser("/goldnexus/mall/order/create", Map.of("requestId", "ex-11", "productId", 1, "addressInfo", "x"));
        ResponseEntity<Map> r = restTemplate.exchange("/goldnexus/mall/order/list", HttpMethod.GET,
                new HttpEntity<>(authHeader(userToken(1))), Map.class);
        assertEquals(200, r.getStatusCode().value());
        String orderNo = jdbcTemplate.queryForObject("select orderNo from points_order where userId = 1", String.class);
        r = restTemplate.exchange("/goldnexus/mall/order/detail?orderNo=" + orderNo, HttpMethod.GET,
                new HttpEntity<>(authHeader(userToken(1))), Map.class);
        assertEquals(200, r.getStatusCode().value());
    }

    // ---------- 用户端: 账单 ----------

    @Test
    void accountBill() {
        grant("it-biz-12", 1, 20);
        String month = java.time.YearMonth.now().toString();
        ResponseEntity<Map> r = restTemplate.exchange(
                "/goldnexus/points/account/bill?month=" + month, HttpMethod.GET,
                new HttpEntity<>(authHeader(userToken(1))), Map.class);
        assertEquals(200, r.getStatusCode().value());
        assertTrue(((List) ((Map) r.getBody().get("data")).get("list")).size() >= 1);
    }

    // ---------- 管理端: 商品新增/上下架 ----------

    @Test
    void adminProductSaveAndOnoff() {
        ResponseEntity<Map> r = postAdmin("/goldnexus/admin/points/product/save", Map.of(
                "name", "test-product", "itemType", "PHYSICAL", "pointsPrice", 100, "stock", 5));
        assertEquals(200, r.getBody().get("code"));
        int pid = jdbcTemplate.queryForObject("select id from points_product where name = 'test-product'", Integer.class);
        r = postAdmin("/goldnexus/admin/points/product/onoff", Map.of("productId", pid, "status", 0));
        assertEquals(200, r.getBody().get("code"));
        assertEquals(0, jdbcTemplate.queryForObject("select status from points_product where id = " + pid, Integer.class));
    }

    // ---------- 过期提醒 ----------

    @Test
    void remindTaskCreatesMessage() {
        grant("it-biz-13", 1, 20);
        jdbcTemplate.update("update points_batch set expiresAt = date_add(now(), interval 3 day) where userId = 1");
        ResponseEntity<Map> r = postAdmin("/goldnexus/admin/points/ops/remind", new HashMap<>());
        assertEquals(200, r.getBody().get("code"));
        assertEquals(1, count("select count(*) from points_message where userId = 1 and type = 'POINT_EXPIRING'"));
    }

    // ---------- helpers ----------

    private void grant(String bizNo, Integer userId, Integer points) {
        Map<String, Object> body = new HashMap<>();
        body.put("eventType", "REGISTER");
        body.put("bizNo", bizNo);
        body.put("userId", userId);
        if (points != null) {
            body.put("points", points);
        }
        ResponseEntity<Map> r = restTemplate.postForEntity(
                "/goldnexus/internal/grant", new HttpEntity<>(body, jsonHeaders()), Map.class);
        assertEquals(200, r.getBody().get("code"));
    }

    private ResponseEntity<Map> postUser(String path, Map<String, Object> body) {
        HttpHeaders h = jsonHeaders();
        h.setBearerAuth(userToken(1));
        return restTemplate.postForEntity(path, new HttpEntity<>(body, h), Map.class);
    }

    private ResponseEntity<Map> postAdmin(String path, Map<String, Object> body) {
        HttpHeaders h = jsonHeaders();
        h.setBearerAuth(adminToken(1));
        return restTemplate.postForEntity(path, new HttpEntity<>(body, h), Map.class);
    }

    private ResponseEntity<Map> get(String path) {
        return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(authHeader(userToken(1))), Map.class);
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private HttpHeaders authHeader(String token) {
        HttpHeaders h = jsonHeaders();
        h.setBearerAuth(token);
        return h;
    }

    private int balance(int userId) {
        Integer b = jdbcTemplate.queryForObject(
                "select balance from points_account where userId = " + userId, Integer.class);
        return b == null ? 0 : b;
    }

    private int count(String sql) {
        Integer c = jdbcTemplate.queryForObject(sql, Integer.class);
        return c == null ? 0 : c;
    }

    private String userToken(int id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user" + id);
        u.setPassword("x");
        return jwtUtil.generateUserToken(u);
    }

    private String adminToken(int id) {
        Admin a = new Admin();
        a.setId(id);
        a.setName("admin");
        a.setPassword("x");
        return jwtUtil.generateAdminToken(a);
    }
}
