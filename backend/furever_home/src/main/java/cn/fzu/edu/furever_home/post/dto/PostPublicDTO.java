package cn.fzu.edu.furever_home.post.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Data
@Schema(name = "帖子公开信息", description = "对外展示帖子信息DTO（不含审核状态）")
public class PostPublicDTO {
    @Schema(description = "帖子ID")
    private Integer postId;
    @Schema(description = "用户ID")
    private Integer userId;
    @Schema(description = "发布者昵称")
    private String userName;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "正文内容")
    private String content;
    @Schema(description = "多媒体地址列表", example = "[\"https://img.example.com/a.jpg\",\"https://img.example.com/b.jpg\"]")
    private List<String> mediaUrls;
    @Schema(description = "浏览次数")
    private Integer viewCount;
    @Schema(description = "点赞次数")
    private Integer likeCount;
    @Schema(description = "评论次数")
    private Integer commentCount;
    @Schema(description = "发帖时间")
    private java.time.LocalDateTime createTime;
}