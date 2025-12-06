package cn.fzu.edu.furever_home.post.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(name = "帖子信息", description = "帖子基础信息DTO")
public class PostDTO {
    @Schema(description = "帖子ID")
    private Integer postId;
    @Schema(description = "发布用户ID")
    private Integer userId;
    @Schema(description = "发布用户头像URL")
    private String userAvatar;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "审核状态")
    private ReviewStatus reviewStatus;
    @Schema(description = "图片链接列表", example = "[\"https://img.example.com/a.jpg\",\"https://img.example.com/b.jpg\"]")
    private List<String> mediaUrls;
    @Schema(description = "浏览次数")
    private Integer viewCount;
    @Schema(description = "点赞数")
    private Integer likeCount;
    @Schema(description = "评论数")
    private Integer commentCount;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}