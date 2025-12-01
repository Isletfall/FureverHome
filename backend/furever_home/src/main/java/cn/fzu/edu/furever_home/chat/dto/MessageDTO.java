package cn.fzu.edu.furever_home.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "MessageDTO", description = "消息项")
public class MessageDTO {
    @Schema(description = "消息ID")
    private Integer messageId;
    @Schema(description = "会话ID")
    private Integer conversationId;
    @Schema(description = "发送者ID")
    private Integer senderId;
    @Schema(description = "接收者ID")
    private Integer receiverId;
    @Schema(description = "消息内容")
    private String content;
    @Schema(description = "消息类型")
    private String messageType;
    @Schema(description = "是否已读")
    private Boolean isRead;
    @Schema(description = "发送时间")
    private LocalDateTime createdAt;
}