package cn.fzu.edu.furever_home.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "管理后台首页统计数据")
public class DashboardStatisticsDTO {

    @Schema(description = "已发布帖子数量")
    private long totalPostCount;

    @Schema(description = "已发布长期宠物数量")
    private long longTermPetCount;

    @Schema(description = "已发布短期宠物数量")
    private long shortTermPetCount;

    @Schema(description = "待审核的帖子数量")
    private long pendingPostCount;

    @Schema(description = "待审核的宠物数量")
    private long pendingPetCount;

    @Schema(description = "待审核的领养申请数量")
    private long pendingAdoptCount;
}

