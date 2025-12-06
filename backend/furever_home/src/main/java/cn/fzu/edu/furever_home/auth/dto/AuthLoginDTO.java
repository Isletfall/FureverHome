package cn.fzu.edu.furever_home.auth.dto;

import cn.dev33.satoken.stp.SaTokenInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "登录成功响应数据")
public class AuthLoginDTO {
    @Schema(description = "令牌信息")
    private SaTokenInfo tokenInfo;

    @Schema(description = "角色代码列表")
    private List<String> roles;

    @Schema(description = "用户昵称")
    private String userName;
}
