package cn.fzu.edu.furever_home.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.fzu.edu.furever_home.admin.dto.AdminPostDetailDTO;
import cn.fzu.edu.furever_home.admin.dto.AdminPostSummaryDTO;
import cn.fzu.edu.furever_home.admin.request.PostReviewRequest;
import cn.fzu.edu.furever_home.admin.service.AdminPostService;
import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/posts")
@RequiredArgsConstructor
@Tag(name = "管理后台-帖子管理")
public class AdminPostController {

    private final AdminPostService adminPostService;

    @GetMapping("/pending")
    //@SaCheckRole("ADMIN")
    @Operation(summary = "获取待审核帖子列表")
    public Result<PageResult<AdminPostSummaryDTO>> listPending(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "按标题模糊搜索") @RequestParam(required = false) String keyword) {
        PageResult<AdminPostSummaryDTO> data = adminPostService.listPending(page, pageSize, keyword);
        return Result.success(data);
    }

    @GetMapping("/published")
    //@SaCheckRole("ADMIN")
    @Operation(summary = "获取已发布帖子列表")
    public Result<PageResult<AdminPostSummaryDTO>> listPublished(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "按标题模糊搜索") @RequestParam(required = false) String keyword) {
        PageResult<AdminPostSummaryDTO> data = adminPostService.listPublished(page, pageSize, keyword);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    //@SaCheckRole("ADMIN")
    @Operation(summary = "获取帖子详情（含评论）")
    public Result<AdminPostDetailDTO> getDetail(@Parameter(description = "帖子ID") @PathVariable Integer id) {
        AdminPostDetailDTO dto = adminPostService.getDetail(id);
        return Result.success(dto);
    }

    @PostMapping("/{id}/approve")
    //@SaCheckRole("ADMIN")
    @Operation(summary = "审核通过帖子")
    public Result<Void> approve(
            @Parameter(description = "帖子ID") @PathVariable Integer id,
            @RequestBody(required = false) @Valid PostReviewRequest body) {
        Integer reviewerId = StpUtil.getLoginIdAsInt();
        String reason = body == null ? null : body.getReason();
        adminPostService.approve(reviewerId, id, reason);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    //@SaCheckRole("ADMIN")
    @Operation(summary = "审核拒绝帖子")
    public Result<Void> reject(
            @Parameter(description = "帖子ID") @PathVariable Integer id,
            @RequestBody @Valid PostReviewRequest body) {
        Integer reviewerId = StpUtil.getLoginIdAsInt();
        adminPostService.reject(reviewerId, id, body.getReason());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    //@SaCheckRole("ADMIN")
    @Operation(summary = "删除帖子")
    public Result<Void> delete(@Parameter(description = "帖子ID") @PathVariable Integer id) {
        adminPostService.delete(id);
        return Result.success();
    }
}


