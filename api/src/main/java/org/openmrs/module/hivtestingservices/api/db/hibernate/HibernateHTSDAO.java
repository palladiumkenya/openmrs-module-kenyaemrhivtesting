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
package org.openmrs.module.hivtestingservices.api.db.hibernate;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.hivtestingservices.api.db.HTSDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.db.hibernate.DbSession;
import org.hibernate.Query;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.openmrs.module.hivtestingservices.api.impl.PatientContact;

@Repository("org.openmrs.module.hivtestingservices.api.db.HTSDAO")
public class HibernateHTSDAO implements HTSDAO{
	protected final Log log = LogFactory.getLog(this.getClass());

	//Inject the session factory
	//@Autowired
	SessionFactory sessionFactory;
	/**
	 * @Autowired
	private HTSDAO htsDAO;
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
	public void persistPatientContact(PatientContact patientContact) {
		//Get the current hibernate session
		//Persist patientcontact
		sessionFactory.getCurrentSession().saveOrUpdate(patientContact);

	}

	@Override
	public List<PatientContact> getPatientContacts() {

		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(PatientContact.class);
		criteria.add(Restrictions.eq("voided",false));
		//return result
		return criteria.list();
	}
	@Override
	public void deletePatientContact(int theId){

		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(PatientContact.class);

		//sessionFactory.getCurrentSession().createQuery("update PatientContact set voided = 'true' where id=:theId").executeUpdate();
		//getSessionFactory().getCurrentSession().createQuery(query).executeUpdate();
	}
	@Override
	public List<PatientContact> searchPatientContact(String searchName){
		// get the current hibernate session

		Query query = null;
		//only search by name if name is not empty
		if(searchName!=null && searchName.trim().length()>0){

			//query = this.sessionFactory.getCurrentSession().createQuery("FROM PatientContact where lower(firstName) like :searchName or lower(lastName) like :searchName or lower(middleName) like :searchName");
			//query.setParameter("searchName","%"+searchName.toLowerCase()+"%");

		}
		else{
			//the searchName is empty...so list patient contacts
			//query = this.sessionFactory.getCurrentSession().createQuery("FROM PatientContact");
		}
		//Execute query and get the result list
		List<PatientContact> contacts = query.list();
		return  contacts;
	}

}