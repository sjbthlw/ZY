package com.hzsun.mpos.Logger;

import java.util.logging.Logger;

public class RefreshLogger {
    private static Logger logger = Logger.getLogger(String.valueOf(RefreshLogger.class));

    public static Logger getLogger() {
        return logger;
    }
}
