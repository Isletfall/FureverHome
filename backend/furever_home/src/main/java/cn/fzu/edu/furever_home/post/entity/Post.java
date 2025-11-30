package cn.fzu.edu.furever_home.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(value = "post_id", type = IdType.AUTO)
    private Integer postId;
    private Integer userId;
    private String title;
    private String content;
    private ReviewStatus reviewStatus;
    private String mediaUrls;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createTime;
}