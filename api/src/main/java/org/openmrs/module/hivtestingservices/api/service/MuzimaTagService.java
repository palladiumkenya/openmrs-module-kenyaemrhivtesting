package org.openmrs.module.hivtestingservices.api.service;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.hivtestingservices.model.MuzimaFormTag;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MuzimaTagService extends OpenmrsService {
    @Transactional(readOnly = true)
    List<MuzimaFormTag> getAll();

    @Transactional
    MuzimaFormTag add(String name);
}
