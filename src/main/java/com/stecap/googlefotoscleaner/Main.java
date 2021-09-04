package com.stecap.googlefotoscleaner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {

    public static void main(String[] args) {
        try {
            JCommander.newBuilder()
                    .addObject(new Params())
                    .build()
                    .parse(args);
        } catch(ParameterException e) {
            System.out.println(e.getMessage());
        }
    }
}
