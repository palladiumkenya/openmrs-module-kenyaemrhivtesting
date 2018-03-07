package org.openmrs.module.hivtestingservices.api.impl;
import org.openmrs.module.hivtestingservices.api.db.HTSDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.Query;
import java.util.List;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.openmrs.module.hivtestingservices.api.PatientContact;

@Repository
@Service
public class HTSDAOImpl implements HTSDAO{
    protected final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private static SessionFactory sessionFactory;

    @Autowired
    private HTSDAO htsDAO;
    /**
     */
    public void setSessionFactory() {
        setSessionFactory();
    }

    /**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public void persistPatientContact(PatientContact patientContact) {
        //Get the current hibernate session
        Session currentSession = sessionFactory.getCurrentSession();
        //Persist pattientcontact
        currentSession.saveOrUpdate(patientContact);
    }

    @Override
    public List<PatientContact> getPatientContact() {
        //Getting the current hibernate session
        Session currentSession = sessionFactory.getCurrentSession();
        //create query String
        String query = "select * from patient_contact where voided !='true'";
         //exec query and get result
        List<PatientContact> patientContacts =  currentSession.createSQLQuery(query).list();
        //return result
      return patientContacts;
    }
    @Override
    public void deletePatientContact(int theId){

    Session currentSession = sessionFactory.getCurrentSession();
    String query = "update PatientContact set voided = 'true' where id=:theId";

    }
    @Override
    public List<PatientContact> searchPatientContact(String searchName){

        // get the current hibernate session
        Session currentSession = sessionFactory.getCurrentSession();

        Query query = null;
        //only search by name if name is not empty
        if(searchName!=null && searchName.trim().length()>0){

          /*  TODO query =currentSession.createQuery("from PatientContact where lower(firstName) like :searchName or lower(lastName) like :searchName or lower(middleName) like :searchName",PatientContact.class);*/
            query.setParameter("searchName","%"+searchName.toLowerCase()+"%");
        }
        else{
            //the searchName is empty...so list patient contacts
           /*TODO query = currentSession.createQuery("from PatientContact",PatientContact.class);*/
        }
        //Execute query and get the result list
        List<PatientContact> contacts = query.list();
       return  contacts;
    }

}
