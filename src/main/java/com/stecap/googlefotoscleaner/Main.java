package com.stecap.googlefotoscleaner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.stecap.googlefotoscleaner.params.Params;
import com.stecap.googlefotoscleaner.params.ParamsCommandDuplicatesCleaner;
import com.stecap.googlefotoscleaner.params.ParamsCommandExifCleaner;

public class Main {

    public static void main(String[] args) {
        Params params = new Params();
        ParamsCommandExifCleaner paramsCommandExifCleaner = new ParamsCommandExifCleaner();
        ParamsCommandDuplicatesCleaner paramsCommandDuplicatesCleaner = new ParamsCommandDuplicatesCleaner();

        try {

            JCommander jc = JCommander.newBuilder()
                    .addObject(params)
                    .addCommand(paramsCommandExifCleaner.getCommand(), paramsCommandExifCleaner)
                    .addCommand(paramsCommandDuplicatesCleaner.getCommand(), paramsCommandDuplicatesCleaner)
                    .build();

            jc.parse(args);

            System.out.println("fotos path: " + params.googleFotosPath);

            if(jc.getParsedCommand().equals(paramsCommandExifCleaner.getCommand())) {
                ExifCleaner exifCleaner = new ExifCleaner(params, paramsCommandExifCleaner);
                exifCleaner.clean();
            }

            if(jc.getParsedCommand().equals(paramsCommandDuplicatesCleaner.getCommand())) {
                DuplicatesCleaner duplicatesCleaner = new DuplicatesCleaner(params, paramsCommandDuplicatesCleaner);
                duplicatesCleaner.clean();
            }

        } catch(ParameterException e) {
            System.out.println(e.getMessage());
        }
    }
}
