//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.api;

import java.util.Date;
import java.util.List;

public interface FspStateMgr {
	public void init() throws FspException;
	
	// Management of States
	
	public boolean hasState(FspStateId stateId) throws FspException;
	public FspState getState(FspStateId stateId) throws FspException;
	public FspEdge addState(FspState prev, Date start, Date end, String edgeLabel, String edgeDesc) throws FspException;
	public FspStateId addState(Date start, Date end) throws FspException;

	public FspEdge cloneState(FspStateId stateId) throws FspException;
	public FspStateId getStateIdForFeature(FspFeatureId id) throws FspException;
	public FspEntityId getEntityIdForFeature(FspFeatureId id) throws FspException;
	public FspFeatureMapId getFeatureMapIdForFeature(FspFeatureId id) throws FspException;
	
	// Management of Edges
	
	public List<FspEdgeId> getNextEdges(FspStateId state) throws FspException;
	public FspEdgeId getPrevEdge(FspStateId state) throws FspException;
	public FspEdge getEdge(FspEdgeId edgeId) throws FspException;
	
	public List<FspConditionOption> getConditions(FspEdgeId edge) throws FspException;
	public FspCondition addCondition(FspConditionType type, String label, String desc) throws FspException;		
	public FspConditionOption addConditionOption(FspConditionOption option, FspEdgeId edge) throws FspException;
	
	// Management of Mission Spaces
	
	public FspMission createMission(String label, String desc, Date start, Date horizon);
	public FspMissionId addMission(String label, String desc, FspStateId stateId, Date horizon) throws FspException;
	public List<FspMissionId> getMissions() throws FspException;
	public FspMission getMission(FspMissionId id) throws FspException;	
	
	// Management of Projector Types
	
	public FspProjectorTypeId addProjectorType(String jarFile, String className, String label, String desc) throws FspException;
	public List<FspProjectorTypeId> getProjectorTypes() throws FspException;
	public FspProjectorType getProjectorType(FspProjectorTypeId id) throws FspException;
	
	public FspProjector getAssignedProjector(FspFeatureMapId featureMapId) throws FspException;

	
	// Special State Space Operators
	
	public boolean hasFeature(FspStateId stateId, FspEntityId entityId, FspFeatureMapId fmap) throws FspException;
	public FspFeature getFeature(FspStateId stateId, FspEntityId entityId, FspFeatureMapId fmap) throws FspException;
	public FspFeature getFeature(FspFeatureId featureId) throws FspException;
	public FspFeatureId addFeature(FspStateId stateId, FspEntityId entityId, FspFeatureMapId fmap, FspFeature feature) throws FspException;
	public void updateFeature(FspFeatureId featureId, FspFeature feature) throws FspException;
	
	
	public void walkStateSpace(FspState state, FspStateSpaceForwardWalker walker) throws FspException;
	public void walkStateSpace(FspState state, FspStateSpaceBackwardWalker walker) throws FspException;




	



	
}
