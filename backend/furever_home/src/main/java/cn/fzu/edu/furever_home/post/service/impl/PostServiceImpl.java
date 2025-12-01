package cn.fzu.edu.furever_home.post.service.impl;

import cn.fzu.edu.furever_home.post.dto.PostDTO;
import cn.fzu.edu.furever_home.post.entity.Post;
import cn.fzu.edu.furever_home.post.mapper.PostMapper;
import cn.fzu.edu.furever_home.post.request.CreatePostRequest;
import cn.fzu.edu.furever_home.post.request.UpdatePostRequest;
import cn.fzu.edu.furever_home.post.service.PostService;
import cn.fzu.edu.furever_home.review.service.ReviewService;
import cn.fzu.edu.furever_home.common.enums.ReviewTargetType;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;
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
    public PageResult<cn.fzu.edu.furever_home.post.dto.PostPublicDTO> pageByUser(Integer userId, int page, int pageSize) {
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
            d.setMediaUrls(p.getMediaUrls());
            d.setViewCount(p.getViewCount());
            d.setLikeCount(p.getLikeCount());
            d.setCommentCount(p.getCommentCount());
            d.setCreateTime(p.getCreateTime());
            return d;
        }).toList();
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), records);
    }

    @Override
    public PostDTO getById(Integer id) {
        Post p = postMapper.selectById(id);
        if (p == null || p.getReviewStatus() != ReviewStatus.APPROVED) return null;
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
        p.setMediaUrls(req.getMediaUrls());
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
        if (p == null) throw new IllegalStateException("帖子不存在");
        if (!p.getUserId().equals(userId)) throw new IllegalStateException("无权修改该帖子");
        if (req.getTitle() != null) p.setTitle(req.getTitle());
        if (req.getContent() != null) p.setContent(req.getContent());
        if (req.getMediaUrls() != null) p.setMediaUrls(req.getMediaUrls());
        postMapper.updateById(p);
    }

    @Override
    public void delete(Integer userId, Integer id) {
        Post p = postMapper.selectById(id);
        if (p == null) return;
        if (!p.getUserId().equals(userId)) throw new IllegalStateException("无权删除该帖子");
        postMapper.deleteById(id);
    }

    private PostDTO toDTO(Post p) {
        if (p == null) return null;
        PostDTO d = new PostDTO();
        d.setPostId(p.getPostId());
        d.setUserId(p.getUserId());
        d.setTitle(p.getTitle());
        d.setContent(p.getContent());
        d.setReviewStatus(p.getReviewStatus());
        d.setMediaUrls(p.getMediaUrls());
        d.setViewCount(p.getViewCount());
        d.setLikeCount(p.getLikeCount());
        d.setCommentCount(p.getCommentCount());
        d.setCreateTime(p.getCreateTime());
        return d;
    }
}