package cn.fzu.edu.furever_home.review.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import cn.fzu.edu.furever_home.common.enums.ReviewTargetType;
import cn.fzu.edu.furever_home.common.enums.ReviewStatus;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(value = "review_id", type = IdType.AUTO)
    private Integer reviewId;
    private ReviewTargetType targetType;
    private Integer targetId;
    private ReviewStatus status;
    private Integer reviewerId;
    private String reason;
    private String extraInfo;
    private LocalDateTime createTime;
    private LocalDateTime updatedAt;
}