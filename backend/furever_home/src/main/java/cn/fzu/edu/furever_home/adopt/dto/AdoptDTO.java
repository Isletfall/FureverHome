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
    @Schema(description = "居住环境")
    private LivingEnvironment livingEnvironment;
    @Schema(description = "房屋类型")
    private HouseType houseType;
    @Schema(description = "是否有其他宠物")
    private Boolean hasOtherPets;
    @Schema(description = "家庭成员数量")
    private Integer familyMemberCount;
    @Schema(description = "是否有儿童")
    private Boolean hasChild;
    @Schema(description = "领养理由")
    private String adoptReason;
    @Schema(description = "月收入")
    private Integer monthSalary;
    @Schema(description = "申请时间")
    private LocalDateTime createTime;
    @Schema(description = "审核通过时间")
    private LocalDateTime passTime;
    @Schema(description = "审核状态")
    private ReviewStatus reviewStatus;
}