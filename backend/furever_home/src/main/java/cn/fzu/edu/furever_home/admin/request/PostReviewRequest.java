package cn.fzu.edu.furever_home.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "帖子审核请求")
public class PostReviewRequest {

    @Schema(description = "审核备注/原因", example = "内容健康，允许发布")
    private String reason;
}

