package cn.fzu.edu.furever_home.animal.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.fzu.edu.furever_home.common.enums.*;
import java.util.List;

@Data
@Schema(name = "动物公开信息", description = "对外展示动物信息DTO（不含审核状态）")
public class AnimalPublicDTO {
    @Schema(description = "动物ID")
    private Integer animalId;
    @Schema(description = "发布用户ID")
    private Integer userId;
    @Schema(description = "发布用户头像URL")
    private String userAvatar;
    @Schema(description = "动物名称")
    private String animalName;
    @Schema(description = "照片URL列表（JSON数组）")
    private List<String> photoUrls;
    @Schema(description = "动物种类")
    private Species species;
    @Schema(description = "品种")
    private String breed;
    @Schema(description = "性别")
    private Gender gender;
    @Schema(description = "年龄")
    private Integer animalAge;
    @Schema(description = "是否绝育")
    private SterilizedStatus isSterilized;
    @Schema(description = "领养状态")
    private AdoptionStatus adoptionStatus;
    @Schema(description = "简短描述")
    private String shortDescription;
    @Schema(description = "领养者名字")
    private String adopterName;
    @Schema(description = "暂时领养时间（天）")
    private Integer adoptionDays;
}
