package com.stecap.googlefotoscleaner.params;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "exif cleaner")
public class ParamsCommandExifCleaner extends ParamsCommand {
    @Parameter(names = "-editedTag", required = true, description = "Google Replacement when edited (-bearbeitet for german customers)")
    public String editedTag = "-bearbeitet";

    @Parameter(names = "-modeDeleteEdited", description = "1=active, 2=dry run, 0=inactive")
    public Integer modeDeleteEdited = 1;

    @Parameter(names = "-modeUpdateExif", description = "1=active, 2=dry run, 0=inactive")
    public Integer modeUpdateExif = 1;

    @Parameter(names = "-modeRenamePng", description = "1=active, 2=dry run, 0=inactive")
    public Integer modeRenamePng = 1;

    @Override
    public String getCommand() {
        return "exifCleaner";
    }
}
