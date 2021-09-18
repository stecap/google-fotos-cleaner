package com.stecap.googlefotoscleaner;

import com.stecap.googlefotoscleaner.params.Params;
import com.stecap.googlefotoscleaner.params.ParamsCommandDuplicatesCleaner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DuplicatesCleaner {
    private final Params params;
    private final ParamsCommandDuplicatesCleaner paramsCommandDuplicatesCleaner;

    public DuplicatesCleaner(Params params, ParamsCommandDuplicatesCleaner paramsCommandDuplicatesCleaner) {
        this.params = params;
        this.paramsCommandDuplicatesCleaner = paramsCommandDuplicatesCleaner;
    }

    private void log(String message) {
        System.out.println(message);
    }

    public void clean() {
        log("collect duplicates");

        Collection<File> allMediaFiles = new FilesManager().getAllMediaFiles(params.googleFotosPath);
        Map<String, List<File>> groupedByFileName = allMediaFiles.stream().collect(Collectors.groupingBy(File::getName));
        List<Map.Entry<String, List<File>>> filteredByDuplicates = groupedByFileName.entrySet().stream().filter(f -> f.getValue().size() > 1).collect(Collectors.toList());

        List<Map.Entry<String, List<Duplicate>>> duplicates = new ArrayList<>();
        for(Map.Entry<String, List<File>> entry: filteredByDuplicates) {
            List<Duplicate> duplicateList = new ArrayList<>();

            int keepSize = 0;
            List<File> potentiallyDelete = entry.getValue();

            if(paramsCommandDuplicatesCleaner.keepFolderIfDuplicate != null) {
                List<File> keep = entry.getValue().stream().filter(f -> FilenameUtils.getFullPath(f.getAbsolutePath()).contains(paramsCommandDuplicatesCleaner.keepFolderIfDuplicate)).collect(Collectors.toList());
                for(File file: keep) {
                    duplicateList.add(new Duplicate(file, false));
                }
                keepSize = keep.size();
                potentiallyDelete = entry.getValue().stream().filter(f -> !FilenameUtils.getFullPath(f.getAbsolutePath()).contains(paramsCommandDuplicatesCleaner.keepFolderIfDuplicate)).collect(Collectors.toList());
            }

            int count = 1;

            for(File file: potentiallyDelete) {
                if(keepSize > 0 || count != 1) {
                    duplicateList.add(new Duplicate(file, true));
                } else {
                    duplicateList.add(new Duplicate(file, false));
                }
                count++;
            }

            duplicates.add(new AbstractMap.SimpleEntry<>(entry.getKey(), duplicateList));
        }

        log("report duplicates ...");
        duplicates.forEach(e -> {
            log("---------------");
            log("DUPLICATE:");
            log(e.getKey() + " : " + e.getValue().size());
            e.getValue().forEach(v -> {
                log(FilenameUtils.getPath(v.getFile().getAbsolutePath()) + " : " + v.isDelete());
            });
        });
        log("report duplicates - done.");
        log("collect duplicates - done.");

        if(paramsCommandDuplicatesCleaner.modeDuplicatesCleaner == 1) {
            log("delete duplicates ...");
            duplicates.forEach(e -> {
                for(Duplicate duplicate: e.getValue()) {
                    if(duplicate.isDelete()) {
                        try {
                            log("delete: " + duplicate.getFile().getAbsolutePath());
                            FileUtils.delete(duplicate.getFile());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            log("delete duplicates - done.");
        }
    }

    static class Duplicate {
        private final boolean delete;
        private final File file;

        public Duplicate(File file, boolean delete) {
            this.file = file;
            this.delete = delete;
        }

        public boolean isDelete() {
            return delete;
        }

        public File getFile() {
            return file;
        }
    }
}
