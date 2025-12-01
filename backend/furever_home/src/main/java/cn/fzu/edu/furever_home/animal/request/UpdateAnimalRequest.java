package cn.fzu.edu.furever_home.animal.request;

import io.swagger.v3.oas.annotations.media.Schema;
import cn.fzu.edu.furever_home.common.enums.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "UpdateAnimalRequest", description = "更新动物信息请求体")
public class UpdateAnimalRequest {
    @Schema(description = "动物名字")
    @Size(max = 50)
    private String animalName;
    @Schema(description = "动物照片URL列表（JSON数组）", example = "[\"https://img.example.com/a.jpg\",\"https://img.example.com/b.jpg\"]")
    private List<String> photoUrls;
    @Schema(description = "动物种类")
    private Species species;
    @Schema(description = "动物品种")
    @Size(max = 50)
    private String breed;
    @Schema(description = "性别")
    private Gender gender;
    @Schema(description = "月龄")
    @Min(0)
    private Integer animalAge;
    @Schema(description = "健康状态")
    @Size(max = 200)
    private String healthStatus;
    @Schema(description = "是否绝育")
    private SterilizedStatus isSterilized;
    @Schema(description = "领养状态")
    private AdoptionStatus adoptionStatus;
    @Schema(description = "宠物简介")
    @Size(max = 200)
    private String shortDescription;
}