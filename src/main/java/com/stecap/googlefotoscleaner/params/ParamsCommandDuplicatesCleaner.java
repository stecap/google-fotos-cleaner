package com.stecap.googlefotoscleaner.params;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "remove duplicates by name and date in given folder")
public class ParamsCommandDuplicatesCleaner extends ParamsCommand  {
    @Override
    public String getCommand() {
        return "duplicatesCleaner";
    }

    @Parameter(names = "-keepFolderIfDuplicate", description = "In case of duplicates keep duplicate in this folder (or parent folder)")
    public String keepFolderIfDuplicate;

    @Parameter(names = "-modeDuplicatesCleaner", description = "1=active, 2=dry run, 0=inactive (default=2)")
    public Integer modeDuplicatesCleaner = 2;
}
