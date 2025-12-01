package cn.fzu.edu.furever_home.chat.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.fzu.edu.furever_home.chat.dto.ConversationDTO;
import cn.fzu.edu.furever_home.chat.dto.MessageDTO;
import cn.fzu.edu.furever_home.chat.request.SendMessageRequest;
import cn.fzu.edu.furever_home.chat.service.ChatService;
import cn.fzu.edu.furever_home.common.result.PageResult;
import cn.fzu.edu.furever_home.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "聊天模块", description = "会话列表、消息历史、发送消息、标记已读、搜索、未读统计")
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/conversations")
    @SaCheckPermission("chat:read")
    @Operation(summary = "获取会话列表")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<PageResult<ConversationDTO>> conversations(@RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "20") int pageSize) {
        Integer uid = StpUtil.getLoginIdAsInt();
        PageResult<ConversationDTO> data = chatService.listConversations(uid, page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @SaCheckPermission("chat:read")
    @Operation(summary = "获取聊天记录")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<PageResult<MessageDTO>> messages(@PathVariable Integer conversationId,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "50") int pageSize,
                                                   @RequestParam(required = false) Integer beforeMessageId) {
        Integer uid = StpUtil.getLoginIdAsInt();
        PageResult<MessageDTO> data = chatService.listMessages(uid, conversationId, page, pageSize, beforeMessageId);
        return Result.success(data);
    }

    @PostMapping("/messages")
    @SaCheckPermission("chat:send_message")
    @Operation(summary = "发送消息")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<MessageDTO> send(@RequestBody @Valid SendMessageRequest req) {
        Integer uid = StpUtil.getLoginIdAsInt();
        MessageDTO dto = chatService.sendMessage(uid, req);
        return Result.success(dto);
    }

    @PutMapping("/conversations/{conversationId}/read")
    @SaCheckPermission("chat:read")
    @Operation(summary = "标记消息已读")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<Void> markRead(@PathVariable Integer conversationId) {
        Integer uid = StpUtil.getLoginIdAsInt();
        chatService.markConversationRead(uid, conversationId);
        return Result.success();
    }

    @GetMapping("/conversations/search")
    @SaCheckPermission("chat:read")
    @Operation(summary = "搜索会话或用户")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<PageResult<ConversationDTO>> search(@RequestParam String keyword,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int pageSize) {
        Integer uid = StpUtil.getLoginIdAsInt();
        PageResult<ConversationDTO> data = chatService.searchConversations(uid, keyword, page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/messages/unread-count")
    @SaCheckPermission("chat:read")
    @Operation(summary = "获取未读消息总数")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<java.util.Map<String, Integer>> unreadCount() {
        Integer uid = StpUtil.getLoginIdAsInt();
        Integer total = chatService.totalUnread(uid);
        java.util.Map<String, Integer> m = new java.util.HashMap<>();
        m.put("totalUnread", total);
        return Result.success(m);
    }

    @GetMapping("/ws/chat/info")
    @SaCheckPermission("chat:read")
    @Operation(summary = "WebSocket 连接信息", description = "用于在 Swagger 中展示聊天实时推送的连接方式与消息格式")
    @Parameter(name = "Authorization", description = "认证令牌，格式为: Bearer {token}", in = ParameterIn.HEADER, required = false, example = "Bearer xxxxxx")
    public Result<java.util.Map<String, Object>> wsInfo() {
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        info.put("urlTemplate", "/api/ws/chat?token={userToken}");
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("type", "new_message");
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("messageId", 1);
        data.put("conversationId", 1);
        data.put("senderId", 1);
        data.put("senderName", "alice");
        data.put("senderAvatar", "https://example.com/avatar.png");
        data.put("content", "你好");
        data.put("messageType", "text");
        data.put("createdAt", "2025-12-01T12:00:00");
        payload.put("data", data);
        info.put("receiveExample", payload);
        return Result.success(info);
    }
}