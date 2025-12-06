package cn.fzu.edu.furever_home.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.fzu.edu.furever_home.admin.dto.AdminAdoptDetailDTO;
import cn.fzu.edu.furever_home.admin.dto.AdminAdoptSummaryDTO;
import cn.fzu.edu.furever_home.admin.request.AdoptReviewRequest;
import cn.fzu.edu.furever_home.admin.service.AdminAdoptService;
import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/adopts")
@RequiredArgsConstructor
@Tag(name = "管理后台-领养申请管理", description = "领养申请的列表与审核")
public class AdminAdoptController {

    private final AdminAdoptService adminAdoptService;

    @GetMapping("/pending")
    @SaCheckPermission("adopt:read")
    @Operation(summary = "获取待审核领养申请列表", description = "分页查询待审核领养申请，支持按ID或姓名/宠物名称搜索")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = true, example = "Bearer xxxxxx")
    public Result<PageResult<AdminAdoptSummaryDTO>> listPending(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "关键字：申请ID / 用户ID / 宠物ID 或 姓名/宠物名称模糊") @RequestParam(required = false) String keyword) {
        PageResult<AdminAdoptSummaryDTO> data = adminAdoptService.listPending(page, pageSize, keyword);
        return Result.success(data);
    }

    @GetMapping("/processed")
    @SaCheckPermission("adopt:read")
    @Operation(summary = "获取已处理领养申请列表", description = "分页查询已审核的领养申请，搜索规则同待审核列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = true, example = "Bearer xxxxxx")
    public Result<PageResult<AdminAdoptSummaryDTO>> listProcessed(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "关键字：申请ID / 用户ID / 宠物ID 或 姓名/宠物名称模糊") @RequestParam(required = false) String keyword) {
        PageResult<AdminAdoptSummaryDTO> data = adminAdoptService.listProcessed(page, pageSize, keyword);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("adopt:read")
    @Operation(summary = "获取领养申请详情", description = "根据领养申请ID获取详情")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = true, example = "Bearer xxxxxx")
    public Result<AdminAdoptDetailDTO> getDetail(
            @Parameter(description = "申请ID") @PathVariable Integer id) {
        AdminAdoptDetailDTO dto = adminAdoptService.getDetail(id);
        return Result.success(dto);
    }

    @PostMapping("/{id}/approve")
    @SaCheckPermission("adopt:review")
    @Operation(summary = "审核通过领养申请", description = "审核通过指定领养申请，并自动拒绝同宠物的其他待审核申请")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = true, example = "Bearer xxxxxx")
    public Result<Void> approve(
            @Parameter(description = "申请ID") @PathVariable Integer id,
            @RequestBody(required = false) @Valid AdoptReviewRequest body) {
        Integer reviewerId = StpUtil.getLoginIdAsInt();
        String reason = body == null ? null : body.getReason();
        adminAdoptService.approve(reviewerId, id, reason);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    @SaCheckPermission("adopt:review")
    @Operation(summary = "审核拒绝领养申请", description = "审核拒绝指定领养申请并记录原因")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = true, example = "Bearer xxxxxx")
    public Result<Void> reject(
            @Parameter(description = "申请ID") @PathVariable Integer id,
            @RequestBody @Valid AdoptReviewRequest body) {
        Integer reviewerId = StpUtil.getLoginIdAsInt();
        adminAdoptService.reject(reviewerId, id, body.getReason());
        return Result.success();
    }
}
