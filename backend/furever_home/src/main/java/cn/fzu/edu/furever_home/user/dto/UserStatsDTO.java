package cn.fzu.edu.furever_home.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户统计数据")
public class UserStatsDTO {
    @Schema(description = "我的帖子总数")
    private long postCount;

    @Schema(description = "我的短期宠物总数")
    private long shortTermPetCount;

    @Schema(description = "我的长期宠物总数")
    private long longTermPetCount;

    @Schema(description = "我的申请总数")
    private long myApplicationsCount;

    @Schema(description = "我的待办总数")
    private long myTodosCount;

    @Schema(description = "我的评分均值")
    private Double ratingAverage;

    @Schema(description = "我的评分次数")
    private Integer ratingCount;
}
