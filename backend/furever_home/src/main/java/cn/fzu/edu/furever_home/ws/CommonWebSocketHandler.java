package cn.fzu.edu.furever_home.ws;

import cn.dev33.satoken.stp.StpUtil;
import cn.fzu.edu.furever_home.chat.mapper.ChatMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommonWebSocketHandler extends TextWebSocketHandler {
    private final cn.fzu.edu.furever_home.ws.WebSocketSessionManager sessionManager;
    private final ChatMapper chatMapper;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private static final Logger log = LoggerFactory.getLogger(CommonWebSocketHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Integer uid = parseAndAuth(session.getUri());
        if (uid == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        session.getAttributes().put("uid", uid);
        sessionManager.register(uid, session);
        log.info("WS connected uid={} sessionId={}", uid, session.getId());
        try {
            String key = "notify:pending:" + uid;
            String text;
            // 逐个弹出并推送离线未送达的提醒
            while ((text = stringRedisTemplate.opsForList().rightPop(key)) != null) {
                sessionManager.sendToUser(uid, text);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Object v = session.getAttributes().get("uid");
        if (v instanceof Integer) {
            sessionManager.unregister((Integer) v, session);
        }
        log.info("WS closed uid={} sessionId={} status={}", v, session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Object v = session.getAttributes().get("uid");
        if (!(v instanceof Integer))
            return;
        Integer uid = (Integer) v;
        String payload = message.getPayload();
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode t = root.get("type");
            if (t == null || !t.isTextual())
                return;
            String type = t.asText();
            if ("typing".equals(type)) {
                JsonNode cidNode = root.get("conversationId");
                JsonNode actionNode = root.get("action");
                if (cidNode == null || !cidNode.isInt())
                    return;
                Integer conversationId = cidNode.asInt();
                Integer targetId = findTargetUserId(conversationId, uid);
                if (targetId == null)
                    return;
                java.util.Map<String, Object> out = new java.util.HashMap<>();
                out.put("type", "typing");
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("conversationId", conversationId);
                data.put("fromUserId", uid);
                data.put("action", actionNode != null && actionNode.isTextual() ? actionNode.asText() : "start");
                out.put("data", data);
                String text = objectMapper.writeValueAsString(out);
                log.info("WS event typing from uid={} to userId={} payload={}", uid, targetId, text);
                sessionManager.sendToUser(targetId, text);
            }
        } catch (Exception ignored) {
        }
    }

    private Integer parseAndAuth(URI uri) {
        if (uri == null || uri.getQuery() == null)
            return null;
        Map<String, String> q = parseQuery(uri.getQuery());
        String token = q.get("token");
        if (token == null || token.isBlank())
            return null;
        try {
            Object id = StpUtil.getLoginIdByToken(token);
            if (id == null)
                return null;
            return Integer.parseInt(id.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer findTargetUserId(Integer conversationId, Integer currentUserId) {
        cn.fzu.edu.furever_home.chat.entity.Chat c = chatMapper.selectById(conversationId);
        if (c == null)
            return null;
        if (java.util.Objects.equals(c.getCreatorId(), currentUserId))
            return c.getReceiverId();
        return c.getCreatorId();
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> m = new HashMap<>();
        for (String p : query.split("&")) {
            int i = p.indexOf('=');
            if (i > 0) {
                String k = p.substring(0, i);
                String v = p.substring(i + 1);
                m.put(k, v);
            }
        }
        return m;
    }
}

