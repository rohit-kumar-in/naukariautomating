package com.naukri.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

public class RetryUtils {
    private static final Logger logger = LogManager.getLogger(RetryUtils.class);

    public static <T> T retry(int maxAttempts, long waitMs, Callable<T> task) throws Exception {
        int attempt = 1;
        Exception lastException = null;

        while (attempt <= maxAttempts) {
            try {
                return task.call();
            } catch (Exception e) {
                lastException = e;
                logger.warn("Attempt " + attempt + " failed. Reason: " + e.getMessage());
                if (attempt < maxAttempts) {
                    logger.info("Retrying in " + waitMs + " ms...");
                    Thread.sleep(waitMs);
                }
                attempt++;
            }
        }
        
        logger.error("All " + maxAttempts + " attempts failed.");
        throw lastException;
    }

    public static void retryVoid(int maxAttempts, long waitMs, Runnable task) throws Exception {
        retry(maxAttempts, waitMs, () -> {
            task.run();
            return null;
        });
    }
}
