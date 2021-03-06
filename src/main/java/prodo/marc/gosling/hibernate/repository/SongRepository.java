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

    public static void addSong(Song song) {

        Session session = null;
        try {
            session = HibernateUtils.openSession();
            session.getTransaction().begin();
            session.saveOrUpdate(song);
            session.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Error while adding song " + song, e);

        } finally {
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
        } catch (Exception e) {
            logger.error("Error while adding song " + song, e);

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }


    public List<Song> getSongs() {
        Session session = null;
        try {
            session = HibernateUtils.openSession();
            session.getTransaction().begin();
            List<Song> songs = session.createQuery("from Song", Song.class).list();
            session.getTransaction().commit();
            return songs;
        } catch (Exception e) {
            logger.error("Error while getting songs " + e);

        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return Collections.emptyList();
    }

    public static Integer getIDofFile(String currentFileLoc) {
        Session session = null;
        try {
            session = HibernateUtils.openSession();
            session.getTransaction().begin();
            String query1 = "from Song S WHERE lower(S.fileLoc) = :fileLoc";
            List<Song> songs = session.createQuery(query1, Song.class)
                    .setParameter("fileLoc", currentFileLoc.toLowerCase())
                    .list();
            session.getTransaction().commit();
            if (!songs.isEmpty()) {
                return songs.get(0).getId();
            }
        } catch (Exception e) {
            logger.error("Error while getting songs " + e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return null;
    }

    public static List<String> getPublishers() {
        Session session = null;
        try {
            session = HibernateUtils.openSession();
            session.getTransaction().begin();
            String query1 = "select distinct publisher from Song S";
            List<String> publishers = session.createQuery(query1, String.class).list();
            session.getTransaction().commit();
            return publishers;
        } catch (Exception e) {
            logger.error("Error while getting publishers " + e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return Collections.emptyList();
    }

}
