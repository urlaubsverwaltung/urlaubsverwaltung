package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.tenancy.configuration.single.ConditionalOnSingleTenantMode;

import java.util.Map;

@Service
@ConditionalOnSingleTenantMode
class SessionServiceImpl<S extends Session> implements SessionService {

    static final String RELOAD_AUTHORITIES = "reloadAuthorities";

    private final FindByIndexNameSessionRepository<S> sessionRepository;

    @Autowired
    SessionServiceImpl(FindByIndexNameSessionRepository<S> sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void markSessionToReloadAuthorities(String username) {
        final Map<String, S> map = sessionRepository.findByPrincipalName(username);
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

    @EventListener
    void deleteSessionByEvent(PersonDeletedEvent event) {
        sessionRepository.findByPrincipalName(event.person().getUsername())
            .forEach((s, session) -> sessionRepository.deleteById(session.getId()));
    }
}
