package cn.fzu.edu.furever_home.post.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
@Schema(name = "UpdatePostRequest", description = "更新帖子请求体")
public class UpdatePostRequest {
    @Schema(description = "标题")
    @Size(max = 100)
    private String title;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "图片链接列表（JSON数组）", example = "[\"https://img.example.com/a.jpg\",\"https://img.example.com/b.jpg\"]")
    private List<String> mediaUrls;
}
