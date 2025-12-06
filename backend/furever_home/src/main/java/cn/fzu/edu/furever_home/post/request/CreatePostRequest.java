package cn.fzu.edu.furever_home.post.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
@Schema(name = "CreatePostRequest", description = "发布帖子请求体")
public class CreatePostRequest {
    @Schema(description = "标题")
    @NotBlank
    @Size(max = 100)
    private String title;

    @Schema(description = "内容")
    @NotBlank
    private String content;

    @Schema(description = "图片链接列表（JSON数组）", example = "[\"https://img.example.com/a.jpg\",\"https://img.example.com/b.jpg\"]")
    private List<String> mediaUrls;
}
