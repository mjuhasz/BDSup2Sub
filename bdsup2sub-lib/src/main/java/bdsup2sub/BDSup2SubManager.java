package bdsup2sub;

public interface BDSup2SubManager {

	boolean usesBT601();

	void setProgress(long bufferSize);

	boolean isCanceled();

	void setProgressMax(int n);

}
