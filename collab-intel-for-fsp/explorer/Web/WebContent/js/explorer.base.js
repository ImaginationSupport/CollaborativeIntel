//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

define( ['require', 'jquery'], ( function ( require, $ )
{
	'use strict';

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// public

	var my = {};

	// holds the sizes of the elements
	my.featuresPanelWidth = 400;
	my.previewHeight = 100;
	my.previewWidth = 300;

	my.DisplayMode = {
		Unknown: 0,
		ShapeOnly: 1,
		ShapeWithTitle: 2,
		FullDetails: 3,
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// protected

	my.internal = {};

	my.internal.selectedNode = null;
	my.internal.hoveredNode = null;

	my.internal.selectedMissionStateId = 1;

	my.internal.rootState = null;
	my.internal.stateLookup = null;
	my.internal.conditionEventLookup = null;

	my.internal.totalStates = 0;

	my.internal.dataBounds = null;

	my.internal.showInactiveNodes = false;

	my.internal.missions = [];
	
	my.internal.dateTimeParser = new RegExp( '^(\\d{4})-(\\d{1,2})-(\\d{1,2})( (\\d{1,2}):(\\d{2}):(\\d{2}))?$' );

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// private

	var m_nodeModule = null;
	var m_stateModule = null;
	var m_conditionEventModule = null;
	var m_featuresPanelModule = null;
	var m_statusWindowModule = null;
	var m_mainCanvasModule = null;
	var m_previewCanvasModule = null;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.initialize = function ( mainCanvasId, previewCanvasId, featuresPanelId, statusWindowId )
	{
		m_nodeModule = require( 'explorerNode' );
		m_stateModule = require( 'explorerState' );
		m_conditionEventModule = require( 'explorerConditionEvent' );
		m_featuresPanelModule = require( 'explorerFeaturesPanel' );
		m_statusWindowModule = require( 'explorerStatusWindow' );
		m_mainCanvasModule = require( 'explorerMainCanvas' );
		m_previewCanvasModule = require( 'explorerPreviewCanvas' );

		m_featuresPanelModule.initialize( featuresPanelId );
		m_statusWindowModule.initialize( statusWindowId );
		m_mainCanvasModule.initialize( mainCanvasId );
		m_previewCanvasModule.initialize( previewCanvasId );

		m_mainCanvasModule.selectedNodeChangedCallback = my.internal.selectedNodeChanged;

		// resize the HTML elements
		resizeElements();

		// reset the views and redraw the canvases
		m_mainCanvasModule.resetView();
		m_previewCanvasModule.resetView();

		// set up the callback to resize everything when the window is resized
		$( window ).resize( resizeElements );

		// see if the root id was given
		var rootStateIdMatcher = new RegExp( 'id=(\\d+)', 'g' );
		var match = rootStateIdMatcher.exec( window.location.search );
		if( match !== null )
			my.internal.selectedMissionStateId = parseInt( match[1], 10 );

		// kick off the downloads to fetch the data from the server
		my.internal.switchMissions( my.internal.selectedMissionStateId );

		// finally kick off the download for the missions
		downloadMissions();

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.redrawAll = function ()
	{
		m_mainCanvasModule.redraw();
		m_previewCanvasModule.redraw();

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.showInactiveNodes = function ( showInactiveNodes )
	{
		// if there is no change, just bail
		if( showInactiveNodes === my.internal.showInactiveNodes )
			return;

		// set the new value
		my.internal.showInactiveNodes = showInactiveNodes;

		my.internal.treeDataChanged( null, false );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.addNode = function ( parentNodeType, parentId )
	{
		// find the parent node
		var parentNode = my.internal.rootState.findNodeWithId( parentNodeType, parentId );

		// if it couldn't be found, just bail
		if( parentNode === null )
		{
			m_statusWindowModule.addLogMessage( 'Could not find the parent node to add a child!' );
			return;
		}

		// set up the data to send in the request
		var requestData =
		{
			q: 'addNode',
		};

		var sourceData = null;
		var newNode = null;

		switch( parentNodeType )
		{
			case m_nodeModule.NodeType.State:

				m_statusWindowModule.addLogMessage( 'Adding child condition event...' );

				// set up the data for the constructor
				sourceData = {};
				sourceData.id = -1;
				sourceData.label = 'New condition event';
				sourceData.description = '';
				sourceData.active = true;
				sourceData.color = '255,0,0';
				sourceData.timeAt = my.internal.formatDateTime(
					new Date( parentNode.endTime.getTime() + ( parentNode.endTime.getTime() - parentNode.startTime.getTime() ) ),
					true
					);
				sourceData.stateId = parentNode.id;

				// create the node
				newNode = new m_conditionEventModule.ExplorerConditionEvent( sourceData );

				// now copy that into the request to send to the server
				requestData.nodeType = m_nodeModule.NodeType.ConditionEvent;
				requestData.label = sourceData.label;
				requestData.description = sourceData.description;
				requestData.active = sourceData.active;
				requestData.color = sourceData.color;
				requestData.timeAt = sourceData.timeAt;
				requestData.stateId = sourceData.stateId;

				break;

			case m_nodeModule.NodeType.ConditionEvent:

				m_statusWindowModule.addLogMessage( 'Adding child state...' );

				// set up the data for the constructor
				sourceData = {};
				sourceData.id = -1;
				sourceData.label = 'New state';
				sourceData.description = '';
				sourceData.active = true;
				sourceData.color = '255,0,0';
				sourceData.start = my.internal.formatDateTime(
					new Date( parentNode.timeAt.getTime() + 1000 * 60 * 60 * 24 * 7 ),
					true
					);
				sourceData.end = my.internal.formatDateTime(
					new Date( parentNode.timeAt.getTime() + 1000 * 60 * 60 * 24 * 14 ),
					true
					);
				sourceData.conditionEventId = parentNode.id;

				// create the node
				newNode = new m_stateModule.ExplorerState( sourceData );

				// now copy that into the request to send to the server
				requestData.nodeType = m_nodeModule.NodeType.State;
				requestData.label = sourceData.label;
				requestData.description = sourceData.description;
				requestData.active = sourceData.active;
				requestData.color = sourceData.color;
				requestData.start = sourceData.start;
				requestData.end = sourceData.end;
				requestData.conditionEventId = sourceData.conditionEventId;

				break;

			default:
				return;
		}

		// kick off the request
		$.ajax( {
			url: 'explorer-backend',
			type: 'POST',
			data: requestData,
			newNode: newNode,
			beforeSend: function ( jqXHR, settings )
			{
				jqXHR.newNode = settings.newNode;

				return;
			},
			success: function ( response, responseRaw, jqXHR )
			{
				if( response.success )
				{
					my.internal.addLogMessage( 'Node added.' );
					jqXHR.newNode.id = response.newNodeId;

					switch( jqXHR.newNode.nodeType )
					{
						case m_nodeModule.NodeType.State:
							my.internal.stateLookup['s-' + response.newNodeId] = jqXHR.newNode;
							break;

						case m_nodeModule.NodeType.ConditionEvent:
							my.internal.conditionEventLookup['ce-' + response.newNodeId] = jqXHR.newNode;
							break;

						default:
							return;
					}

					my.internal.treeDataChanged( jqXHR.newNode, false );
				}
				else
				{
					my.internal.showError( 'Error deleting node: ' + response.message );
				}

				return;
			},
			error: function ( XMLHttpRequest, textStatus )
			{
				my.internal.showError( 'Server error deleting node: ' + textStatus );

				return;
			},
		} );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.deleteNode = function ( nodeType, id )
	{
		// find the node to delete
		var node = my.internal.rootState.findNodeWithId( nodeType, id );

		// if it couldn't be found, just bail
		if( node === null )
		{
			m_statusWindowModule.addLogMessage( 'Could not find the node to delete!' );
			return;
		}

		// show the status message
		m_statusWindowModule.addLogMessage(
			'Deleting '
			+ ( nodeType === m_nodeModule.NodeType.State ? 'State' : 'Condition Event' )
			+ ' '
			+ id
			+ ' "'
			+ node.label
			+ '" ...'
			);

		// set this as the selected node
		my.internal.hoveredNode = null;
		my.internal.selectedNode = null;
		my.internal.selectedNodeChanged();

		// delete the node and all its children from the lookups
		deleteNodeHelper( node );

		// rebuild and redraw the tree
		my.internal.treeDataChanged( null, false );

		// set up the data to send in the request
		var requestData = {
			q: 'deleteNode',
			nodeType: nodeType,
			id: id,
			//debug: true,
		};

		// kick off the request
		$.ajax( {
			url: 'explorer-backend',
			type: 'POST',
			data: requestData,
			success: function ( response )
			{
				if( response.success )
				{
					my.internal.addLogMessage( 'Node deleted.' );
				}
				else
				{
					my.internal.showError( 'Error deleting node: ' + response.message );
				}

				return;
			},
			error: function ( XMLHttpRequest, textStatus )
			{
				my.internal.showError( 'Server error deleting node: ' + textStatus );

				return;
			},
		} );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function deleteNodeHelper( node )
	{
		var i;
		for( i = 0; i < node.childNodes.length; ++i )
		{
			deleteNodeHelper( node.childNodes[i] );
		}

		// mark the internal node as deleted
		var lookupKey;
		switch( node.nodeType )
		{
			case m_nodeModule.NodeType.State:
				lookupKey = 's-' + node.id;
				if( my.internal.stateLookup[lookupKey] === undefined )
				{
					return;
				}

				delete my.internal.stateLookup[lookupKey];
				break;

			case m_nodeModule.NodeType.ConditionEvent:
				lookupKey = 'ce-' + node.id;
				if( my.internal.conditionEventLookup[lookupKey] === undefined )
				{
					return;
				}

				delete my.internal.conditionEventLookup[lookupKey];
				break;

			default:
				break;
		}

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.treeDataChanged = function ( newSelectedNode, updateDataBounds )
	{
		// set these to null to make sure it isn't a node that's now hidden
		my.internal.selectedNode = newSelectedNode === undefined ? null : newSelectedNode;
		my.internal.hoveredNode = null;

		// rebuild the tree
		my.internal.rebuildTree( updateDataBounds );

		// redraw the canvases
		my.redrawAll();

		// set this as the selected node
		my.internal.selectedNodeChanged();

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.rebuildTree = function ( updateDataBounds )
	{
		var stateKey = null;
		var conditionEventKey = null;

		// first remove all of the current links...

		for( stateKey in my.internal.stateLookup )
		{
			my.internal.stateLookup[stateKey].parentNode = null;
			my.internal.stateLookup[stateKey].childNodes = [];
		}

		for( conditionEventKey in my.internal.conditionEventLookup )
		{
			my.internal.conditionEventLookup[conditionEventKey].parentNode = null;
			my.internal.conditionEventLookup[conditionEventKey].childNodes = [];
		}

		// now add them all back...
		for( stateKey in my.internal.stateLookup )
		{
			if( my.internal.stateLookup[stateKey].conditionEventId !== -1 )
			{
				conditionEventKey = 'ce-' + my.internal.stateLookup[stateKey].conditionEventId;
				my.internal.stateLookup[stateKey].parentNode = my.internal.conditionEventLookup[conditionEventKey];
				my.internal.stateLookup[stateKey].parentNode.childNodes.push( my.internal.stateLookup[stateKey] );

				my.internal.stateLookup[stateKey].parentNode.childNodes.sort( my.internal.compareStates );
			}

		}

		for( conditionEventKey in my.internal.conditionEventLookup )
		{
			stateKey = 's-' + my.internal.conditionEventLookup[conditionEventKey].stateId;
			my.internal.conditionEventLookup[conditionEventKey].parentNode = my.internal.stateLookup[stateKey];
			my.internal.conditionEventLookup[conditionEventKey].parentNode.childNodes.push( my.internal.conditionEventLookup[conditionEventKey] );

			my.internal.conditionEventLookup[conditionEventKey].parentNode.childNodes.sort( my.internal.compareConditionEvents );
		}

		// TODO check for unlinked nodes

		if( updateDataBounds )
		{
			// calculate the bounds of the active data
			my.internal.dataBounds = my.internal.rootState.findDataBounds();

			// now that we know the start and end times, figure out the scaling and reset the view
			m_mainCanvasModule.resetView();
		}

		// now that we know the 1.0 zoom scaling, we can figure out the sizes
		my.internal.rootState.calculateSize( m_mainCanvasModule );

		// and now that we know the sizes, we can figure out the locations
		my.internal.rootState.calculateChildStateLocations();

		// re-sort the children based on time
		// TODO: sort!

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.showError = function ( message )
	{
		// TODO make this a lot prettier...
		alert( message );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.addLogMessage = function ( message )
	{
		m_statusWindowModule.addLogMessage( message );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.switchMissions = function ( rootStateId )
	{
		m_statusWindowModule.addLogMessage( 'Downloading state space...' );

		my.internal.selectedMissionStateId = rootStateId;

		// set up the data to send in the request
		var requestData = {
			q: 'getStateSpace',
			id: rootStateId,
			//debug: true,
		};

		// kick off the request
		$.ajax( {
			url: 'explorer-backend',
			type: 'GET',
			data: requestData,
			success: function ( response )
			{
				if( response.success )
				{
					parseStateSpaceData( response.states, response.conditionEvents );
				}
				else
				{
					my.internal.showError( 'Error downloading state space: ' + response.message );
				}

				return;
			},
			error: function ( XMLHttpRequest, textStatus )
			{
				my.internal.showError( 'Server error downloading states space: ' + textStatus );

				return;
			},
		} );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.selectedNodeChanged = function ()
	{
		m_featuresPanelModule.showFeaturesForNode( my.internal.selectedNode );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.saveNodeProperties = function ( node, setNodeAsActive )
	{
		var requestData = {
			q: 'saveNodeProperties',
			nodeType: node.nodeType,
			id: node.id,
			//debug: true,
		};

		requestData.label = node.label;
		requestData.description = node.description;
		requestData.active = node.isActive;
		requestData.p = -1; // TODO fix!

		switch( node.nodeType )
		{
			case m_nodeModule.NodeType.State:
				requestData.start = my.internal.formatDateTime( node.startTime, true );
				requestData.end = my.internal.formatDateTime( node.endTime, true );
				requestData.color = node.color;
				break;

			case m_nodeModule.NodeType.ConditionEvent:
				requestData.type = node.type;
				break;

			default:
				return;
		}

		// kick off the request
		$.ajax( {
			url: 'explorer-backend',
			type: 'POST',
			data: requestData,
			success: function ( response )
			{
				if( response.success )
				{
					my.internal.addLogMessage( 'Node properties saved.' );

					my.internal.treeDataChanged( setNodeAsActive && ( node.isActive || my.internal.showInactiveNodes ) ? node : null, false );
				}
				else
				{
					my.internal.showError( 'Error saving changes: ' + response.message );
				}

				return;
			},
			error: function ( XMLHttpRequest, textStatus )
			{
				my.internal.showError( 'Server error saving changes: ' + textStatus );

				return;
			},
		} );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.formatDateTime = function ( dateTime, showTime )
	{
		if( dateTime === undefined || dateTime === null )
		{
			return '';
		}

		// start with the year
		var formatted = '' + dateTime.getFullYear().toString() + '-';

		// months
		if( dateTime.getMonth() < 9 )
		{
			formatted += '0';
		}
		formatted += ( dateTime.getMonth() + 1 ) + '-';

		// month day
		if( dateTime.getDate() < 10 )
		{
			formatted += '0';
		}
		formatted += dateTime.getDate();

		if( showTime )
		{
			formatted += ' ';

			// hour
			if( dateTime.getHours() < 10 )
			{
				formatted += '0';
			}
			formatted += dateTime.getHours() + ':';

			// minute
			if( dateTime.getMinutes() < 10 )
			{
				formatted += '0';
			}
			formatted += dateTime.getMinutes() + ':';

			// seconds
			if( dateTime.getSeconds() < 10 )
			{
				formatted += '0';
			}
			formatted += dateTime.getSeconds();
		}

		return formatted;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.parseDateTime = function ( dateTimeString )
	{
		if( dateTimeString === null || dateTimeString === undefined )
		{
			return null;
		}
		
		var match = my.internal.dateTimeParser.exec( dateTimeString );
		if( match !== null )
		{
			return new Date(
				parseInt( match[1], 10 ),
				parseInt( match[2] - 1, 10 ),
				parseInt( match[3], 10 ),
				parseInt( match[4], 10 ),
				parseInt( match[5], 10 ),
				parseInt( match[6], 10 ),
				0
				);
		}
		else
		{
			return null;
		}
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.debugDrawDot = function ( canvasModule, x, y, color )
	{
		canvasModule.canvasContext.save();

		canvasModule.canvasContext.fillStyle = color;
		canvasModule.canvasContext.beginPath();
		canvasModule.canvasContext.arc( x, y, 3, 0, 2 * Math.PI );
		canvasModule.canvasContext.fill();

		canvasModule.canvasContext.restore();

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function downloadMissions()
	{
		m_statusWindowModule.addLogMessage( 'Downloading missions...' );

		// kick off the request
		$.ajax( {
			url: 'explorer-backend',
			type: 'GET',
			data: {
				q: 'getMissions'
			},
			success: function ( response )
			{
				if( response.success )
				{
					my.internal.missions = response.missions;

					m_statusWindowModule.addLogMessage( 'Loaded ' + my.internal.missions.length + ' mission' + ( my.internal.missions.length === 1 ? '' : 's' ) + '.' );
				}
				else
				{
					my.internal.showError( 'Error downloading missions: ' + response.message );
				}

				return;
			},
			error: function ( XMLHttpRequest, textStatus )
			{
				my.internal.showError( 'Server error downloading missions: ' + textStatus );

				return;
			},
		} );

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function parseStateSpaceData( states, conditionEvents )
	{
		if( states === undefined || states === null || conditionEvents === undefined || conditionEvents === null )
		{
			return;
		}

		// reset any data that already existed
		my.internal.rootState = null;
		my.internal.stateLookup = {};
		my.internal.conditionEventLookup = {};
		my.internal.selectedNode = null;
		my.internal.hoveredNode = null;

		var i;

		for( i = 0; i < states.length; ++i )
		{
			my.internal.stateLookup['s-' + states[i].id] = new m_stateModule.ExplorerState( states[i] );
		}

		for( i = 0; i < conditionEvents.length; ++i )
		{
			my.internal.conditionEventLookup['ce-' + conditionEvents[i].id] = new m_conditionEventModule.ExplorerConditionEvent( conditionEvents[i] );
		}

		// set the root state
		my.internal.rootState = my.internal.stateLookup['s-' + my.internal.selectedMissionStateId];

		// make sure we found the root node
		if( my.internal.rootState === undefined )
		{
			my.internal.showError( 'Root state ' + my.internal.selectedMissionStateId + ' does not exist in the state space.' );
			return;
		}

		// rebuild the tree
		my.internal.treeDataChanged( null, true );

		// show the user we're done loading the state space
		m_statusWindowModule.addLogMessage( 'State space loaded: ' + states.length + ' states, ' + conditionEvents.length + ' condition events' );

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function resizeElements()
	{
		var windowWidth = $( window ).width();
		var windowHeight = $( window ).height();

		m_mainCanvasModule.element
			.css( 'width', ( windowWidth - my.featuresPanelWidth ).toString() + 'px' )
			.css( 'height', ( windowHeight - my.previewHeight ).toString() + 'px' );

		m_previewCanvasModule.element
			.css( 'width', my.previewWidth.toString() + 'px' )
			.css( 'height', my.previewHeight.toString() + 'px' );

		m_featuresPanelModule.element
			.css( 'left', ( windowWidth - my.featuresPanelWidth ).toString() + 'px' )
			.css( 'height', ( windowHeight - my.previewHeight ).toString() + 'px' );

		m_statusWindowModule.element
			.css( 'left', my.previewWidth.toString() + 'px' )
			.css( 'height', my.previewHeight.toString() + 'px' );

		// store the main canvas setup
		m_mainCanvasModule.canvasWidth = m_mainCanvasModule.element[0].clientWidth;
		m_mainCanvasModule.canvasHeight = m_mainCanvasModule.element[0].clientHeight;
		m_mainCanvasModule.element[0].width = m_mainCanvasModule.canvasWidth;
		m_mainCanvasModule.element[0].height = m_mainCanvasModule.canvasHeight;

		// store the preview canvas setup
		m_previewCanvasModule.canvasWidth = m_previewCanvasModule.element[0].clientWidth;
		m_previewCanvasModule.canvasHeight = m_previewCanvasModule.element[0].clientHeight;
		m_previewCanvasModule.element[0].width = m_previewCanvasModule.canvasWidth;
		m_previewCanvasModule.element[0].height = m_previewCanvasModule.canvasHeight;

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.compareStates = function ( a, b )
	{
		if( a.startTime.getTime() === b.startTime.getTime() )
		{
			if( a.label === b.label )
			{
				return 0
			}
			else if( a.label < b.label )
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
		else
		{
			if( a.startTime.getTime() < b.startTime.getTime() )
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.compareConditionEvents = function ( a, b )
	{
		if( a.timeAt.getTime() === b.timeAt.getTime() )
		{
			if( a.label === b.label )
			{
				return 0
			}
			else if( a.label < b.label )
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
		else
		{
			if( a.timeAt.getTime() < b.timeAt.getTime() )
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.ContextMenuItem = function ( label, iconUri, disabled, action, data )
	{
		this.label = label === undefined ? 'Menu item' : label;
		this.iconUri = iconUri === undefined ? null : iconUri;
		this.disabled = disabled === undefined ? true : disabled;
		this.action = action === undefined ? null : action;
		this.data = data === undefined ? null : data;

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.internal.ContextMenuItemSeparator = function ()
	{
		this.label = null;
		this.iconUri = null;
		this.disabled = null;
		this.action = null;
		this.data = null;

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	Function.prototype.setBaseClass = function ( baseClass )
	{
		this.prototype = new baseClass();
		this.prototype.constructor = this;
		this.prototype.parent = baseClass.prototype;

		return this;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	return my;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

} ) );

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
