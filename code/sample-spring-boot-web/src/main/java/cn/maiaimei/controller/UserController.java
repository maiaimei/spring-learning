package cn.maiaimei.controller;

import cn.maiaimei.model.domain.User;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

  @GetMapping
  public List<User> getUsers() {
    log.info("get users");
    return List.of();
  }

  @GetMapping("/{id}")
  public User getUser(@PathVariable(name = "id") BigDecimal id) {
    log.info("get user by id: {}", id);
    return new User();
  }

  @PostMapping
  public User createUser(@RequestBody User user) {
    log.info("create user {}", user);
    return user;
  }

  @PatchMapping
  public User updateUser(@RequestBody User user) {
    log.info("update user {}", user);
    return user;
  }

  @DeleteMapping("/{id}")
  public String deleteUser(@PathVariable(name = "id") BigDecimal id) {
    log.info("delete user by id: {}", id);
    return "success";
  }

}
