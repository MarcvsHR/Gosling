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

    public static void testing() {
        long timer = System.currentTimeMillis();
        try (Session session = HibernateUtils.openSession()) {
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
        logger.debug("testing took: " + (System.currentTimeMillis() - timer) + "ms");
    }

    public static void addSong(Song song) {
        long timer = System.currentTimeMillis();
        try (Session session = HibernateUtils.openSession()) {
            session.getTransaction().begin();
            session.saveOrUpdate(song);
            session.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Error while adding song " + song, e);
        }
        logger.debug("addSong took: " + (System.currentTimeMillis() - timer) + "ms");
    }

    public static void delete(Song song) {
        long timer = System.currentTimeMillis();
        try (Session session = HibernateUtils.openSession()) {
            session.getTransaction().begin();
            session.delete(song);
            session.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Error while adding song " + song, e);
        }

        logger.debug("delete took: " + (System.currentTimeMillis() - timer) + "ms");
    }

    public List<Song> getSongs() {
        long timer = System.currentTimeMillis();
        try (Session session = HibernateUtils.openSession()) {
            session.getTransaction().begin();
            List<Song> songs = session.createQuery("from Song", Song.class).list();
            session.getTransaction().commit();
            return songs;
        } catch (Exception e) {
            logger.error("Error while getting songs " + e);

        }

        logger.debug("getSongs took: " + (System.currentTimeMillis() - timer) + "ms");

        return Collections.emptyList();
    }

    public static Integer getFileID(String currentFileLoc) {
        long timer = System.currentTimeMillis();
        try (Session session = HibernateUtils.openSession()) {
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
        }

        logger.debug("getFileID took: " + (System.currentTimeMillis() - timer) + "ms");

        return null;
    }

    public static List<String> getPublishers() {
        long timer = System.currentTimeMillis();
        try (Session session = HibernateUtils.openSession()) {
            session.getTransaction().begin();
            //get all the publishers from the database and how often they are in the database
            String query1 = "select publisher from Song S";
            List<String> publishers = session.createQuery(query1, String.class).list();
            session.getTransaction().commit();
            publishers.remove(null);
            publishers.remove("");

            //sort the publishers by how often they are in the database
            return publishers.stream()
                    .distinct()
                    .sorted((o1, o2) -> {
                        int count1 = Collections.frequency(publishers, o1);
                        int count2 = Collections.frequency(publishers, o2);
                        return Integer.compare(count2, count1);
                    }).toList();

        } catch (Exception e) {
            logger.error("Error while getting publishers " + e);
        }
        logger.debug("getPublishers took: " + (System.currentTimeMillis() - timer) + "ms");

        return Collections.emptyList();
    }

}
