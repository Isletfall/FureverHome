package cn.fzu.edu.furever_home.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("recent_activity")
public class RecentActivity {
    @TableId(value = "activity_id", type = IdType.AUTO)
    private Integer activityId;
    private Integer recipientId;
    private Integer actorId;
    private String targetType;
    private Integer targetId;
    private String event;
    private String title;
    private String content;
    private String extraInfo;
    private Boolean isRead;
    private LocalDateTime createTime;
}
