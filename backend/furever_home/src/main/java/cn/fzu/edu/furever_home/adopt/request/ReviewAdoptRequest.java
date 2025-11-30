package cn.fzu.edu.furever_home.adopt.request;

import cn.fzu.edu.furever_home.common.enums.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "ReviewAdoptRequest", description = "审核领养申请请求体")
public class ReviewAdoptRequest {
    @NotNull
    @Schema(description = "审核状态（申请成功/申请失败）")
    private ApplicationStatus applicationStatus;
}
