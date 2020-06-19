//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime.mysql;

import com.ara.fsp.api.*;

public class FeatureMap implements FspFeatureMap {
		protected FspFeatureMapId id;
		protected String label;
		protected FspFeatureType type;
		protected String units;
		
		protected FeatureMap(FspFeatureMapId id, String label, FspFeatureType type, String units){
			this.id=id;
			this.label=label;
			this.type=type;
			this.units=units;
		}

		public FspFeatureMapId getId() {
			return id;
		}

		protected void setId(FspFeatureMapId id) {
			this.id = id;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
		
		public FspFeatureType getType() {
			return type;
		}

		public void setType(FspFeatureType type) {
			this.type = type;
		}
		
		public String getUnits() {
			return units;
		}

		public void setUnits(String units) {
			this.units = units;
		}

		@Override
		public boolean save() {
			// TODO Auto-generated method stub
			return false;
		}
}
