package cn.fzu.edu.furever_home.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.fzu.edu.furever_home.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cn.fzu.edu.furever_home.auth.entity.User;
import cn.fzu.edu.furever_home.auth.request.LoginRequest;
import cn.fzu.edu.furever_home.auth.request.SendCodeRequest;
import cn.fzu.edu.furever_home.auth.request.ConfirmRegisterRequest;
import cn.fzu.edu.furever_home.auth.request.ResetPasswordSendCodeRequest;
import cn.fzu.edu.furever_home.auth.request.ResetPasswordRequest;
import cn.fzu.edu.furever_home.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "登录、注册、验证码发送")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "登录", description = "使用昵称或邮箱加密码登录")
    public Result<SaTokenInfo> login(@RequestBody @Valid LoginRequest req) {
        User u = authService.findByAccount(req.getAccount());
        if (u == null)
            return Result.error(401, "账户不存在");
        if (!authService.matches(req.getPassword(), u.getPasswordHash()))
            return Result.error(401, "密码错误");
        StpUtil.login(u.getUserId());
        return Result.success("登录成功", StpUtil.getTokenInfo());
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<String> logout() {
        StpUtil.logout();
        return Result.success("操作成功", "ok");
    }

    @GetMapping("/check")
    @Operation(summary = "登录态检查")
    public Result<Boolean> check() {
        return Result.success(StpUtil.isLogin());
    }

    @PostMapping("/register/send-code")
    @Operation(summary = "发送注册验证码", description = "向邮箱发送注册验证码")
    public Result<String> sendCode(@RequestBody @Valid SendCodeRequest req) {
        authService.sendRegisterCode(req.getEmail());
        return Result.success("验证码已发送", null);
    }

    @PostMapping("/register/confirm")
    @Operation(summary = "确认注册", description = "校验验证码并创建账号")
    public Result<Integer> confirm(@RequestBody @Valid ConfirmRegisterRequest req) {
        User u = authService.registerWithEmail(req.getEmail(), req.getCode(), req.getUserName(), req.getPassword());
        return Result.success(u.getUserId());
    }

    @PostMapping("/password/send-code")
    @Operation(summary = "发送重置密码验证码")
    public Result<String> sendResetCode(@RequestBody @Valid ResetPasswordSendCodeRequest req) {
        authService.sendResetCode(req.getEmail());
        return Result.success("验证码已发送", null);
    }

    @PostMapping("/password/reset")
    @Operation(summary = "重置密码")
    public Result<String> resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        authService.resetPassword(req.getEmail(), req.getCode(), req.getNewPassword());
        return Result.success("操作成功", null);
    }
}