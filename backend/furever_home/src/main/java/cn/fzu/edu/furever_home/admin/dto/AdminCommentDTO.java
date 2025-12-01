package cn.fzu.edu.furever_home.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理端帖子评论信息")
public class AdminCommentDTO {

    @Schema(description = "评论ID")
    private Integer commentId;

    @Schema(description = "父评论ID")
    private Integer parentCommentId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论者ID")
    private Integer userId;

    @Schema(description = "评论者昵称")
    private String userName;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论时间")
    private LocalDateTime createTime;
}

