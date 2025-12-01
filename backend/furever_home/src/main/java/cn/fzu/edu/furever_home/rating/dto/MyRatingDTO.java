package cn.fzu.edu.furever_home.rating.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "我对某用户的评价")
public class MyRatingDTO {
    @Schema(description = "评价ID")
    private Integer ratingId;
    @Schema(description = "评分")
    private Integer score;
    @Schema(description = "文字评价")
    private String content;
}