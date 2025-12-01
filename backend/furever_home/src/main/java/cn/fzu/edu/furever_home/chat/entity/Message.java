package cn.fzu.edu.furever_home.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(value = "message_id", type = IdType.AUTO)
    private Integer messageId;
    private Integer chatId;
    private Integer senderId;
    private String content;
    private LocalDateTime createTime;
}