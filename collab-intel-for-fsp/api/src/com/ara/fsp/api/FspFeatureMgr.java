//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.api;

import java.util.List;

public interface FspFeatureMgr {

	public void init() throws FspException;
	public boolean hasFeatureType(String name) throws FspException;
	public FspFeatureTypeId getFeatureTypeId(String featureName) throws FspException;
	public List<FspFeatureId> getFeatureIds(String typeName) throws FspException;
	public List<FspFeatureId> getFeatureIdsByType(String typeName) throws FspException;
	
	// Management of Feature Types
	
	public FspFeatureTypeId addFeatureType(String jarFile, String className) throws FspException;
	public List<FspFeatureTypeId> getFeatureTypes() throws FspException;
	public FspFeatureType getFeatureType(FspFeatureTypeId id) throws FspException;
	public FspFeature loadFeature(FspFeatureTypeId id) throws FspException;
	
	// Management of Feature Maps
	
	public FspFeatureMapId addFeatureMap(String label, FspFeatureTypeId typeId, String units) throws FspException;
	public List<FspFeatureMapId> getFeatureMaps() throws FspException;
	public FspFeatureMap getFeatureMap(FspFeatureMapId id) throws FspException;
	public List<FspFeatureMap> getFeatureMaps(FspEntityTypeId eTypeId) throws FspException;
	
	// Management of Entity Types
	
	public FspEntityTypeId addEntityType(String label) throws FspException;
	public List<FspEntityTypeId> getEntityTypes() throws FspException;
	//public FspEntityType getEntityType(FspEntityTypeId id) throws FspException;
	public void addFeatureSlot(FspEntityTypeId typeId, FspFeatureMapId mapId) throws FspException;
	public List<FspFeatureMapId> getFeatureSlots(FspEntityTypeId typeId) throws FspException;

	// Management of Entities
	public FspEntityId addEntity(FspEntityTypeId typeId, String label, String desc) throws FspException;
	public List<FspEntityId> getEntities() throws FspException;
	public FspEntity getEntity(FspEntityId id) throws FspException;
	
}
