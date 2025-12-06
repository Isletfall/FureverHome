package cn.fzu.edu.furever_home.adopt.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "UpdateAdoptRequest", description = "修改领养申请请求体")
public class UpdateAdoptRequest {
    @NotBlank
    @Size(max = 50)
    @Schema(description = "申请人姓名")
    private String userName;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "申请人邮箱")
    private String email;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "申请人电话")
    private String phone;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "所在省")
    private String province;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "所在市")
    private String city;

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "居住地址")
    private String livingLocation;

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "领养原因")
    private String adoptReason;
}
