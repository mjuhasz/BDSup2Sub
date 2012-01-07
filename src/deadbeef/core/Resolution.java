package deadbeef.core;

public enum Resolution {
	NTSC {
		@Override
		public String toString() {
			return "NTSC (720x480)";
		}

		@Override
		public String getResolutionNameForXml() {
			return "480i";
		}

		@Override
		public int[] getDimensions() {
			return new int[] {720, 480};
		}
	},
	PAL {
		@Override
		public String toString() {
			return "PAL (720x576)";
		}
		
		@Override
		public String getResolutionNameForXml() {
			return "576i";
		}

		@Override
		public int[] getDimensions() {
			return new int[]{720, 576};
		}
	},
	HD_720 {
		@Override
		public String toString() {
			return "720p (1280x720)";
		}
		
		@Override
		public String getResolutionNameForXml() {
			return "720p";
		}

		@Override
		public int[] getDimensions() {
			return new int[]{1280, 720};
		}
	},
	HD_1440x1080 {
		@Override
		public String toString() {
			return "1080p (1440x1080)";
		}
		
		@Override
		public String getResolutionNameForXml() {
			return "1440x1080";
		}

		@Override
		public int[] getDimensions() {
			return new int[]{1440, 1080};
		}
	},
	HD_1080 {
		@Override
		public String toString() {
			return "1080p (1920x1080)";
		}
		
		@Override
		public String getResolutionNameForXml() {
			return "1080p";
		}

		@Override
		public int[] getDimensions() {
			return new int[]{1920, 1080};
		}
	};
	
	public abstract String getResolutionNameForXml();
	public abstract int[] getDimensions();
}