package cn.fzu.edu.furever_home.post.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "SubmitCommentRequest", description = "提交评论请求体")
public class SubmitCommentRequest {
    @Schema(description = "评论内容")
    @NotBlank
    @Size(max = 1000)
    private String content;

    @Schema(description = "父评论ID，可为空")
    private Integer parentCommentId;
}