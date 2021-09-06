package prodo.marc.gosling.hibernate.repository;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import prodo.marc.gosling.dao.Song;
import prodo.marc.gosling.hibernate.HibernateUtils;

import java.util.Collections;
import java.util.List;

public class SongRepository {
    private static final Logger logger = LogManager.getLogger(SongRepository.class);

    public static void addSong(Song song ){

        Session session = null;
        try {
            session = HibernateUtils.openSession();
            session.getTransaction().begin();
            session.saveOrUpdate(song);
            session.getTransaction().commit();
        }catch (Exception e){
            logger.error("Error while adding song "+song,e);

        }finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }


    }
    public List<Song> getSongs(){
        Session session = null;
        try {
            session = HibernateUtils.openSession();
            session.getTransaction().begin();
            List<Song> songs = session.createQuery("from Song",Song.class).list();
            session.getTransaction().commit();
            return songs;
        }catch (Exception e){
            logger.error("Error while getting songs "+e);

        }finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return Collections.emptyList();
    }


}
