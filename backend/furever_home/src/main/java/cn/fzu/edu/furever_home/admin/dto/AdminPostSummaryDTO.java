package cn.fzu.edu.furever_home.admin.dto;

import cn.fzu.edu.furever_home.common.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理端帖子列表项")
public class AdminPostSummaryDTO {

    @Schema(description = "帖子ID")
    private Integer postId;

    @Schema(description = "帖子标题")
    private String title;

    @Schema(description = "内容摘要")
    private String excerpt;

    @Schema(description = "作者ID")
    private Integer authorId;

    @Schema(description = "作者昵称")
    private String authorName;

    @Schema(description = "审核状态")
    private ReviewStatus reviewStatus;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "发布时间")
    private LocalDateTime createTime;
}

