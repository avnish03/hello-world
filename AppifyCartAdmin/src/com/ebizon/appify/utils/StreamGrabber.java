package com.ebizon.appify.utils;

/**
 * Created by avnish on 2/9/17.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

class StreamGrabber extends Thread {
    private InputStream in;
    private PrintWriter pw;

    StreamGrabber(InputStream in, PrintWriter pw) {
        this.in = in;
        this.pw = pw;
    }

    @Override
    public void run() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ( (line = br.readLine()) != null) {
                pw.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}