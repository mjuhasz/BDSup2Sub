package bdsup2sub.core;

public final class LibLogger {

    private static final LibLogger INSTANCE = new LibLogger();

    private LibLogger() {
    }

    public static LibLogger getInstance() {
        return INSTANCE;
    }

    private LibLoggerObserver observer;

    public void setObserver(LibLoggerObserver observer) {
        this.observer = observer;
    }

    public void warn(String message) {
        if (observer != null) {
            observer.warn(message);
        }
    }

    public void info(String message) {
        if (observer != null) {
            observer.info(message);
        }
    }

    public void trace(String message) {
        if (observer != null) {
            observer.trace(message);
        }
    }

    public void error(String message) {
        if (observer != null) {
            observer.error(message);
        }
    }
}
