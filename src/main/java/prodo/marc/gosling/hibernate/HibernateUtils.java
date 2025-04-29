package prodo.marc.gosling.hibernate;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class HibernateUtils {
    private static final SessionFactory sessionFactory;
    private static final Logger logger = LogManager.getLogger(HibernateUtils.class);

    static {
        long timer = System.currentTimeMillis();
        try {

            sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        } catch (Exception ex) {

            logger.error("######## Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError("Connection to database error!");
        }
        logger.debug("SessionFactory created in: " + (System.currentTimeMillis() - timer) + "ms");
    }

    public static Session openSession() {
        Session session;
        try {
            session = sessionFactory.openSession();
        } catch (Exception e) {
            logger.error("######## openSession failed.", e);
            throw e;
        }
        return session;
    }
}