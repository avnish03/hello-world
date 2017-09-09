package com.ebizon.appify.utils;

import com.ebizon.appify.builder.BuilderConfig;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by avnish on 25/5/17.
 */
public class TransferFilesToIOSServer {

    public static boolean copyFiles(String path, String fileName)
    {
        int exitValue = 0;
        // code to delete file physically
        ArrayList<String> commands = new ArrayList<String>();
        commands.add(BuilderConfig.getInstance().getCopyFilesScript());
        commands.add(path);
        commands.add(fileName);

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb = pb.inheritIO();
        Process process = null;

        try {
            process = pb.start();
            exitValue = process.waitFor();
            System.out.println("Copying Files : "+path+fileName);
            System.out.println("Copying Files on physical disk exitvalue "+exitValue);
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(exitValue == 0) return true;
        else return false;
    }


    public static void copyAndroidConfig(String fileName)
    {
        int exitValue = 0;
        // code to delete file physically
        ArrayList<String> commands = new ArrayList<String>();
        commands.add(BuilderConfig.getInstance().getCopyAndroiConfigScript());
        commands.add(fileName);

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb = pb.inheritIO();
        Process process = null;

        try {
            process = pb.start();
            exitValue = process.waitFor();
            System.out.println("Copying Android Config Backup exitvalue "+exitValue);
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
