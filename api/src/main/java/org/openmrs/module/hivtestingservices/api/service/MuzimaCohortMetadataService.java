package org.openmrs.module.hivtestingservices.api.service;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.hivtestingservices.model.MuzimaCohortMetadata;

import java.util.List;

public interface MuzimaCohortMetadataService extends OpenmrsService {
    List<MuzimaCohortMetadata> saveMuzimaCohortMetadata(final List<MuzimaCohortMetadata> muzimaCohortMetadata);
    List<MuzimaCohortMetadata> getMuzimaCohortMetadata(List<Integer> patientIds, Integer cohortId);
    List<Object> executeFilterQuery(String filterQuery);
    void deleteMuzimaCohortMetadata(final List<MuzimaCohortMetadata> muzimaCohortMetadata);
}
