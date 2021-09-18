package com.stecap.googlefotoscleaner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FilesManager {
    public Collection<File> getAllMediaFiles(String path) {
        String[] extensions = {
                "png", "jpg", "jpeg", "heic", "mov", "gif", "mp4", "PNG", "JPG", "JPEG", "HEIC", "MOV", "GIF", "MP4"
        };
        Collection<File> files = FileUtils.listFiles(new File(path), extensions, true);

        Map<String, Integer> extensionCounter = new HashMap<>();
        for (File file : files) {
            String extension = FilenameUtils.getExtension(file.getAbsolutePath());
            extensionCounter.merge(extension, 1, Integer::sum);
        }

        log("Collected files:");
        for (Map.Entry<String, Integer> ext : extensionCounter.entrySet()) {
            log(ext.getKey() + ":" + ext.getValue());
        }

        return files;
    }

    private void log(String message) {
        System.out.println(message);
    }
}
