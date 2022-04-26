package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME;

@Service
public class SessionServiceImpl<S extends Session> implements SessionService<S> {

    static final String RELOAD_AUTHORITIES = "reloadAuthorities";

    private final FindByIndexNameSessionRepository<S> sessionRepository;

    @Autowired
    SessionServiceImpl(FindByIndexNameSessionRepository<S> sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void markSessionToReloadAuthorities(String username) {
        final Map<String, S> map = sessionRepository.findByIndexNameAndIndexValue(PRINCIPAL_NAME_INDEX_NAME, username);
        for (final S session : map.values()) {
            session.setAttribute(RELOAD_AUTHORITIES, true);
            sessionRepository.save(session);
        }
    }

    @Override
    public void unmarkSessionToReloadAuthorities(String sessionId) {
        final S session = sessionRepository.findById(sessionId);
        session.removeAttribute(RELOAD_AUTHORITIES);
        sessionRepository.save(session);
    }
}
