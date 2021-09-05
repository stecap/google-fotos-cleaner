package com.stecap.googlefotoscleaner;

import com.beust.jcommander.Parameter;

public class Params {
    @Parameter(names = "-googleFotosPath", required = true, description = "Google Fotos Path")
    public String googleFotosPath;

    @Parameter(names = "-debug", description = "Debug mode")
    public boolean debug = false;

    @Parameter(names = "-editedTag", required = true, description = "Google Replacement when edited (-bearbeitet for german customers)")
    public String editedTag = "-bearbeitet";

    @Parameter(names = "-modeDeleteEdited", description = "1=active, 2=dry run, 0=inactive")
    public Integer modeDeleteEdited = 1;

    @Parameter(names = "-modeUpdateExif", description = "1=active, 2=dry run, 0=inactive")
    public Integer modeUpdateExif = 1;

    @Parameter(names = "-modeRenamePng", description = "1=active, 2=dry run, 0=inactive")
    public Integer modeRenamePng = 1;
}
