package cn.fzu.edu.furever_home.rating.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rating")
public class Rating {
    @TableId(value = "rating_id", type = IdType.AUTO)
    private Integer ratingId;
    private Integer userId;
    private Integer targetUserId;
    private Integer adoptId;
    private Integer score;
    private String content;
    private LocalDateTime createTime;
}