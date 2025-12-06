package cn.fzu.edu.furever_home.adopt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "我的领养申请列表项")
public class MyAdoptItemDTO {
    @Schema(description = "领养申请ID")
    private Integer adoptId;
    @Schema(description = "被申请用户ID（宠物发布者ID）")
    private Integer targetUserId;
    @Schema(description = "被申请用户头像 URL")
    private String targetUserAvatar;
    @Schema(description = "被申请用户名称（宠物发布者昵称）")
    private String targetUserName;
    @Schema(description = "宠物ID")
    private Integer animalId;
    @Schema(description = "宠物名称")
    private String animalName;
    @Schema(description = "宠物封面照片 URL")
    private String animalPhoto;
    @Schema(description = "领养理由")
    private String reason;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "管理员审核状态")
    private String reviewStatus;
    @Schema(description = "用户审核状态")
    private String applicationStatus;
    @Schema(description = "用户撤销状态")
    private String isCanceled;
}
