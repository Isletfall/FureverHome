package cn.fzu.edu.furever_home.chat.ws;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
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
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ChatWebSocketSessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Integer uid = parseAndAuth(session.getUri());
        if (uid == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        session.getAttributes().put("uid", uid);
        sessionManager.register(uid, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Object v = session.getAttributes().get("uid");
        if (v instanceof Integer) {
            sessionManager.unregister((Integer) v, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    }

    private Integer parseAndAuth(URI uri) {
        if (uri == null || uri.getQuery() == null) return null;
        Map<String, String> q = parseQuery(uri.getQuery());
        String token = q.get("token");
        if (token == null || token.isBlank()) return null;
        try {
            Object id = StpUtil.getLoginIdByToken(token);
            if (id == null) return null;
            return Integer.parseInt(id.toString());
        } catch (Exception e) {
            return null;
        }
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