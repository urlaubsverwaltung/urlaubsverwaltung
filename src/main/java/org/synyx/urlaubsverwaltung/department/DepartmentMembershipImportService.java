package org.synyx.urlaubsverwaltung.department;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class DepartmentMembershipImportService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final DepartmentMembershipRepository repository;

    DepartmentMembershipImportService(DepartmentMembershipRepository repository) {
        this.repository = repository;
    }

    public void importDepartmentMembership(DepartmentMembershipEntity entity) {
        repository.save(entity);
        LOG.debug("imported department membership: {}", entity);
    }
}
