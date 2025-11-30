package cn.fzu.edu.furever_home.user.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.fzu.edu.furever_home.common.enums.*;

import java.time.LocalDateTime;

@Data
@Schema(name = "用户信息", description = "用户资料DTO")
public class UserDTO {
    @Schema(description = "用户ID")
    private Integer userId;
    @Schema(description = "昵称")
    private String userName;
    @Schema(description = "年龄")
    private Integer userAge;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "头像图片链接")
    private String avatarUrl;
    @Schema(description = "性别")
    private Sex sex;
    @Schema(description = "所在地")
    private String location;
    @Schema(description = "资质说明")
    private String proofText;
    @Schema(description = "资质图片链接")
    private String proofPhoto;
    @Schema(description = "信用分")
    private Double creditScore;
    @Schema(description = "评分次数")
    private Integer creditScoreCount;
    @Schema(description = "状态")
    private UserStatus status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;
}