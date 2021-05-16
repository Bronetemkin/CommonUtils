package ru.hnm1nd.base.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

    public static String exceptionAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static String exceptionAsString(Throwable t, int maxLength) {
        String exceptionStackTrace = exceptionAsString(t);
        return exceptionStackTrace.substring(0, Math.min(exceptionStackTrace.length(), maxLength));
    }

}
