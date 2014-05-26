package bdsup2sub;

public class BDSup2SubManagerBase implements BDSup2SubManager {
    @Override
    public boolean usesBT601() {
        return false;
    }

    @Override
    public void setProgress(long bufferSize) {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void setProgressMax(int n) {

    }
}
