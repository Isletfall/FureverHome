package cn.fzu.edu.furever_home.rating.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Schema(description = "收到的评价条目")
public class ReceivedRatingItemDTO {
    @Schema(description = "评价ID")
    private Integer ratingId;
    @Schema(description = "其他用户ID")
    private Integer otherUserId;
    @Schema(description = "其他用户头像")
    private String otherUserAvatar;
    @Schema(description = "其他用户昵称")
    private String otherUserName;
    @Schema(description = "评分")
    private Integer score;
    @Schema(description = "文字评价")
    private String content;
    @Schema(description = "评价时间")
    private LocalDateTime createTime;
}