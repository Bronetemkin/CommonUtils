package ru.hnm1nd.base.utils.log;

public interface LoggerService {

    void log(String name, Object msg);

    void err(String name, Throwable t);

    void log(Object msg);

    void err(Throwable t);

}
