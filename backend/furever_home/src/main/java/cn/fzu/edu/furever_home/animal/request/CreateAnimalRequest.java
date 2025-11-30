package cn.fzu.edu.furever_home.animal.request;

import cn.fzu.edu.furever_home.common.enums.AdoptionStatus;
import cn.fzu.edu.furever_home.common.enums.Gender;
import cn.fzu.edu.furever_home.common.enums.Species;
import cn.fzu.edu.furever_home.common.enums.SterilizedStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "CreateAnimalRequest", description = "发布动物信息请求体")
public class CreateAnimalRequest {
    @Schema(description = "动物名字")
    @NotBlank
    @Size(max = 50)
    private String animalName;

    @Schema(description = "动物照片（图片链接）")
    private String photoUrls;

    @Schema(description = "动物种类")
    @NotNull
    private Species species;

    @Schema(description = "动物品种")
    @Size(max = 50)
    private String breed;

    @Schema(description = "性别")
    @NotNull
    private Gender gender;

    @Schema(description = "月龄")
    @Min(0)
    private Integer animalAge;

    @Schema(description = "健康状态")
    @NotBlank
    @Size(max = 200)
    private String healthStatus;

    @Schema(description = "是否绝育")
    @NotNull
    private SterilizedStatus isSterilized;

    @Schema(description = "领养状态")
    @NotNull
    private AdoptionStatus adoptionStatus;

    @Schema(description = "宠物简介")
    @Size(max = 200)
    private String shortDescription;
}