package com.example.controller;

import com.example.model.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功", 
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/Result")))
    })
    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        return "用户信息: ID=" + id;
    }

    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    public String createUser(@RequestBody String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        return "用户创建成功: " + username;
    }
}