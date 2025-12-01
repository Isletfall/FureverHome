package cn.fzu.edu.furever_home.adopt.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.fzu.edu.furever_home.adopt.dto.AdoptDTO;
import cn.fzu.edu.furever_home.adopt.entity.Adopt;
import cn.fzu.edu.furever_home.adopt.request.ReviewAdoptRequest;
import cn.fzu.edu.furever_home.adopt.request.SubmitAdoptRequest;
import cn.fzu.edu.furever_home.adopt.service.AdoptService;
import cn.fzu.edu.furever_home.animal.entity.Animal;
import cn.fzu.edu.furever_home.animal.mapper.AnimalMapper;
import cn.fzu.edu.furever_home.auth.entity.User;
import cn.fzu.edu.furever_home.auth.mapper.UserMapper;
import cn.fzu.edu.furever_home.common.enums.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import cn.fzu.edu.furever_home.common.result.Result;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/adopt")
@RequiredArgsConstructor
@Tag(name = "领养申请", description = "提交、查看、审核领养申请")
public class AdoptController {
    private final AdoptService adoptService;
    private final UserMapper userMapper;
    private final AnimalMapper animalMapper;

    @PostMapping
    @SaCheckPermission("adopt:apply")
    @Operation(summary = "提交领养申请")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<Integer> submit(@RequestBody @Valid SubmitAdoptRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        Integer id = adoptService.submit(uid, req);
        return Result.success(id);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("adopt:read")
    @Operation(summary = "获取领养申请详情")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<AdoptDTO> detail(@Parameter(description = "领养申请ID") @PathVariable Integer id) {
        AdoptDTO dto = adoptService.getById(id);
        return Result.success(dto);
    }

    @PostMapping("/{id}/review")
    @SaCheckPermission("adopt:review")
    @Operation(summary = "审核领养申请")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<Void> review(@Parameter(description = "领养申请ID") @PathVariable Integer id, @RequestBody @Valid ReviewAdoptRequest req) {
        adoptService.review(id, req.getApplicationStatus());
        return Result.success();
    }

    @GetMapping("/todo")
    @SaCheckPermission("adopt:read")
    @Operation(summary = "获取我的待办（别人对我的申请）列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<java.util.List<java.util.Map<String, Object>>> myTodo() {
        Integer uid = StpUtil.getLoginIdAsInt();
        java.util.List<cn.fzu.edu.furever_home.adopt.entity.Adopt> adopts = adoptService.listByAnimalOwner(uid);
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        for (cn.fzu.edu.furever_home.adopt.entity.Adopt a : adopts) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("adoptId", a.getAdoptId());
            User u = userMapper.selectById(a.getUserId());
            m.put("applicantId", u.getUserId());
            m.put("applicantAvatar", u.getAvatarUrl());
            m.put("applicantName", u.getUserName());
            Animal animal = animalMapper.selectById(a.getAnimalId());
            m.put("animalId", animal.getAnimalId());
            m.put("animalName", animal.getAnimalName());
            java.util.List<String> photos = animal.getPhotoUrls();
            m.put("animalPhoto", photos == null || photos.isEmpty() ? null : photos.get(0));
            m.put("reason", a.getAdoptReason());
            m.put("createTime", a.getCreateTime());
            list.add(m);
        }
        return Result.success(list);
    }

    @GetMapping("/mine")
    @SaCheckPermission("adopt:read")
    @Operation(summary = "获取我的申请列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<List<Map<String, Object>>> myApplications() {
        Integer uid = StpUtil.getLoginIdAsInt();
        List<Adopt> adopts = adoptService.listByApplicant(uid);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Adopt a : adopts) {
            Map<String, Object> m = new HashMap<>();
            m.put("adoptId", a.getAdoptId());
            Animal animal = animalMapper.selectById(a.getAnimalId());
            User owner = userMapper.selectById(animal.getUserId());
            m.put("targetUserId", owner.getUserId());
            m.put("targetUserAvatar", owner.getAvatarUrl());
            m.put("targetUserName", owner.getUserName());
            m.put("animalId", animal.getAnimalId());
            m.put("animalName", animal.getAnimalName());
            java.util.List<String> photos = animal.getPhotoUrls();
            m.put("animalPhoto", photos == null || photos.isEmpty() ? null : photos.get(0));
            m.put("reason", a.getAdoptReason());
            m.put("createTime", a.getCreateTime());
            m.put("reviewStatus", a.getReviewStatus());
            m.put("confirmStatus", mapConfirmStatus(a.getApplicationStatus()));
            list.add(m);
        }
        return Result.success(list);
    }

    private String mapConfirmStatus(ApplicationStatus st) {
        if (st == null) return "待确认";
        return switch (st) {
            case APPLYING -> "待确认";
            case SUCCESS -> "已同意";
            case FAIL -> "已拒绝";
        };
    }
}
