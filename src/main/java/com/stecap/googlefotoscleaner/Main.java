package com.stecap.googlefotoscleaner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {

    public static void main(String[] args) {
        Params params = new Params();
        try {
            JCommander.newBuilder()
                    .addObject(params)
                    .build()
                    .parse(args);
        } catch(ParameterException e) {
            System.out.println(e.getMessage());
            return;
        }

        Cleaner cleaner = new Cleaner(params);
        cleaner.clean();
    }
}
