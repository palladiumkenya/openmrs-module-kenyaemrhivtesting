/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.hivtestingservices.api.db.hibernate;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.hivtestingservices.advice.model.AOPEncounterEntry;
import org.openmrs.module.hivtestingservices.api.ContactTrace;
import org.openmrs.module.hivtestingservices.api.db.HTSDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;

import java.util.List;

import org.hibernate.SessionFactory;
import org.openmrs.module.hivtestingservices.api.PatientContact;

import javax.persistence.criteria.CriteriaBuilder;


public class HibernateHTSDAO implements HTSDAO {
    protected final Log log = LogFactory.getLog(this.getClass());

    private SessionFactory sessionFactory;
    /**
     * @Autowired private HTSDAO htsDAO;
     */

    /**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * @return the sessionFactory
     */

    @Override
    public PatientContact savePatientContact(PatientContact patientContact) throws DAOException {

        sessionFactory.getCurrentSession().saveOrUpdate(patientContact);
        return patientContact;

    }

    @Override
    public List<PatientContact> getPatientContactByPatient(Patient patient) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(PatientContact.class);
        criteria.add(Restrictions.eq("patientRelatedTo", patient));
        return criteria.list();
    }

    @Override
    public List<ContactTrace> getContactTraceByPatientContact(PatientContact patientContact) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(ContactTrace.class);
        criteria.add(Restrictions.eq("patientContact", patientContact));
        return criteria.list();
    }

    @Override
    public AOPEncounterEntry saveAopEncounterEntry(AOPEncounterEntry aopEncounterEntry) {
        sessionFactory.getCurrentSession().saveOrUpdate(aopEncounterEntry);
        return aopEncounterEntry;
    }

    @Override
    public AOPEncounterEntry getAopEncounterEntry(Integer entryId) {
        return (AOPEncounterEntry) this.sessionFactory.getCurrentSession().get(AOPEncounterEntry.class, entryId);

    }

    @Override
    public List<AOPEncounterEntry> getAopEncounterEntryList() {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(AOPEncounterEntry.class);
        criteria.add(Restrictions.eq("status", 0));
        return criteria.list();
    }

    @Override
    public PatientContact getPatientContactEntryForPatient(Patient patient) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(PatientContact.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.eq("voided", false));
        if(!CollectionUtils.isEmpty(criteria.list())){
            return (PatientContact) criteria.list().get(0);
        }
        return null;
    }

    @Override
    public List<PatientContact> getPatientContacts() {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(PatientContact.class);
        criteria.add(Restrictions.eq("voided", false));
        //return result
        return criteria.list();
    }

    @Override
    public void voidPatientContact(int theId) {

        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(PatientContact.class);
    }

    @Override
    public List<PatientContact> searchPatientContact(String searchName) {
        // get the current hibernate session

        Query query = null;
        //only search by name if name is not empty
        if (searchName != null && searchName.trim().length() > 0) {

        } else {

        }
        List<PatientContact> contacts = query.list();
        return contacts;
    }

    @Override
    public PatientContact getPatientContactByID(Integer patientContactId) {
        return (PatientContact) this.sessionFactory.getCurrentSession().get(PatientContact.class, patientContactId);
    }

    @Override
    public ContactTrace saveClientTrace(ContactTrace contactTrace) throws DAOException {

        sessionFactory.getCurrentSession().saveOrUpdate(contactTrace);
        return contactTrace;
    }

    public ContactTrace getPatientContactTraceById(Integer patientContactTraceId) {
        return (ContactTrace) this.sessionFactory.getCurrentSession().get(ContactTrace.class, patientContactTraceId);

    }

    @Override
    public ContactTrace getLastTraceForPatientContact(PatientContact patientContact) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(ContactTrace.class);
        criteria.add(Restrictions.eq("voided", false));
        criteria.add(Restrictions.eq("patientContact", patientContact));
        criteria.addOrder(Order.desc("date"));
        criteria.addOrder(Order.desc("id"));
        criteria.setMaxResults(1);
        //return result
        if(!CollectionUtils.isEmpty(criteria.list())){
            return (ContactTrace) criteria.list().get(0);
        }
        return null;
    }


}