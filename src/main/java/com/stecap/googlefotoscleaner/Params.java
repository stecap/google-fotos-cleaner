package com.stecap.googlefotoscleaner;

import com.beust.jcommander.Parameter;

public class Params {
    @Parameter(names = "-googleFotosPath", required = true, description = "Google Fotos Path")
    public String googleFotosPath;

    @Parameter(names = "-debug", required = true, description = "Debug mode")
    public boolean debug = false;

    @Parameter(names = "-editedTag", required = true, description = "Google Replacement when edited (-bearbeitet in german)")
    public String editedTag = "-bearbeitet";

}
