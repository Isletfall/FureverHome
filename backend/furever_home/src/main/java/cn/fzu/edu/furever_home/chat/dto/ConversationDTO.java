package cn.fzu.edu.furever_home.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ConversationDTO", description = "会话摘要")
public class ConversationDTO {
    @Schema(description = "会话ID")
    private Integer conversationId;
    @Schema(description = "对方用户ID")
    private Integer targetUserId;
    @Schema(description = "对方用户名")
    private String targetUserName;
    @Schema(description = "对方头像URL")
    private String targetUserAvatar;
    @Schema(description = "最后一条消息内容")
    private String lastMessage;
    @Schema(description = "最后消息时间")
    private java.time.LocalDateTime lastMessageTime;
    @Schema(description = "未读消息数")
    private Long unreadCount;
}