package cn.fzu.edu.furever_home.adopt.request;

import cn.fzu.edu.furever_home.common.enums.HouseType;
import cn.fzu.edu.furever_home.common.enums.LivingEnvironment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "SubmitAdoptRequest", description = "提交领养申请请求体")
public class SubmitAdoptRequest {
    @NotNull
    @Schema(description = "动物ID")
    private Integer animalId;

    @NotNull
    @Schema(description = "居住环境")
    private LivingEnvironment livingEnvironment;

    @NotNull
    @Schema(description = "房屋产权")
    private HouseType houseType;

    @NotNull
    @Schema(description = "是否有其他宠物")
    private Boolean hasOtherPets;

    @NotNull
    @Min(1)
    @Schema(description = "家庭成员数量")
    private Integer familyMemberCount;

    @NotNull
    @Schema(description = "是否有小孩")
    private Boolean hasChild;

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "领养原因")
    private String adoptReason;

    @NotNull
    @Min(1)
    @Schema(description = "月收入")
    private Integer monthSalary;
}
