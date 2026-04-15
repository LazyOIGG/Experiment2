package ynu.edu.controller;

import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.*;
import ynu.edu.entity.Cart;
import ynu.edu.entity.User;
import ynu.edu.feign.UserService;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Resource
    private UserService userService;

    // 1. GET：调用UserController的查询用户方法
    @GetMapping("/getUser/{userId}")
    public User getUser(@PathVariable("userId") Integer userId) {
        return userService.getUserById(userId);
    }

    // 2. POST：调用UserController的新增用户方法
    @PostMapping("/addUser")
    public String addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    // 3. PUT：调用UserController的修改用户方法
    @PutMapping("/updateUser")
    public String updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    // 4. DELETE：调用UserController的删除用户方法
    @DeleteMapping("/deleteUser/{userId}")
    public String deleteUser(@PathVariable("userId") Integer userId) {
        return userService.deleteUser(userId);
    }

    // 获取购物车方法
    @GetMapping("/getCartByUserId/{userId}")
    public Cart getCartByUserId(@PathVariable("userId") Integer userId) {
        User user = getUser(userId);

        // 模拟购物车商品列表
        List<String> goodList = Arrays.asList("苹果手机", "华为手表", "小米手环");

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setGoodList(goodList);

        return cart;
    }
}