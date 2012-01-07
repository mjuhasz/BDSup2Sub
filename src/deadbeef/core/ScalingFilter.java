package deadbeef.core;

public enum ScalingFilter {
	BILINEAR {
		@Override
		public String toString() {
			return "Bilinear";
		}
	},
	TRIANGLE {
		@Override
		public String toString() {
			return "Triangle";
		}
	},
	BICUBIC {
		@Override
		public String toString() {
			return "Bicubic";
		}
	},
	BELL {
		@Override
		public String toString() {
			return "Bell";
		}
	},
	BICUBIC_SPLINE {
		@Override
		public String toString() {
			return "Bicubic-Spline";
		}
	},
	HERMITE {
		@Override
		public String toString() {
			return "Hermite";
		}
	},
	LANCZOS3 {
		@Override
		public String toString() {
			return "Lanczos3";
		}
	},
	MITCHELL {
		@Override
		public String toString() {
			return "Mitchell";
		}
	},
}