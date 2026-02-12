package webserver.http.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    private SessionManager() {
    }

    private static class SessionManagerHolder {
        private static final SessionManager SESSION_MANAGER = new SessionManager();
    }

    public static SessionManager getInstance() {
        return SessionManagerHolder.SESSION_MANAGER;
    }

    public HttpSession createSession() {
        HttpSession session = new HttpSession();
        sessions.put(session.getId(), session);
        logger.debug("Session created: {}", session.getId());
        return session;
    }

    public void invalidateSession(String sessionId) {
        HttpSession session = sessions.remove(sessionId);
        if (session != null) {
            session.invalidate();
            logger.debug("Session invalidate: {}", sessionId);
        }
    }

    public HttpSession getSession(String sessionId) {
        if (sessionId == null) { // sessionId가 null인 경우
            return createSession();
        }

        HttpSession session = sessions.get(sessionId);
        if (session == null) { // sessionId에 해당하는 session이 없는 경우
            return createSession();
        }

        session.access();
        return session;
    }


}
