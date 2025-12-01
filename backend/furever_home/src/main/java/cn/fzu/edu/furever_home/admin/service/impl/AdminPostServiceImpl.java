package cn.fzu.edu.furever_home.admin.service.impl;

import cn.fzu.edu.furever_home.admin.dto.AdminCommentDTO;
import cn.fzu.edu.furever_home.admin.dto.AdminPostDetailDTO;
import cn.fzu.edu.furever_home.admin.dto.AdminPostSummaryDTO;
import cn.fzu.edu.furever_home.admin.service.AdminPostService;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;
import cn.fzu.edu.furever_home.common.enums.ReviewTargetType;
import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.post.entity.Comment;
import cn.fzu.edu.furever_home.post.entity.Post;
import cn.fzu.edu.furever_home.post.mapper.CommentMapper;
import cn.fzu.edu.furever_home.post.mapper.PostMapper;
import cn.fzu.edu.furever_home.review.entity.Review;
import cn.fzu.edu.furever_home.review.mapper.ReviewMapper;
import cn.fzu.edu.furever_home.auth.entity.User;
import cn.fzu.edu.furever_home.auth.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPostServiceImpl implements AdminPostService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<AdminPostSummaryDTO> listPending(int page, int pageSize, String keyword) {
        return pagePostsByStatus(ReviewStatus.PENDING, page, pageSize, keyword);
    }

    @Override
    public PageResult<AdminPostSummaryDTO> listPublished(int page, int pageSize, String keyword) {
        return pagePostsByStatus(ReviewStatus.APPROVED, page, pageSize, keyword);
    }

    @Override
    public AdminPostDetailDTO getDetail(Integer postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return null;
        }
        AdminPostDetailDTO dto = new AdminPostDetailDTO();
        dto.setPostId(post.getPostId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setMediaUrls(parseMediaUrls(post.getMediaUrls()));
        dto.setReviewStatus(post.getReviewStatus());
        dto.setViewCount(safeInt(post.getViewCount()));
        dto.setLikeCount(safeInt(post.getLikeCount()));
        dto.setCommentCount(safeInt(post.getCommentCount()));
        dto.setCreateTime(post.getCreateTime());

        if (post.getUserId() != null) {
            User u = userMapper.selectById(post.getUserId());
            if (u != null) {
                dto.setAuthorId(u.getUserId());
                dto.setAuthorName(u.getUserName());
                dto.setAuthorEmail(u.getEmail());
            }
        }

        // 加载评论列表
        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getPostId, postId)
                        .orderByAsc(Comment::getCreateTime));
        List<AdminCommentDTO> commentDTOList = comments.stream()
                .map(this::toCommentDTO)
                .collect(Collectors.toList());
        dto.setComments(commentDTOList);

        return dto;
    }

    @Override
    public void approve(Integer reviewerId, Integer postId, String reason) {
        updateReviewAndPostStatus(reviewerId, postId, reason, ReviewStatus.APPROVED);
    }

    @Override
    public void reject(Integer reviewerId, Integer postId, String reason) {
        updateReviewAndPostStatus(reviewerId, postId, reason, ReviewStatus.REJECTED);
    }

    @Override
    public void delete(Integer postId) {
        if (postId == null) return;
        postMapper.deleteById(postId);
    }

    // ==================== 内部辅助方法 ====================

    private PageResult<AdminPostSummaryDTO> pagePostsByStatus(ReviewStatus status, int page, int pageSize, String keyword) {
        Page<Post> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getReviewStatus, status)
                .orderByDesc(Post::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Post::getTitle, keyword.trim());
        }
        Page<Post> resultPage = postMapper.selectPage(mpPage, wrapper);
        List<AdminPostSummaryDTO> records = resultPage.getRecords().stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
        return new PageResult<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal(), records);
    }

    private AdminPostSummaryDTO toSummaryDTO(Post p) {
        AdminPostSummaryDTO dto = new AdminPostSummaryDTO();
        dto.setPostId(p.getPostId());
        dto.setTitle(p.getTitle());
        dto.setExcerpt(buildExcerpt(p.getContent()));
        dto.setAuthorId(p.getUserId());
        if (p.getUserId() != null) {
            User u = userMapper.selectById(p.getUserId());
            if (u != null) {
                dto.setAuthorName(u.getUserName());
            }
        }
        dto.setReviewStatus(p.getReviewStatus());
        dto.setViewCount(safeInt(p.getViewCount()));
        dto.setLikeCount(safeInt(p.getLikeCount()));
        dto.setCommentCount(safeInt(p.getCommentCount()));
        dto.setCreateTime(p.getCreateTime());
        return dto;
    }

    private AdminCommentDTO toCommentDTO(Comment c) {
        AdminCommentDTO dto = new AdminCommentDTO();
        dto.setCommentId(c.getCommentId());
        dto.setParentCommentId(c.getParentCommentId());
        dto.setContent(c.getContent());
        dto.setLikeCount(safeInt(c.getLikeCount()));
        dto.setCreateTime(c.getCreateTime());
        dto.setUserId(c.getUserId());
        if (c.getUserId() != null) {
            User u = userMapper.selectById(c.getUserId());
            if (u != null) {
                dto.setUserName(u.getUserName());
            }
        }
        return dto;
    }

    private void updateReviewAndPostStatus(Integer reviewerId, Integer postId, String reason, ReviewStatus status) {
        if (postId == null) {
            throw new IllegalArgumentException("postId 不能为空");
        }
        // 1. 更新 review 表记录
        Review review = reviewMapper.selectOne(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getTargetType, ReviewTargetType.POST)
                        .eq(Review::getTargetId, postId)
                        .orderByDesc(Review::getCreateTime)
                        .last("limit 1"));
        if (review != null) {
            review.setStatus(status);
            review.setReviewerId(reviewerId);
            if (reason != null && !reason.isBlank()) {
                review.setReason(reason.trim());
            }
            reviewMapper.updateById(review);
        }

        // 2. 更新帖子本身的审核状态
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setReviewStatus(status);
            postMapper.updateById(post);
        }
    }

    private int safeInt(Integer v) {
        return v == null ? 0 : v;
    }

    private String buildExcerpt(String content) {
        if (content == null) {
            return "";
        }
        String trimmed = content.trim();
        if (trimmed.length() <= 80) {
            return trimmed;
        }
        return trimmed.substring(0, 80) + "...";
    }

    private List<String> parseMediaUrls(String mediaUrlsJson) {
        if (mediaUrlsJson == null || mediaUrlsJson.isBlank()) {
            return Collections.emptyList();
        }
        // 简单处理：如果是 JSON 数组字符串，去掉首尾 [ ] 后按逗号拆分再去引号
        String s = mediaUrlsJson.trim();
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1);
        }
        if (s.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .map(v -> v.replaceAll("^\"|\"$", ""))
                .filter(v -> !v.isBlank())
                .collect(Collectors.toList());
    }
}


