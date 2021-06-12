package ru.hnm1nd.base.utils.log;

import com.google.gson.Gson;
import ru.hnm1nd.base.utils.ExceptionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LocalLoggerServiceImpl implements LoggerService {

    private static Map<String, LoggerService> instance = new HashMap<>();

    private static final long MAX_LOG_FILE_LENGTH = 4 * 1024 * 1024;
    private Path path;
    private String fileName, pathURI;
    private SimpleDateFormat dateFormat;
    private boolean initFlag = true;
    private boolean printToConsole = true;

    public static final String DEFAULT_LOG_FORMAT_0 = "%1$s [%2$s.%3$s:%4$d]";
    public static final String DEFAULT_LOG_FORMAT = "%1$s: %2$s\n";

    public static LoggerService getInstance(String logName) {
        if (!instance.containsKey(logName)) {
            instance.put(logName, new LocalLoggerServiceImpl(logName, true));
        }
        return instance.get(logName);
    }

    public static LoggerService getInstance(String logName, boolean printToConsole) {
        if (!instance.containsKey(logName)) {
            instance.put(logName, new LocalLoggerServiceImpl(logName, printToConsole));
        }
        return instance.get(logName);
    }

    public LocalLoggerServiceImpl(String fileName, boolean printToConsole) {
        this.printToConsole = printToConsole;
        this.fileName = fileName;
        this.pathURI = "./logs";
        this.path = createIfNotExist(pathURI, fileName + ".log");
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    @Override
    public void log(String name, Object msg) {
        try {
            String outMsg;
            if (!(msg instanceof String)) {
                try {
                    outMsg = new Gson().toJson(msg);
                } catch (Throwable t) {
                    outMsg = msg.toString();
                }
            } else {
                outMsg = msg.toString();
            }
            String resultMsg = String.format(DEFAULT_LOG_FORMAT, name, outMsg);
            if (printToConsole) {
                System.out.println(resultMsg.trim());
            }
            write(resultMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void err(String name, Throwable t) {
        String date = dateFormat.format(new Date());
        StackTraceElement callerClass = Thread.currentThread().getStackTrace()[2];
        String className = callerClass.getClassName();
        className = className.substring(className.lastIndexOf(".") + 1);
        try {
            String resultMsg = String.format(DEFAULT_LOG_FORMAT, String.format(DEFAULT_LOG_FORMAT_0, date, className, callerClass.getMethodName(), callerClass.getLineNumber()) + ": " + name, t.getMessage());
            if (printToConsole) {
                System.out.println(resultMsg.trim());
            }
            resultMsg = String.format(DEFAULT_LOG_FORMAT, String.format(DEFAULT_LOG_FORMAT_0, date, className, callerClass.getMethodName(), callerClass.getLineNumber()) + ": " + name, ExceptionUtils.exceptionAsString(t));
            write(resultMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void log(Object msg) {
        String date = dateFormat.format(new Date());
        StackTraceElement callerClass = Thread.currentThread().getStackTrace()[2];
        String className = callerClass.getClassName();
        className = className.substring(className.lastIndexOf(".") + 1);

        log(String.format(DEFAULT_LOG_FORMAT_0, date, className, callerClass.getMethodName(), callerClass.getLineNumber()), msg);
    }

    @Override
    public void err(Throwable t) {
        String date = dateFormat.format(new Date());
        StackTraceElement callerClass = Thread.currentThread().getStackTrace()[2];
        String className = callerClass.getClassName();
        className = className.substring(className.lastIndexOf(".") + 1);
        err(String.format(DEFAULT_LOG_FORMAT_0, date, className, callerClass.getMethodName(), callerClass.getLineNumber()), t);
    }

    private Path createIfNotExist() {
        return createIfNotExist(pathURI, fileName + ".log");
    }

    private Path createIfNotExist(String pathURI, String fileName) {
        Path path = Paths.get(pathURI, fileName);
        if (!Files.exists(path)) {
            try {
                path.getParent().toFile().mkdirs();
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if(isFileTooLarge(path) || initFlag) movePreviousLogsToOld(path);
        }
        return path;
    }

    private void write(String content) throws IOException {
        if (isFileTooLarge()) movePreviousLogsToOld();
        Path path = createIfNotExist();
        Files.write(path, content.getBytes(), StandardOpenOption.APPEND);
    }

    private void movePreviousLogsToOld() {
        movePreviousLogsToOld(path);
    }

    private void movePreviousLogsToOld(Path path) {
        if (path.toFile().length() < 1) {
            initFlag = false;
            return;
        }
        String oldLogFileName = fileName + "_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".log", logFileName = fileName + ".log";
        Path oldLogFileFolder = Paths.get(pathURI + "/old");
        oldLogFileFolder.toFile().mkdirs();
        Path oldLogFilePath = Paths.get(pathURI + "/old", oldLogFileName);
        if (Files.exists(oldLogFilePath)) {
            oldLogFilePath.toFile().delete();
        }
        path.toFile().renameTo(oldLogFilePath.toFile());
        this.path = createIfNotExist(pathURI, logFileName);
    }

    private boolean isFileTooLarge() {
        return isFileTooLarge(path);
    }

    private boolean isFileTooLarge(Path path) {
        return path.toFile().length() > MAX_LOG_FILE_LENGTH;
    }
}
