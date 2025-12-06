package cn.fzu.edu.furever_home.post.service.impl;

import cn.fzu.edu.furever_home.post.dto.PostDTO;
import cn.fzu.edu.furever_home.post.entity.Post;
import cn.fzu.edu.furever_home.post.entity.Comment;
import cn.fzu.edu.furever_home.post.mapper.PostMapper;
import cn.fzu.edu.furever_home.post.mapper.CommentMapper;
import cn.fzu.edu.furever_home.post.request.CreatePostRequest;
import cn.fzu.edu.furever_home.post.request.UpdatePostRequest;
import cn.fzu.edu.furever_home.post.service.PostService;
import cn.fzu.edu.furever_home.review.service.ReviewService;
import cn.fzu.edu.furever_home.common.enums.ReviewTargetType;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;
import cn.fzu.edu.furever_home.auth.mapper.UserMapper;
import cn.fzu.edu.furever_home.auth.entity.User;
import cn.fzu.edu.furever_home.post.mapper.LikeMapper;
import cn.fzu.edu.furever_home.post.entity.Like;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import cn.fzu.edu.furever_home.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostMapper postMapper;
    private final ReviewService reviewService;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final LikeMapper likeMapper;

    @Override
    public PageResult<PostDTO> pageAll(int page, int pageSize) {
        Page<Post> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getReviewStatus, ReviewStatus.APPROVED)
                .orderByDesc(Post::getCreateTime);
        Page<Post> result = postMapper.selectPage(mpPage, wrapper);
        java.util.List<PostDTO> records = result.getRecords().stream().map(this::toDTO).toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public PageResult<PostDTO> pageMine(Integer userId, int page, int pageSize) {
        Page<Post> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .orderByDesc(Post::getCreateTime);
        Page<Post> result = postMapper.selectPage(mpPage, wrapper);
        java.util.List<PostDTO> records = result.getRecords().stream().map(this::toDTO).toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public PageResult<cn.fzu.edu.furever_home.post.dto.PostPublicDTO> pageByUser(Integer userId, int page,
            int pageSize) {
        Page<Post> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(Post::getReviewStatus, ReviewStatus.APPROVED)
                .orderByDesc(Post::getCreateTime);
        Page<Post> result = postMapper.selectPage(mpPage, wrapper);
        java.util.List<cn.fzu.edu.furever_home.post.dto.PostPublicDTO> records = result.getRecords().stream().map(p -> {
            cn.fzu.edu.furever_home.post.dto.PostPublicDTO d = new cn.fzu.edu.furever_home.post.dto.PostPublicDTO();
            d.setPostId(p.getPostId());
            d.setUserId(p.getUserId());
            d.setTitle(p.getTitle());
            d.setContent(p.getContent());
            d.setMediaUrls(parseMediaUrls(p.getMediaUrls()));
            d.setViewCount(p.getViewCount());
            d.setLikeCount(p.getLikeCount());
            d.setCommentCount(p.getCommentCount());
            d.setCreateTime(p.getCreateTime());
            User u = p.getUserId() == null ? null : userMapper.selectById(p.getUserId());
            d.setUserName(u == null ? null : u.getUserName());
            return d;
        }).toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public PostDTO getById(Integer id) {
        Post p = postMapper.selectById(id);
        if (p == null || p.getReviewStatus() != ReviewStatus.APPROVED)
            return null;
        p.setViewCount((p.getViewCount() == null ? 0 : p.getViewCount()) + 1);
        postMapper.updateById(p);
        return toDTO(p);
    }

    @Override
    public Integer create(Integer userId, CreatePostRequest req) {
        Post p = new Post();
        p.setUserId(userId);
        p.setTitle(req.getTitle());
        p.setContent(req.getContent());
        p.setMediaUrls(serializeMediaUrls(req.getMediaUrls()));
        p.setViewCount(0);
        p.setLikeCount(0);
        p.setCommentCount(0);
        p.setCreateTime(LocalDateTime.now());
        postMapper.insert(p);
        reviewService.createPending(ReviewTargetType.POST, p.getPostId());
        return p.getPostId();
    }

    @Override
    public void update(Integer userId, Integer id, UpdatePostRequest req) {
        Post p = postMapper.selectById(id);
        if (p == null)
            throw new IllegalStateException("帖子不存在");
        if (!p.getUserId().equals(userId))
            throw new IllegalStateException("无权修改该帖子");
        if (req.getTitle() != null)
            p.setTitle(req.getTitle());
        if (req.getContent() != null)
            p.setContent(req.getContent());
        if (req.getMediaUrls() != null)
            p.setMediaUrls(serializeMediaUrls(req.getMediaUrls()));
        p.setReviewStatus(cn.fzu.edu.furever_home.common.enums.ReviewStatus.PENDING);
        postMapper.updateById(p);
        reviewService.createPending(cn.fzu.edu.furever_home.common.enums.ReviewTargetType.POST, p.getPostId());
    }

    @Override
    public void delete(Integer userId, Integer id) {
        Post p = postMapper.selectById(id);
        if (p == null)
            return;
        if (!p.getUserId().equals(userId))
            throw new IllegalStateException("无权删除该帖子");
        postMapper.deleteById(id);
    }

    @Override
    public cn.fzu.edu.furever_home.common.result.PageResult<cn.fzu.edu.furever_home.post.dto.PostCommentDTO> listComments(
            Integer postId, int page, int pageSize, cn.fzu.edu.furever_home.post.enums.CommentSortBy sortBy,
            cn.fzu.edu.furever_home.post.enums.SortOrder order) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Comment> mpPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                page, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId);
        boolean asc = order == null || order == cn.fzu.edu.furever_home.post.enums.SortOrder.ASC;
        if (sortBy == cn.fzu.edu.furever_home.post.enums.CommentSortBy.LIKES) {
            wrapper.orderBy(true, asc, Comment::getLikeCount).orderBy(true, asc, Comment::getCreateTime);
        } else {
            wrapper.orderBy(true, asc, Comment::getCreateTime).orderBy(true, asc, Comment::getLikeCount);
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Comment> result = commentMapper.selectPage(mpPage,
                wrapper);
        java.util.List<cn.fzu.edu.furever_home.post.dto.PostCommentDTO> records = result.getRecords().stream()
                .map(c -> {
                    cn.fzu.edu.furever_home.post.dto.PostCommentDTO dto = new cn.fzu.edu.furever_home.post.dto.PostCommentDTO();
                    dto.setCommentId(c.getCommentId());
                    dto.setParentCommentId(c.getParentCommentId());
                    dto.setContent(c.getContent());
                    dto.setLikeCount(c.getLikeCount());
                    dto.setCreateTime(c.getCreateTime());
                    dto.setUserId(c.getUserId());
                    User u = c.getUserId() == null ? null : userMapper.selectById(c.getUserId());
                    dto.setUserName(u == null ? null : u.getUserName());
                    return dto;
                }).toList();
        return new cn.fzu.edu.furever_home.common.result.PageResult<>(result.getCurrent(), result.getSize(),
                result.getTotal(), records);
    }

    @Override
    public boolean toggleCommentLike(Integer userId, Integer commentId) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null)
            throw new IllegalStateException("评论不存在");
        Like existing = likeMapper
                .selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Like>()
                        .eq(Like::getUserId, userId)
                        .eq(Like::getKind, "评论")
                        .eq(Like::getTargetId, commentId)
                        .last("limit 1"));
        boolean liked;
        if (existing != null) {
            likeMapper.deleteById(existing.getLikeId());
            int cnt = (c.getLikeCount() == null ? 0 : c.getLikeCount()) - 1;
            c.setLikeCount(Math.max(cnt, 0));
            liked = false;
        } else {
            Like l = new Like();
            l.setUserId(userId);
            l.setKind("评论");
            l.setTargetId(commentId);
            l.setCreateTime(java.time.LocalDateTime.now());
            likeMapper.insert(l);
            c.setLikeCount((c.getLikeCount() == null ? 0 : c.getLikeCount()) + 1);
            liked = true;
        }
        commentMapper.updateById(c);
        return liked;
    }

    @Override
    public Integer getCommentLikeCount(Integer commentId) {
        Comment c = commentMapper.selectById(commentId);
        return c == null ? null : c.getLikeCount();
    }

    @Override
    public Integer submitComment(Integer userId, Integer postId,
            cn.fzu.edu.furever_home.post.request.SubmitCommentRequest req) {
        Post p = postMapper.selectById(postId);
        if (p == null || p.getReviewStatus() != ReviewStatus.APPROVED)
            throw new IllegalStateException("帖子不存在或未发布");
        Comment c = new Comment();
        c.setPostId(postId);
        c.setUserId(userId);
        c.setParentCommentId(req.getParentCommentId());
        c.setContent(req.getContent());
        c.setLikeCount(0);
        c.setCreateTime(java.time.LocalDateTime.now());
        commentMapper.insert(c);
        p.setCommentCount((p.getCommentCount() == null ? 0 : p.getCommentCount()) + 1);
        postMapper.updateById(p);
        return c.getCommentId();
    }

    @Override
    public PageResult<cn.fzu.edu.furever_home.post.dto.PostPublicDTO> search(String keyword, int page, int pageSize) {
        Page<Post> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getReviewStatus, ReviewStatus.APPROVED);
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim();
            wrapper.and(w -> w.like(Post::getTitle, k).or().like(Post::getContent, k));
        }
        wrapper.orderByDesc(Post::getCreateTime);
        Page<Post> result = postMapper.selectPage(mpPage, wrapper);
        java.util.List<cn.fzu.edu.furever_home.post.dto.PostPublicDTO> records = result.getRecords().stream().map(p -> {
            cn.fzu.edu.furever_home.post.dto.PostPublicDTO d = new cn.fzu.edu.furever_home.post.dto.PostPublicDTO();
            d.setPostId(p.getPostId());
            d.setUserId(p.getUserId());
            d.setTitle(p.getTitle());
            d.setContent(p.getContent());
            d.setMediaUrls(parseMediaUrls(p.getMediaUrls()));
            d.setViewCount(p.getViewCount());
            d.setLikeCount(p.getLikeCount());
            d.setCommentCount(p.getCommentCount());
            d.setCreateTime(p.getCreateTime());
            User u = p.getUserId() == null ? null : userMapper.selectById(p.getUserId());
            d.setUserName(u == null ? null : u.getUserName());
            return d;
        }).toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public boolean toggleLike(Integer userId, Integer postId) {
        Post p = postMapper.selectById(postId);
        if (p == null || p.getReviewStatus() != ReviewStatus.APPROVED)
            throw new IllegalStateException("帖子不存在或未发布");
        Like existing = likeMapper
                .selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Like>()
                        .eq(Like::getUserId, userId)
                        .eq(Like::getKind, "帖子")
                        .eq(Like::getTargetId, postId)
                        .last("limit 1"));
        boolean liked;
        if (existing != null) {
            likeMapper.deleteById(existing.getLikeId());
            int cnt = (p.getLikeCount() == null ? 0 : p.getLikeCount()) - 1;
            p.setLikeCount(Math.max(cnt, 0));
            liked = false;
        } else {
            Like l = new Like();
            l.setUserId(userId);
            l.setKind("帖子");
            l.setTargetId(postId);
            l.setCreateTime(java.time.LocalDateTime.now());
            likeMapper.insert(l);
            p.setLikeCount((p.getLikeCount() == null ? 0 : p.getLikeCount()) + 1);
            liked = true;
        }
        postMapper.updateById(p);
        return liked;
    }

    @Override
    public Integer getLikeCount(Integer postId) {
        Post p = postMapper.selectById(postId);
        return p == null ? null : p.getLikeCount();
    }

    private PostDTO toDTO(Post p) {
        if (p == null)
            return null;
        PostDTO d = new PostDTO();
        d.setPostId(p.getPostId());
        d.setUserId(p.getUserId());
        User u = p.getUserId() == null ? null : userMapper.selectById(p.getUserId());
        d.setUserAvatar(u == null ? null : u.getAvatarUrl());
        d.setTitle(p.getTitle());
        d.setContent(p.getContent());
        d.setReviewStatus(p.getReviewStatus());
        d.setMediaUrls(parseMediaUrls(p.getMediaUrls()));
        d.setViewCount(p.getViewCount());
        d.setLikeCount(p.getLikeCount());
        d.setCommentCount(p.getCommentCount());
        d.setCreateTime(p.getCreateTime());
        return d;
    }

    private java.util.List<String> parseMediaUrls(String mediaUrlsJson) {
        if (mediaUrlsJson == null || mediaUrlsJson.isBlank()) {
            return java.util.Collections.emptyList();
        }
        String s = mediaUrlsJson.trim();
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1);
        }
        if (s.isBlank()) {
            return java.util.Collections.emptyList();
        }
        return java.util.Arrays.stream(s.split(","))
                .map(String::trim)
                .map(v -> v.replaceAll("^\"|\"$", ""))
                .filter(v -> !v.isBlank())
                .collect(java.util.stream.Collectors.toList());
    }

    private String serializeMediaUrls(java.util.List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return "[]";
        }
        return "[" + urls.stream()
                .filter(u -> u != null && !u.isBlank())
                .map(u -> "\"" + u.replace("\"", "\\\"") + "\"")
                .collect(java.util.stream.Collectors.joining(",")) + "]";
    }
}
