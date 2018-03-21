/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.hivtestingservices.api.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.impl.PatientContact;
import org.openmrs.module.hivtestingservices.api.db.HTSDAO;
import org.openmrs.module.hivtestingservices.api.db.hibernate.HibernateHTSDAO;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * It is a default implementation of {@link HTSService}.
 */
@Service("HTSServiceImpl")
@Component
public class HTSServiceImpl implements HTSService{

    protected final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private HibernateHTSDAO patientContactDAO;

    public void setPatientContactDAO(HibernateHTSDAO patientContactDAO) {
        this.patientContactDAO = patientContactDAO;
    }

    public HibernateHTSDAO getPatientContactDAO() {
        return patientContactDAO;
    }

    public HibernateHTSDAO getDao()
    {
        return patientContactDAO;
    }

    @Override
    @Transactional
    public  List<PatientContact> getPatientContacts(){

        return patientContactDAO.getPatientContacts();
    }
    @Override
    @Transactional
    public void persistPatientContact(PatientContact patientContact){
        patientContactDAO.persistPatientContact(patientContact);
    }

    @Override
    @Transactional
    public List<PatientContact> searchPatientContact(String searchName) {

        return patientContactDAO.searchPatientContact(searchName);
    }

    @Override
    @Transactional
    public void deletePatientContact(int theId){
        patientContactDAO.deletePatientContact(theId);
    }

    @Override
    public void onStartup() {

    }

    @Override
    public void onShutdown() {

    }


}
