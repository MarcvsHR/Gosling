package prodo.marc.gosling.hibernate.repository;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import prodo.marc.gosling.dao.Artist;
import prodo.marc.gosling.hibernate.HibernateUtils;

import java.util.Collections;
import java.util.List;

public class ArtistRepository {
    private static final Logger logger = LogManager.getLogger(ArtistRepository.class);

    public static void addArtist(Artist artist ){

        Session session = null;
        try {
            session = HibernateUtils.openSession();
            session.getTransaction().begin();
            session.saveOrUpdate(artist);
            session.getTransaction().commit();
        }catch (Exception e){
            logger.error("Error while adding artist "+artist,e);

        }finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }


    }
    public List<Artist> getArtists(){
        Session session = null;
        try {
            session = HibernateUtils.openSession();
            session.getTransaction().begin();
            List<Artist> artists= session.createQuery("from Artist",Artist.class).list();
            session.getTransaction().commit();
            return  artists;
        }catch (Exception e){
            logger.error("Error while getting artist "+e);

        }finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return Collections.emptyList();
    }


}
