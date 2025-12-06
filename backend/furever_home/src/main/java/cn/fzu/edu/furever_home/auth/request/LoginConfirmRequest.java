package cn.fzu.edu.furever_home.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(name = "LoginConfirmRequest", description = "验证码登录请求体")
public class LoginConfirmRequest {
    @Schema(description = "邮箱", example = "user@example.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "验证码", example = "123456")
    @NotBlank
    @Pattern(regexp = "\\d{6}")
    private String code;
}
