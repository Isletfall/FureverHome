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

import java.util.List;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
@Tag(name = "帖子管理", description = "帖子列表、详情、发布、更新、删除")
public class PostController {
    private final PostService postService;

    @GetMapping("/list")
    @Operation(summary = "获取帖子列表")
    public Result<java.util.List<PostDTO>> list() {
        List<PostDTO> list = postService.listAll();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取帖子详情")
    public Result<PostDTO> detail(@Parameter(description = "帖子ID") @PathVariable Integer id) {
        PostDTO dto = postService.getById(id);
        return Result.success(dto);
    }

    @PostMapping
    @SaCheckPermission("post:create")
    @Operation(summary = "发布帖子")
    public Result<Integer> create(@RequestBody @Valid CreatePostRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        Integer id = postService.create(uid, req);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("post:create")
    @Operation(summary = "更新帖子")
    public Result<Void> update(@Parameter(description = "帖子ID") @PathVariable Integer id, @RequestBody @Valid UpdatePostRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        postService.update(uid, id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("post:delete")
    @Operation(summary = "删除帖子")
    public Result<Void> delete(@Parameter(description = "帖子ID") @PathVariable Integer id) {
        Integer uid = StpUtil.getLoginIdAsInt();
        postService.delete(uid, id);
        return Result.success();
    }
}
