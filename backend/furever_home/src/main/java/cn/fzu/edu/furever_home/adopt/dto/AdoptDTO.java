package cn.fzu.edu.furever_home.adopt.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.fzu.edu.furever_home.common.enums.*;

import java.time.LocalDateTime;

@Data
@Schema(name = "领养申请信息", description = "领养申请DTO")
public class AdoptDTO {
    @Schema(description = "申请ID")
    private Integer adoptId;
    @Schema(description = "动物ID")
    private Integer animalId;
    @Schema(description = "用户ID")
    private Integer userId;
    @Schema(description = "申请状态")
    private ApplicationStatus applicationStatus;
    @Schema(description = "申请人昵称")
    private String userName;
    @Schema(description = "申请人电话")
    private String phone;
    @Schema(description = "申请人邮箱")
    private String email;
    @Schema(description = "所在省")
    private String province;
    @Schema(description = "所在市")
    private String city;
    @Schema(description = "居住地址")
    private String livingLocation;
    @Schema(description = "领养理由")
    private String adoptReason;
    @Schema(description = "申请时间")
    private LocalDateTime createTime;
    @Schema(description = "审核通过时间")
    private LocalDateTime passTime;
    @Schema(description = "审核状态")
    private ReviewStatus reviewStatus;
}