package cn.fzu.edu.furever_home.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat")
public class Chat {
    @TableId(value = "chat_id", type = IdType.AUTO)
    private Integer chatId;
    private Integer creatorId;
    private Integer receiverId;
    private LocalDateTime createTime;
    private LocalDateTime lastTime;
}