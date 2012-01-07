package deadbeef.core;

public enum OutputMode {
	/** DVD SUB/IDX (VobSub) stream */
	VOBSUB {
		@Override
		public String toString() {
			return "SUB/IDX";
		}
	},
	/** DVD SUP/IFO stream */
	SUPIFO {
		@Override
		public String toString() {
			return "SUP/IFO";
		}
	},
	/** Blu-Ray SUP stream */
	BDSUP {
		@Override
		public String toString() {
			return "SUP(BD)";
		}
	},
	/** Sony BDN XML (+PNGs) */
	XML {
		@Override
		public String toString() {
			return "XML/PNG";
		}
	},
}
