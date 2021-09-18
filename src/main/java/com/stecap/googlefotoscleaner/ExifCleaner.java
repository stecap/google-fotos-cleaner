package com.stecap.googlefotoscleaner;

import com.stecap.googlefotoscleaner.params.Params;
import com.stecap.googlefotoscleaner.params.ParamsCommandExifCleaner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExifCleaner {
    private final Params params;
    private final ParamsCommandExifCleaner paramsCommandExifCleaner;

    public ExifCleaner(Params params, ParamsCommandExifCleaner paramsCommandExifCleaner) {
        this.params = params;
        this.paramsCommandExifCleaner = paramsCommandExifCleaner;
    }

    public void clean() {
        deleteEdited();
        Map<String, String> allItems = collectUpdateExif();
        Map<String, String> errorPngToJson = updateExif(allItems);
        Map<String, String> jpgToJson = renamePngToJpg(errorPngToJson);
        updateExif(jpgToJson);
    }

    private void log(String message) {
        System.out.println(message);
    }

    private Map<String, String> renamePngToJpg(Map<String, String> pngToJson) {
        Map<String, String> jpgToJson = new HashMap<>();
        log("renaming of png ...");

        if(paramsCommandExifCleaner.modeRenamePng == 1 ||paramsCommandExifCleaner.modeRenamePng == 2) {
            for (Map.Entry<String, String> e : pngToJson.entrySet()) {
                String jpg = e.getKey().replace(".PNG", ".JPG");
                String jpgJson = e.getValue().replace(".PNG", ".JPG");
                try {
                    log(e.getKey() + " rename to " + jpg);
                    FileUtils.moveFile(new File(e.getKey()), new File(jpg));
                    log(e.getValue() + " rename to " + jpgJson);
                    FileUtils.moveFile(new File(e.getValue()), new File(jpgJson));
                    jpgToJson.put(jpg, jpgJson);
                } catch (IOException ex) {
                    log(Arrays.toString(ex.getStackTrace()));
                }
            }
        } else {
            log("dry run! rename png inactive.");
        }
        log("renaming of png - done.");
        return jpgToJson;
    }

    private void deleteEdited() {
        if (paramsCommandExifCleaner.modeDeleteEdited == 1 || paramsCommandExifCleaner.modeDeleteEdited == 2) {
            log("collect delete edited (" + paramsCommandExifCleaner.editedTag + ") if original exists ...");

            List<String> missingOriginal = new ArrayList<>();
            List<String> deleteEdited = new ArrayList<>();

            for (File file : new FilesManager().getAllMediaFiles(params.googleFotosPath)) {
                String fileAbsolutePath = file.getAbsolutePath();
                String fileName = FilenameUtils.getName(fileAbsolutePath);
                if (fileName.contains(paramsCommandExifCleaner.editedTag)) {
                    String cleanedFileName = file.getAbsolutePath().replace(paramsCommandExifCleaner.editedTag, "");
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

            log("collect delete edited (" + paramsCommandExifCleaner.editedTag + ") if original exists - done.");

            log("delete edited tag list ...");
            if (paramsCommandExifCleaner.modeDeleteEdited == 1) {
                for (String delete : deleteEdited) {
                    log("delete: " + delete);
                    try {
                        FileUtils.delete(new File(delete));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                log("dry run! no deletion.");
            }
            log("delete edited tag list - done.");
        }
    }

    List<String> missingJson = new ArrayList<>();

    private Map<String, String> collectUpdateExif() {
        Map<String, String> mapMediaToJson = new HashMap<>();

        if (paramsCommandExifCleaner.modeUpdateExif == 1 || paramsCommandExifCleaner.modeUpdateExif == 2) {
            log("collect for update exif ...");

            for (File file : new FilesManager().getAllMediaFiles(params.googleFotosPath)) {
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

                            if (!fileNameWithExtension.contains(paramsCommandExifCleaner.editedTag)) {
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
        log("updateExif ...");
        Map<String, String> renameToJpg = new HashMap<>();

        if (paramsCommandExifCleaner.modeUpdateExif == 1) {
            long counter = 1;
            long size = updateList.size();
            for (Map.Entry<String, String> e : updateList.entrySet()) {
                String[] command = {"exiftool", "-d", "%s", "-tagsfromfile", e.getValue(),
                        "-GPSAltitude<GeoDataAltitude", "-GPSLatitude<GeoDataLatitude", "-GPSLatitudeRef<GeoDataLatitude", "-GPSLongitude<GeoDataLongitude",
                        "-GPSLongitudeRef<GeoDataLongitude", "-Keywords<Tags", "-Subject<Tags", "-Caption-Abstract<Description", "-ImageDescription<Description",
                        "-ExifIFD:DateTimeOriginal<PhotoTakenTimeTimestamp", "-IFD0:ModifyDate<PhotoTakenTimeTimestamp", "-filecreatedate<phototakentimetimestamp",
                        "-System:FileModifyDate<phototakentimetimestamp", "-ext", "*", "-overwrite_original", "-progress", e.getKey()
                };

                ProcessBuilder builder = new ProcessBuilder(command);
                try {
                    log("(" + counter++ + "/" + size + ") update " + e.getKey() + " from " + e.getValue());

                    Process p = builder.start();
                    int result = p.waitFor();
                    if (result != 0) {
                        String error = IOUtils.toString(p.getErrorStream(), StandardCharsets.UTF_8.name());
                        log("error: " + error.trim());

                        if (error.startsWith("Error: Not a valid PNG (looks more like a JPEG)")) {
                            renameToJpg.put(e.getKey(), e.getValue());
                        }
                    }
                    p.destroy();
                } catch (IOException | InterruptedException ex) {
                    log("exception: " + Arrays.toString(ex.getStackTrace()));
                }
            }
        } else if (paramsCommandExifCleaner.modeUpdateExif == 2) {
            log("dry run! no exif update.");
        } else {
            log("updateExif not active.");
        }

        log("updateExif - done.");

        return renameToJpg;
    }
}
