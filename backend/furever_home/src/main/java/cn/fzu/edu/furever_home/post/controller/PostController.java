package cn.fzu.edu.furever_home.post.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.fzu.edu.furever_home.post.dto.PostDTO;
import cn.fzu.edu.furever_home.post.request.CreatePostRequest;
import cn.fzu.edu.furever_home.post.request.UpdatePostRequest;
import cn.fzu.edu.furever_home.post.service.PostService;
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
@RequestMapping("/api/post")
@RequiredArgsConstructor
@Tag(name = "帖子管理", description = "帖子列表、详情、发布、更新、删除")
public class PostController {
    private final PostService postService;

    @GetMapping("/list")
    @Operation(summary = "获取帖子列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<PageResult<PostDTO>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<PostDTO> data = postService.pageAll(page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取帖子详情")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<PostDTO> detail(@Parameter(description = "帖子ID") @PathVariable Integer id) {
        PostDTO dto = postService.getById(id);
        return Result.success(dto);
    }

    @PostMapping
    @SaCheckPermission("post:create")
    @Operation(summary = "发布帖子")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<Integer> create(@RequestBody @Valid CreatePostRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        Integer id = postService.create(uid, req);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("post:create")
    @Operation(summary = "更新帖子")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<Void> update(@Parameter(description = "帖子ID") @PathVariable Integer id, @RequestBody @Valid UpdatePostRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        postService.update(uid, id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("post:delete")
    @Operation(summary = "删除帖子")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<Void> delete(@Parameter(description = "帖子ID") @PathVariable Integer id) {
        Integer uid = StpUtil.getLoginIdAsInt();
        postService.delete(uid, id);
        return Result.success();
    }

    @GetMapping("/mine/list")
    @Operation(summary = "获取我的帖子列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<PageResult<PostDTO>> myPosts(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int pageSize) {
        Integer uid = cn.dev33.satoken.stp.StpUtil.getLoginIdAsInt();
        PageResult<PostDTO> data = postService.pageMine(uid, page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/user/{userId}/list")
    @Operation(summary = "获取他人帖子列表")
    public Result<PageResult<cn.fzu.edu.furever_home.post.dto.PostPublicDTO>> userPosts(@PathVariable Integer userId,
                                                                                        @RequestParam(defaultValue = "1") int page,
                                                                                        @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<cn.fzu.edu.furever_home.post.dto.PostPublicDTO> data = postService.pageByUser(userId, page, pageSize);
        return Result.success(data);
    }
}
