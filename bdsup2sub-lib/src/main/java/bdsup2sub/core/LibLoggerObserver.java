package bdsup2sub.core;

public interface LibLoggerObserver {

	void warn(String message);

	void info(String message);

	void trace(String message);

	void error(String message);

}
