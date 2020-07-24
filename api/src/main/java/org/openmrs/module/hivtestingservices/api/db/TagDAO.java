package org.openmrs.module.hivtestingservices.api.db;


import org.openmrs.module.hivtestingservices.model.MuzimaFormTag;

import java.util.List;

public interface TagDAO {
    List<MuzimaFormTag> getAll();

    void save(MuzimaFormTag tag);
}
