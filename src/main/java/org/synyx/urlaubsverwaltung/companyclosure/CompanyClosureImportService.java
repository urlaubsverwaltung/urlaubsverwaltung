package org.synyx.urlaubsverwaltung.companyclosure;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class CompanyClosureImportService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final CompanyClosureRepository companyClosureRepository;
    private final CompanyClosureVersionRepository companyClosureVersionRepository;

    CompanyClosureImportService(CompanyClosureRepository companyClosureRepository, CompanyClosureVersionRepository companyClosureVersionRepository) {
        this.companyClosureRepository = companyClosureRepository;
        this.companyClosureVersionRepository = companyClosureVersionRepository;
    }

    @Transactional(readOnly = true)
    public List<CompanyClosureEntity> findAllClosures() {
        return companyClosureRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<CompanyClosureVersionEntity> findAllVersions() {
        return companyClosureVersionRepository.findAll();
    }

    @Transactional
    public void importCompanyClosure(CompanyClosureEntity closure, List<CompanyClosureVersionEntity> versions) {
        final CompanyClosureEntity saved = companyClosureRepository.save(closure);
        versions.forEach(version -> version.setCompanyClosureId(saved.getId()));
        companyClosureVersionRepository.saveAll(versions);
        LOG.debug("imported company closure: {}", saved);
    }

    @Transactional
    public void deleteAll() {
        companyClosureVersionRepository.deleteAll();
        companyClosureRepository.deleteAll();
    }
}
