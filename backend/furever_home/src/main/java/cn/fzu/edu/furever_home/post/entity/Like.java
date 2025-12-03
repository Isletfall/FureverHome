package cn.fzu.edu.furever_home.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("likes")
public class Like {
    @TableId(value = "like_id", type = IdType.AUTO)
    private Integer likeId;
    private Integer userId;
    private String kind;
    private Integer targetId;
    private LocalDateTime createTime;
}