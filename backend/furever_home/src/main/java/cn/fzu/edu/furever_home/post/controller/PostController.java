package cn.fzu.edu.furever_home.post.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.fzu.edu.furever_home.post.dto.PostDTO;
import cn.fzu.edu.furever_home.post.dto.PostPublicDTO;
import cn.fzu.edu.furever_home.post.request.CreatePostRequest;
import cn.fzu.edu.furever_home.post.request.UpdatePostRequest;
import cn.fzu.edu.furever_home.post.service.PostService;
import cn.fzu.edu.furever_home.post.request.SubmitCommentRequest;
import cn.fzu.edu.furever_home.post.dto.PostCommentDTO;
import cn.fzu.edu.furever_home.post.enums.CommentSortBy;
import cn.fzu.edu.furever_home.post.enums.SortOrder;
import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import cn.fzu.edu.furever_home.common.result.Result;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import cn.fzu.edu.furever_home.common.result.PageResult;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
@Tag(name = "帖子管理", description = "帖子列表、详情、发布、更新、删除")
public class PostController {
    private final PostService postService;

    @GetMapping("/list")
    @Operation(summary = "获取帖子列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<PageResult<PostDTO>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<PostDTO> data = postService.pageAll(page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取帖子详情")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<PostDTO> detail(@Parameter(description = "帖子ID") @PathVariable Integer id) {
        PostDTO dto = postService.getById(id);
        return Result.success(dto);
    }

    @PostMapping
    @SaCheckPermission("post:create")
    @Operation(summary = "发布帖子")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<Integer> create(@RequestBody @Valid CreatePostRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        Integer id = postService.create(uid, req);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("post:create")
    @Operation(summary = "更新帖子")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<Void> update(@Parameter(description = "帖子ID") @PathVariable Integer id, @RequestBody @Valid UpdatePostRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        postService.update(uid, id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("post:delete")
    @Operation(summary = "删除帖子")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<Void> delete(@Parameter(description = "帖子ID") @PathVariable Integer id) {
        Integer uid = StpUtil.getLoginIdAsInt();
        postService.delete(uid, id);
        return Result.success();
    }

    @GetMapping("/mine/list")
    @Operation(summary = "获取我的帖子列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<PageResult<PostDTO>> myPosts(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int pageSize) {
        Integer uid = StpUtil.getLoginIdAsInt();
        PageResult<PostDTO> data = postService.pageMine(uid, page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/user/{userId}/list")
    @Operation(summary = "获取他人帖子列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<PageResult<PostPublicDTO>> userPosts(@PathVariable Integer userId,
                                                                                        @RequestParam(defaultValue = "1") int page,
                                                                                        @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<PostPublicDTO> data = postService.pageByUser(userId, page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "获取帖子评论列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<PageResult<PostCommentDTO>> comments(@Parameter(description = "帖子ID") @PathVariable Integer id,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int pageSize,
                                                       @Parameter(description = "排序字段", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CommentSortBy.class))
                                                       @RequestParam(required = false, defaultValue = "TIME") CommentSortBy sortBy,
                                                       @Parameter(description = "排序方向", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = SortOrder.class))
                                                       @RequestParam(required = false, defaultValue = "ASC") SortOrder order) {
        PageResult<PostCommentDTO> data = postService.listComments(id, page, pageSize, sortBy, order);
        return Result.success(data);
    }

    @PostMapping("/comments/{id}/like")
    @SaCheckLogin
    @Operation(summary = "评论点赞/取消点赞")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<java.util.Map<String, Object>> toggleCommentLike(@Parameter(description = "评论ID") @PathVariable Integer id) {
        Integer uid = StpUtil.getLoginIdAsInt();
        boolean liked = postService.toggleCommentLike(uid, id);
        Integer likeCount = postService.getCommentLikeCount(id);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("liked", liked);
        resp.put("likeCount", likeCount);
        return Result.success(resp);
    }


    @PostMapping("/{id}/comments")
    @SaCheckPermission("comment:create")
    @Operation(summary = "提交评论")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<Map<String, Object>> submitComment(@Parameter(description = "帖子ID") @PathVariable Integer id, @RequestBody @Valid SubmitCommentRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        Integer commentId = postService.submitComment(uid, id, req);
        Map<String, Object> resp = new HashMap<>();
        resp.put("commentId", commentId);
        return Result.success(resp);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索帖子")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<PageResult<PostPublicDTO>> search(@RequestParam(required = false) String keyword,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<PostPublicDTO> data = postService.search(keyword, page, pageSize);
        return Result.success(data);
    }

    @PostMapping("/{id}/like")
    @SaCheckLogin
    @Operation(summary = "点赞/取消点赞")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer {{token}}")
    public Result<java.util.Map<String, Object>> toggleLike(@Parameter(description = "帖子ID") @PathVariable Integer id) {
        Integer uid = StpUtil.getLoginIdAsInt();
        boolean liked = postService.toggleLike(uid, id);
        Integer likeCount = postService.getLikeCount(id);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("liked", liked);
        resp.put("likeCount", likeCount);
        return Result.success(resp);
    }
}
