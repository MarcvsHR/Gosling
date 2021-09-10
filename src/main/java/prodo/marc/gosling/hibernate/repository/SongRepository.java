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

    public static void delete(Song song) {
        Session session = null;
        try {
            session = HibernateUtils.openSession();
            session.getTransaction().begin();
            session.delete(song);
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

    public Boolean checkForDupes(Song song){
        Session session = null;
        try {
            session = HibernateUtils.openSession();
            session.getTransaction().begin();
            String query1 = "from Song S WHERE S.fileLoc = '"+song.getFileLoc()+
                    "' OR (S.artist = '"+song.getArtist()+
                    "' AND S.title = '"+song.getTitle()+"')";
            logger.debug(query1);
            List<Song> songs = session.createQuery(query1,Song.class).list();
            session.getTransaction().commit();
            if (!songs.isEmpty()) return true;
        }catch (Exception e){
            logger.error("Error while getting songs "+e);

        }finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return false;
    }


}
