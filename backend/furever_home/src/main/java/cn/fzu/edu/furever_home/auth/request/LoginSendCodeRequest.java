package cn.fzu.edu.furever_home.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "LoginSendCodeRequest", description = "发送登录验证码请求体")
public class LoginSendCodeRequest {
    @Schema(description = "邮箱", example = "user@example.com")
    @NotBlank
    @Email
    private String email;
}
