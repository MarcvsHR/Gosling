package prodo.marc.gosling.service;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import lombok.NonNull;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;

public class ID3v2Utils {

    private static final Logger logger = LogManager.getLogger(ID3v2Utils.class);
    public static boolean compareID3v2(@NonNull ID3v2 source,@NonNull ID3v2 target){
        //TODO ovdje neka pametnija implementacija
        return  source.equals(target);
    }
    /**
     * Extract idv3 tage for given file
     * */
    public static ID3v2 getID3(String fileLoc) {

        File mp3File = new File(fileLoc);
        try {
            Mp3File song = new Mp3File(mp3File);

            return song.getId3v2Tag();
        } catch (Exception ignored) {
            logger.error("can't fetch ID3 data from file",ignored);

            return new ID3v24Tag();
        }
    }
}
