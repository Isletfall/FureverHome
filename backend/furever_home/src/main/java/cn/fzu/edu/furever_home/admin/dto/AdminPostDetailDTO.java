package cn.fzu.edu.furever_home.admin.dto;

import cn.fzu.edu.furever_home.common.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "管理端帖子详情")
public class AdminPostDetailDTO {

    @Schema(description = "帖子ID")
    private Integer postId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "正文内容")
    private String content;

    @Schema(description = "多媒体地址列表")
    private List<String> mediaUrls;

    @Schema(description = "发布者ID")
    private Integer authorId;

    @Schema(description = "发布者昵称")
    private String authorName;

    @Schema(description = "发布者邮箱")
    private String authorEmail;

    @Schema(description = "审核状态")
    private ReviewStatus reviewStatus;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "评论列表")
    private List<AdminCommentDTO> comments;
}

