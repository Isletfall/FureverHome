package cn.fzu.edu.furever_home.adopt.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.fzu.edu.furever_home.adopt.dto.AdoptDTO;
import cn.fzu.edu.furever_home.adopt.request.ReviewAdoptRequest;
import cn.fzu.edu.furever_home.adopt.request.SubmitAdoptRequest;
import cn.fzu.edu.furever_home.adopt.service.AdoptService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import cn.fzu.edu.furever_home.common.result.Result;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/adopt")
@RequiredArgsConstructor
@Tag(name = "领养申请", description = "提交、查看、审核领养申请")
public class AdoptController {
    private final AdoptService adoptService;

    @PostMapping
    @SaCheckPermission("adopt:apply")
    @Operation(summary = "提交领养申请")
    public Result<Integer> submit(@RequestBody @Valid SubmitAdoptRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        Integer id = adoptService.submit(uid, req);
        return Result.success(id);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("adopt:read")
    @Operation(summary = "获取领养申请详情")
    public Result<AdoptDTO> detail(@Parameter(description = "领养申请ID") @PathVariable Integer id) {
        AdoptDTO dto = adoptService.getById(id);
        return Result.success(dto);
    }

    @PostMapping("/{id}/review")
    @SaCheckPermission("adopt:review")
    @Operation(summary = "审核领养申请")
    public Result<Void> review(@Parameter(description = "领养申请ID") @PathVariable Integer id, @RequestBody @Valid ReviewAdoptRequest req) {
        adoptService.review(id, req.getApplicationStatus());
        return Result.success();
    }
}
