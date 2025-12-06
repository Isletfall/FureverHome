package cn.fzu.edu.furever_home.adopt.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.fzu.edu.furever_home.adopt.dto.AdoptDTO;
import cn.fzu.edu.furever_home.adopt.request.ReviewAdoptRequest;
import cn.fzu.edu.furever_home.adopt.request.SubmitAdoptRequest;
import cn.fzu.edu.furever_home.adopt.request.UpdateAdoptRequest;
import cn.fzu.edu.furever_home.adopt.service.AdoptService;
import cn.fzu.edu.furever_home.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/adopt")
@RequiredArgsConstructor
@Tag(name = "领养申请", description = "提交、查看、审核领养申请")
public class AdoptController {
    private final AdoptService adoptService;

    @PostMapping
    @SaCheckPermission("adopt:apply")
    @Operation(summary = "提交领养申请")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<Integer> submit(@RequestBody @Valid SubmitAdoptRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        Integer id = adoptService.submit(uid, req);
        return Result.success(id);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("adopt:read")
    @Operation(summary = "获取领养申请详情")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<AdoptDTO> detail(@Parameter(description = "领养申请ID") @PathVariable Integer id) {
        AdoptDTO dto = adoptService.getById(id);
        return Result.success(dto);
    }

    @PostMapping("/{id}/review")
    @SaCheckPermission("adopt:apply")
    @Operation(summary = "审核领养申请", description = "宠物主人审核是否通过领养申请（设置申请状态）")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<Void> review(@Parameter(description = "领养申请ID") @PathVariable Integer id,
            @RequestBody @Valid ReviewAdoptRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        adoptService.review(uid, id, req.getApplicationStatus());
        return Result.success();
    }

    @GetMapping("/todo")
    @SaCheckPermission("adopt:read")
    @Operation(summary = "获取我的待办（别人对我的申请）列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<java.util.List<cn.fzu.edu.furever_home.adopt.dto.AdoptTodoItemDTO>> myTodo() {
        Integer uid = StpUtil.getLoginIdAsInt();
        java.util.List<cn.fzu.edu.furever_home.adopt.dto.AdoptTodoItemDTO> list = adoptService.listTodoItems(uid);
        return Result.success(list);
    }

    @GetMapping("/mine")
    @SaCheckPermission("adopt:read")
    @Operation(summary = "获取我的申请列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<java.util.List<cn.fzu.edu.furever_home.adopt.dto.MyAdoptItemDTO>> myApplications() {
        Integer uid = StpUtil.getLoginIdAsInt();
        java.util.List<cn.fzu.edu.furever_home.adopt.dto.MyAdoptItemDTO> list = adoptService
                .listMyApplicationItems(uid);
        return Result.success(list);
    }

    @DeleteMapping("/mine/{id}")
    @SaCheckPermission("adopt:apply")
    @Operation(summary = "撤销我的领养申请")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<Void> cancel(@Parameter(description = "领养申请ID") @PathVariable Integer id) {
        Integer uid = StpUtil.getLoginIdAsInt();
        adoptService.cancel(uid, id);
        return Result.success();
    }

    @PutMapping("/mine/{id}")
    @SaCheckPermission("adopt:apply")
    @Operation(summary = "修改我的领养申请", description = "修改后需要重新审核")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<Void> updateMyApplication(@Parameter(description = "领养申请ID") @PathVariable Integer id,
            @RequestBody @Valid UpdateAdoptRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        adoptService.updateMyApplication(uid, id, req);
        return Result.success();
    }

}
