package cn.fzu.edu.furever_home.adopt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "待办领养申请列表项")
public class AdoptTodoItemDTO {
    @Schema(description = "领养申请ID")
    private Integer adoptId;
    @Schema(description = "申请人ID")
    private Integer applicantId;
    @Schema(description = "申请人头像 URL")
    private String applicantAvatar;
    @Schema(description = "申请人名称")
    private String applicantName;
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
    @Schema(description = "确认状态文案")
    private String applicationStatus;
}
