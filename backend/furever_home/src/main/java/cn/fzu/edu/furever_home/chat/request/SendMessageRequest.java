package cn.fzu.edu.furever_home.chat.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "SendMessageRequest", description = "发送消息请求")
public class SendMessageRequest {
    @Schema(description = "接收者用户ID")
    @NotNull
    private Integer receiverId;
    @Schema(description = "消息内容")
    @NotBlank
    private String content;
    @Schema(description = "消息类型")
    private String messageType;
    @Schema(description = "会话ID")
    private Integer conversationId;
}