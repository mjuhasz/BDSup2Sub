package bdsup2sub.core;

import bdsup2sub.BDSup2SubManager;

final class BDSup2SubCoreManager implements BDSup2SubManager {

	static {
		INSTANCE = new BDSup2SubCoreManager();
	}
	
	private BDSup2SubCoreManager() {
		
	}
	
	private static BDSup2SubCoreManager INSTANCE;
	
	public static BDSup2SubManager getInstance() {
		return INSTANCE;
	}
	
	@Override
	public boolean usesBT601() {
		return Core.usesBT601();
	}

	@Override
	public void setProgress(long p) {
		Core.setProgress(p);
	}

	@Override
	public boolean isCanceled() {
		return Core.isCanceled();
	}

	@Override
	public void setProgressMax(int n) {
		Core.setProgressMax(n);
	}

}
