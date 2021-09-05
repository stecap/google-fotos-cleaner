package com.stecap.googlefotoscleaner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cleaner {
    private Params params = null;

    public Cleaner(Params params) {
        this.params = params;
    }

    public void clean() {
        deleteEdited();
        Map<String, String> updateExif = collectUpdateExif();
        Map<String, String> renameToJpg = updateExif(updateExif);
        renamePng(renameToJpg);
        updateExif(renameToJpg);
    }

    private void log(String message) {
        System.out.println(message);
    }

    private void renamePng(Map<String, String> renameToJpg) {
        for(Map.Entry<String, String> e: renameToJpg.entrySet()) {
            String png = e.getKey().replace(".PNG", ".JPG");
            String json = e.getValue().replace(".PNG", ".JPG");
            try {
                log(e.getKey() + " rename to " + png);
                FileUtils.moveFile(new File(e.getKey()), new File(png));
                log(e.getValue() + " rename to " + json);
                FileUtils.moveFile(new File(e.getValue()), new File(json));
            } catch (IOException ex) {
                log(Arrays.toString(ex.getStackTrace()));
            }
        }
    }

    private Collection<File> getAllMediaFiles() {
        String[] extensions = {
                "png", "jpg", "jpeg", "heic", "mov", "gif", "mp4", "PNG", "JPG", "JPEG", "HEIC", "MOV", "GIF", "MP4"
        };
        Collection<File> files = FileUtils.listFiles(new File(params.googleFotosPath), extensions, true);

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

    private void deleteEdited() {
        if (params.modeDeleteEdited == 1 || params.modeDeleteEdited == 2) {

            log("collect delete edited ($edited_tag) if original exists ...");

            List<String> missingOriginal = new ArrayList<>();
            List<String> deleteEdited = new ArrayList<>();

            for (File file : getAllMediaFiles()) {
                String fileAbsolutePath = file.getAbsolutePath();
                String fileName = FilenameUtils.getName(fileAbsolutePath);
                if (fileName.contains(params.editedTag)) {
                    String cleanedFileName = file.getAbsolutePath().replace(params.editedTag, "");
                    if (new File(cleanedFileName).exists()) {
                        deleteEdited.add(fileAbsolutePath);
                    } else {
                        missingOriginal.add(fileAbsolutePath);
                    }
                }
            }

            log("Collected for deletion (" + deleteEdited.size() + "):");
            if (params.debug) {
                for (String delete : deleteEdited) {
                    log(delete);
                }
            }

            log("Missing originals (" + missingOriginal.size() + "):");
            if (params.debug) {
                for (String missing : missingOriginal) {
                    log(missing);
                }
            }

            log("collect delete edited ($edited_tag) if original exists - done.");

            log("delete edited tag list ...");
            if (params.modeDeleteEdited == 1) {
                for (String delete : deleteEdited) {
                    log("delete: " + delete);
                    try {
                        FileUtils.delete(new File(delete));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                log("not active");
            }
            log("delete edited tag list - done.");
        }
    }

    List<String> missingJson = new ArrayList<>();

    private Map<String, String> collectUpdateExif() {
        Map<String, String> mapMediaToJson = new HashMap<>();

        if (params.modeUpdateExif == 1 || params.modeUpdateExif == 2) {
            log("collect for update exif ...");

            for (File file : getAllMediaFiles()) {
                String fileNameWithExtension = FilenameUtils.getName(file.getAbsolutePath());
                String fileName = FilenameUtils.getBaseName(file.getAbsolutePath());
                String fileExtension = "." + FilenameUtils.getExtension(file.getAbsolutePath());
                String fullPath = FilenameUtils.getFullPath(file.getAbsolutePath());

                Pattern pattern = Pattern.compile("([(]){1}([0-9])+([)]){1}");
                Matcher matcher = pattern.matcher(fileName);
                String file_counter = "";
                if (matcher.find()) {
                    file_counter = matcher.group(0);
                }

                // 1. search for exact same with json extension
                String json = fullPath + fileName.replace(file_counter, "") + fileExtension + file_counter + ".json";

                if (!new File(json).exists()) {
                    if (params.debug) {
                        log("#1 " + json + " <-- " + fileNameWithExtension);
                    }

                    // # 2. search for live photos (2 files with equal name - one of them with MP4 extension)
                    String livePhotoJson = fullPath + fileName.replace(file_counter, "") + ".JPG" + file_counter + ".json";
                    if (!new File(livePhotoJson).exists()) {
                        if (params.debug) {
                            log("#2 " + livePhotoJson + " <-- " + fileNameWithExtension);
                        }

                        // #3. search for max $max_length characters match
                        int max_length = 46;
                        String maxLengthFileNameWithExtension = fileNameWithExtension.substring(0, Math.min(fileNameWithExtension.length(), max_length));
                        String maxLengthFileNameWithExtensionJson = fullPath + maxLengthFileNameWithExtension + ".json";

                        if (!new File(maxLengthFileNameWithExtensionJson).exists()) {
                            if (params.debug) {
                                log("#3 " + maxLengthFileNameWithExtensionJson + " <-- " + fileNameWithExtension);
                            }

                            if (!fileNameWithExtension.contains(params.editedTag)) {
                                log("#4 (!) missing json: " + file.getAbsolutePath());
                                log("---------");
                                log("json: " + json);
                                log("live_photo_json: " + livePhotoJson);
                                log("max_length_file_name_with_extension_json: " + maxLengthFileNameWithExtensionJson);
                                log("---------");

                                missingJson.add(file.getAbsolutePath());
                            }
                        } else {
                            mapMediaToJson.put(file.getAbsolutePath(), maxLengthFileNameWithExtensionJson);
                        }
                    } else {
                        mapMediaToJson.put(file.getAbsolutePath(), livePhotoJson);
                    }
                } else {
                    mapMediaToJson.put(file.getAbsolutePath(), json);
                }
            }
        } else {
            log("updateExif not active.");
        }

        return mapMediaToJson;
    }

    private Map<String, String> updateExif(Map<String, String> updateList) {
        Map<String, String> renameToJpg = new HashMap<>();

        if (params.modeUpdateExif == 1) {
            for (Map.Entry<String, String> e : updateList.entrySet()) {
                String[] command = {"exiftool", "-d", "%s", "-tagsfromfile", e.getValue(),
                        "-GPSAltitude<GeoDataAltitude", "-GPSLatitude<GeoDataLatitude", "-GPSLatitudeRef<GeoDataLatitude", "-GPSLongitude<GeoDataLongitude",
                        "-GPSLongitudeRef<GeoDataLongitude", "-Keywords<Tags", "-Subject<Tags", "-Caption-Abstract<Description", "-ImageDescription<Description",
                        "-ExifIFD:DateTimeOriginal<PhotoTakenTimeTimestamp", "-IFD0:ModifyDate<PhotoTakenTimeTimestamp", "-filecreatedate<phototakentimetimestamp",
                        "-System:FileModifyDate<phototakentimetimestamp", "-ext", "*", "-overwrite_original", "-progress", "-efile", params.logUpdateExifErrorsPath, e.getKey()
                };

                ProcessBuilder builder = new ProcessBuilder(command);
                try {
                    Process p = builder.start();
                    int result = p.waitFor();
                    if (result != 0) {
                        String error = IOUtils.toString(p.getErrorStream(), StandardCharsets.UTF_8.name());
                        log("error: " + error);

                        if (error.startsWith("Error: Not a valid PNG (looks more like a JPEG)")) {
                            renameToJpg.put(e.getKey(), e.getValue());
                        }
                    }
                    p.destroy();
                } catch (IOException | InterruptedException ex) {
                    log("exception: " + Arrays.toString(ex.getStackTrace()));
                }
            }
        } else {
            log("updateExif not active.");
        }

        return renameToJpg;
    }
}