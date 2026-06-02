# Experiment2 — Spring Cloud 微服务 Restful 通信与负载均衡项目

基于 Spring Cloud + OpenFeign + Eureka 的微服务间 RESTful 通信项目，演示四种 HTTP 方法（GET/POST/PUT/DELETE）的服务间调用、OpenFeign 声明式客户端配置，以及自定义随机负载均衡策略。

## 技术栈

| 组件                          | 版本       |
|-----------------------------|----------|
| Java                        | 17       |
| Spring Boot                 | 4.0.3    |
| Spring Cloud                | 2025.1.1 |
| Spring Cloud Netflix Eureka | —        |
| Spring Cloud OpenFeign      | —        |
| Spring Cloud LoadBalancer   | —        |
| Lombok                      | 1.18.42  |

## 模块架构

```
Experiment2
├── Service_Eureka_19000/19001/19002  ← 注册中心集群 (3节点)
├── Provider_15001/15002/15003        ← 服务提供者 'provider-service' (3实例)
├── Public/                           ← 共享模块 (实体类 + Feign接口)
├── Consumer_11001/                   ← ★ 服务消费者 (OpenFeign + 随机负载均衡)
└── Consumer_11002/                   ← 服务消费者 (基础)
```

## Consumer_11001 — 核心消费者

### 依赖

```xml
spring-cloud-starter-openfeign       <!-- OpenFeign 声明式HTTP客户端 -->
spring-cloud-starter-loadbalancer    <!-- 客户端负载均衡 -->
spring-cloud-starter-netflix-eureka-client  <!-- Eureka 服务发现 -->
```

### 负载均衡策略

Consumer_11001 通过 `@LoadBalancerClient` 注解配置了**随机（Random）负载均衡策略**，替代默认的轮询策略。

```java
@LoadBalancerClient(name = "provider-service", configuration = RandomLoadBalancerConfig.class)
```

| 策略类型 | 实现类                  | 说明           |
|------|----------------------|--------------|
| 随机   | `RandomLoadBalancer` | 从可用实例中随机选取一个 |

### Feign 接口 (UserService)

Consumer_11001 通过 `@FeignClient("provider-service")` 声明式调用远程 `provider-service` 的 User 服务：

| 方法     | 远程路径                          | 说明         |
|--------|-------------------------------|------------|
| GET    | `/user/getUserById/{userId}`  | 根据ID查询用户   |
| POST   | `/user/addUser`               | 新增用户       |
| PUT    | `/user/updateUser`            | 修改用户信息     |
| DELETE | `/user/deleteUser/{userId}`   | 根据ID删除用户   |

### API 端点总览

| 方法     | 路径                               | 说明                       | 调用链路                                                          |
|--------|----------------------------------|--------------------------|---------------------------------------------------------------|
| GET    | `/cart/getUser/{userId}`         | 查询用户信息                   | CartController → UserService(Feign) → Provider/UserController |
| POST   | `/cart/addUser`                  | 新增用户                     | CartController → UserService(Feign) → Provider/UserController |
| PUT    | `/cart/updateUser`               | 修改用户信息                   | CartController → UserService(Feign) → Provider/UserController |
| DELETE | `/cart/deleteUser/{userId}`      | 删除用户                     | CartController → UserService(Feign) → Provider/UserController |
| GET    | `/cart/getCartByUserId/{userId}` | 查询用户购物车（聚合用户信息 + 模拟商品列表） | CartController → getUser(Feign) + 组装Cart                      |

## Provider — 服务提供者

服务提供者集群共 3 个实例（端口 15001/15002/15003），均注册为 `provider-service`。

### 提供的 REST 接口

| 模块      | 方法     | 路径                                    | 说明                 |
|---------|--------|---------------------------------------|--------------------|
| User    | GET    | `/user/getUserById/{userId}`          | 根据ID查询用户（返回User实体） |
| User    | POST   | `/user/addUser`                       | 新增用户               |
| User    | PUT    | `/user/updateUser`                    | 修改用户信息             |
| User    | DELETE | `/user/deleteUser/{userId}`           | 根据ID删除用户           |
| Product | GET    | `/product/getProductById/{productId}` | 根据ID查询商品信息         |

### 多实例数据差异

Provider_15001 中初始化的测试用户 `userId=1` 用户名为 `"小明-from 15001"`，用于区分不同实例返回的数据，便于验证负载均衡策略是否生效。

## Consumer_11002 — 基础消费者

Consumer_11002 同样启用 OpenFeign，提供独立的订单查询接口：

| 方法  | 路径                              | 说明         |
|-----|---------------------------------|------------|
| GET | `/order/getOrderById/{orderId}` | 根据ID查询订单信息 |

## 运行前提

1. 启动 Eureka 注册中心集群 (19000 → 19001 → 19002)
2. 启动 Provider 服务实例 (15001 → 15002 → 15003)
3. 启动 Consumer_11001 (11001) / Consumer_11002 (11002)
4. 访问 `http://eurekaService19000:19000` 查看 Eureka Dashboard，确认所有实例均已注册

### 验证示例

```bash
# 测试 GET 查询用户
curl http://localhost:11001/cart/getUser/1

# 测试 POST 新增用户
curl -X POST http://localhost:11001/cart/addUser \
  -H "Content-Type: application/json" \
  -d '{"userId":3,"userName":"testUser","userPassword":"pass123"}'

# 测试 PUT 修改用户
curl -X PUT http://localhost:11001/cart/updateUser \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"userName":"updatedName","userPassword":"newpass"}'

# 测试 DELETE 删除用户
curl -X DELETE http://localhost:11001/cart/deleteUser/3

# 测试购物车聚合查询
curl http://localhost:11001/cart/getCartByUserId/1

# 测试订单查询 (Consumer_11002)
curl http://localhost:11002/order/getOrderById/1001
```
