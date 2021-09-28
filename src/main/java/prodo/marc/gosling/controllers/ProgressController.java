package prodo.marc.gosling.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import prodo.marc.gosling.service.FileUtils;
import prodo.marc.gosling.service.SongGlobal;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressController {

    private static final Logger logger = LogManager.getLogger(SongController.class);

    public ProgressBar progressBar;
    public Label progressLabel;
    public Label statusLabel;

    public void initialize() {
        AtomicInteger i = new AtomicInteger();
        List<Path> mp3List = SongGlobal.getMP3List();
        int max = mp3List.size();
        List<String> dupeFiles = new ArrayList<>();

        progressLabel.setText(i+"/"+max);

        Thread folderImportTask = new Thread(() -> {
            Instant start = Instant.now();
            mp3List.forEach(file -> {
                i.getAndIncrement();
                logger.debug("processing file: "+ file);
                logger.debug(i+" out of "+mp3List.size());

                String getFile = FileUtils.addMP3(file, SongGlobal.getCurrentSong().getEditor());
                if (getFile != null) {dupeFiles.add(getFile);}
                Platform.runLater(() -> {
                    updateProgressBar(i+"/"+max,i.doubleValue()/max);
                    if (i.get() == max) {
                        statusLabel.setText("Import done. Please close window and refresh table.");
                    }
                });
            });
            Instant stop = Instant.now();
            logger.debug("Thread finished in: "+ java.time.Duration.between(start, stop).toSeconds());
            logger.debug("duplicate files found: "+dupeFiles);
        });
        folderImportTask.start();
    }

    @FXML
    private void updateProgressBar(String string, Double value) {
        progressLabel.setText(string);
        progressBar.setProgress(value);
    }
}
