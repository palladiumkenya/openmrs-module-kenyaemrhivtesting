package org.openmrs.module.hivtestingservices.api.service.impl;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.hivtestingservices.api.db.TagDAO;
import org.openmrs.module.hivtestingservices.api.service.MuzimaTagService;
import org.openmrs.module.hivtestingservices.model.MuzimaFormTag;

import java.util.List;

public class MuzimaTagServiceImpl extends BaseOpenmrsService implements MuzimaTagService {
    private TagDAO dao;

    public MuzimaTagServiceImpl(TagDAO dao) {
        this.dao = dao;
    }

    public List<MuzimaFormTag> getAll() {
        return dao.getAll();
    }

    public MuzimaFormTag add(String name) {
        MuzimaFormTag tag = new MuzimaFormTag(name);
        dao.save(tag);
        return tag;
    }
}
