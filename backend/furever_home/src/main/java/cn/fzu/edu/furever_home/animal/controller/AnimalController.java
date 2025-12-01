package cn.fzu.edu.furever_home.animal.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.fzu.edu.furever_home.animal.dto.AnimalDTO;
import cn.fzu.edu.furever_home.animal.request.CreateAnimalRequest;
import cn.fzu.edu.furever_home.animal.request.UpdateAnimalRequest;
import cn.fzu.edu.furever_home.animal.service.AnimalService;
import cn.fzu.edu.furever_home.common.enums.AdoptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import cn.fzu.edu.furever_home.common.result.Result;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import cn.fzu.edu.furever_home.common.result.PageResult;

@RestController
@RequestMapping("/api/animal")
@RequiredArgsConstructor
@Tag(name = "动物管理", description = "动物信息的查询与发布更新删除")
public class AnimalController {
    private final AnimalService animalService;

    @GetMapping("/list")
    @Operation(summary = "获取动物列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<PageResult<AnimalDTO>> list(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<AnimalDTO> data = animalService.pageAll(page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取动物详情")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<AnimalDTO> detail(@Parameter(description = "动物ID") @PathVariable Integer id) {
        Integer uid = StpUtil.getLoginIdAsInt();
        AnimalDTO dto = animalService.getById(id, uid);
        return Result.success(dto);
    }

    @PostMapping
    @SaCheckPermission("animal:create")
    @Operation(summary = "发布动物信息")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<Integer> create(@RequestBody @Valid CreateAnimalRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        Integer id = animalService.create(uid, req);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("animal:create")
    @Operation(summary = "更新动物信息")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<Void> update(@Parameter(description = "动物ID") @PathVariable Integer id, @RequestBody @Valid UpdateAnimalRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        animalService.update(uid, id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("animal:create")
    @Operation(summary = "删除动物信息")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<Void> delete(@Parameter(description = "动物ID") @PathVariable Integer id) {
        Integer uid = StpUtil.getLoginIdAsInt();
        animalService.delete(uid, id);
        return Result.success();
    }

    @GetMapping("/mine/short")
    @Operation(summary = "获取我的短期宠物列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<PageResult<AnimalDTO>> myShort(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int pageSize) {
        Integer uid = cn.dev33.satoken.stp.StpUtil.getLoginIdAsInt();
        PageResult<AnimalDTO> data = animalService.pageMineByAdoption(uid, AdoptionStatus.SHORT_TERM, page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/mine/long")
    @Operation(summary = "获取我的长期宠物列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<PageResult<AnimalDTO>> myLong(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int pageSize) {
        Integer uid = cn.dev33.satoken.stp.StpUtil.getLoginIdAsInt();
        PageResult<AnimalDTO> data = animalService.pageMineByAdoption(uid, AdoptionStatus.LONG_TERM, page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/user/{userId}/short")
    @Operation(summary = "获取他人短期宠物列表")
    public Result<PageResult<cn.fzu.edu.furever_home.animal.dto.AnimalPublicDTO>> userShort(@PathVariable Integer userId,
                                                                                            @RequestParam(defaultValue = "1") int page,
                                                                                            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<cn.fzu.edu.furever_home.animal.dto.AnimalPublicDTO> data = animalService.pageUserByAdoption(userId, AdoptionStatus.SHORT_TERM, page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/user/{userId}/long")
    @Operation(summary = "获取他人长期宠物列表")
    public Result<PageResult<cn.fzu.edu.furever_home.animal.dto.AnimalPublicDTO>> userLong(@PathVariable Integer userId,
                                                                                           @RequestParam(defaultValue = "1") int page,
                                                                                           @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<cn.fzu.edu.furever_home.animal.dto.AnimalPublicDTO> data = animalService.pageUserByAdoption(userId, AdoptionStatus.LONG_TERM, page, pageSize);
        return Result.success(data);
    }
}