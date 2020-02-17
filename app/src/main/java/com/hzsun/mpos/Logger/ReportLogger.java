package com.hzsun.mpos.Logger;

import java.util.logging.Logger;

public class ReportLogger {
    private static Logger logger = Logger.getLogger(String.valueOf(ReportLogger.class));

    public static Logger getLogger() {
        return logger;
    }
}
