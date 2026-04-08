package ynu.edu.controller;

import jakarta.annotation.Resource;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ynu.edu.entity.Cart;
import ynu.edu.entity.User;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Resource
    private DiscoveryClient discoveryClient;

    // 获取服务实例的基础URL
    private String getUserServiceBaseUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances("provider-service");
        ServiceInstance instance = instances.get(0);
        return "http://" + instance.getHost() + ":" + instance.getPort();
    }

    // 1. GET：调用UserController的查询用户方法
    @GetMapping("/getUser/{userId}")
    public User getUser(@PathVariable("userId") Integer userId) {
        String baseUrl = getUserServiceBaseUrl();
        return restTemplate.getForObject(
                baseUrl + "/user/getUserById/" + userId,
                User.class
        );
    }

    // 2. POST：调用UserController的新增用户方法
    @PostMapping("/addUser")
    public String addUser(@RequestBody User user) {
        String baseUrl = getUserServiceBaseUrl();
        return restTemplate.postForObject(
                baseUrl + "/user/addUser",
                user,
                String.class
        );
    }

    // 3. PUT：调用UserController的修改用户方法
    @PutMapping("/updateUser")
    public String updateUser(@RequestBody User user) {
        String baseUrl = getUserServiceBaseUrl();
        restTemplate.put(
                baseUrl + "/user/updateUser",
                user
        );
        return "用户更新请求已发送";
    }

    // 4. DELETE：调用UserController的删除用户方法
    @DeleteMapping("/deleteUser/{userId}")
    public String deleteUser(@PathVariable("userId") Integer userId) {
        String baseUrl = getUserServiceBaseUrl();
        restTemplate.delete(
                baseUrl + "/user/deleteUser/" + userId
        );
        return "用户删除请求已发送，用户ID：" + userId;
    }

    // 原获取购物车方法
    @GetMapping("/getCartByUserId/{userId}")
    public Cart getCartByUserId(@PathVariable("userId") Integer userId) {
        // 调用UserController获取用户信息（GET方法）
        User user = getUser(userId);

        // 模拟购物车商品列表
        List<String> goodList = Arrays.asList("苹果手机", "华为手表", "小米手环");

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setGoodList(goodList);

        return cart;
    }
}