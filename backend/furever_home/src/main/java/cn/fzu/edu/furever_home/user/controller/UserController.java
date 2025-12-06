package cn.fzu.edu.furever_home.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import cn.fzu.edu.furever_home.user.dto.UserDTO;
import cn.fzu.edu.furever_home.user.dto.UserStatsDTO;
import cn.fzu.edu.furever_home.user.request.UpdateUserRequest;
import cn.fzu.edu.furever_home.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import cn.fzu.edu.furever_home.common.result.Result;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "个人信息查看与修改、他人信息查看")
public class UserController {
    private final UserProfileService userProfileService;

    @GetMapping("/me")
    @Operation(summary = "获取我的信息")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<UserDTO> me() {
        Integer uid = StpUtil.getLoginIdAsInt();
        UserDTO dto = userProfileService.getMe(uid);
        return Result.success(dto);
    }

    @PutMapping("/me")
    @Operation(summary = "更新我的信息")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<Void> updateMe(@RequestBody @Valid UpdateUserRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        userProfileService.updateMe(uid, req);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取他人信息")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<UserDTO> getById(@PathVariable Integer id) {
        UserDTO dto = userProfileService.getById(id);
        return Result.success(dto);
    }

    @GetMapping("/me/stats")
    @Operation(summary = "获取我的统计数据")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<UserStatsDTO> myStats() {
        Integer uid = StpUtil.getLoginIdAsInt();
        UserStatsDTO dto = userProfileService.getMyStats(uid);
        return Result.success(dto);
    }
}
