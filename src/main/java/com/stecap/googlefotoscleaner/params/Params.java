package com.stecap.googlefotoscleaner.params;

import com.beust.jcommander.Parameter;

public class Params {
    @Parameter(names = "-googleFotosPath", required = true, description = "Google Fotos Path")
    public String googleFotosPath;

    @Parameter(names = "-debug", description = "Debug mode")
    public boolean debug = false;
}
