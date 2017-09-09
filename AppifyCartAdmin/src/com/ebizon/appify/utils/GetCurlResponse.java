package com.ebizon.appify.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by avnish on 2/9/17.
 */

public class GetCurlResponse {
    static Logger logger = Logger.getLogger(GetCurlResponse.class.getSimpleName());
        public static String executeCurlRequest(ArrayList<String> commands) throws Exception {
            logger.info("Commands to be executed : "+commands);

            String curlResponse = null;
            try {
                StringWriter infos = new StringWriter();
                StringWriter errors = new StringWriter();

                ProcessBuilder pb = new ProcessBuilder(commands);
                Process process = pb.start();

                StreamGrabber seInfo = new StreamGrabber(process.getInputStream(), new PrintWriter(infos, true));
                StreamGrabber seError = new StreamGrabber(process.getErrorStream(), new PrintWriter(errors, true));
                seInfo.start();
                seError.start();

                process.waitFor();

                seInfo.join();
                seError.join();

                curlResponse = infos.toString();

                logger.info("CURL RESPONSE : "+curlResponse);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return curlResponse;
        }
}