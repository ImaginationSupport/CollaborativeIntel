//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

define( ['jquery', 'explorerBase', 'explorerNode'], ( function ( $, base, nodeModule )
{
	'use strict';

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// public properties

	var my = {};

	my.FeatureValueType =
	{
		Unknown: 0,
		Boolean: 1,
		CountableQuantity: 2,
		Probabilistic: 3,

		Text: -1,
		DateTime: -2,
	};

	my.element = null;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// private properties

	var m_base = base;
	var m_nodeModule = nodeModule;

	var m_currentNode = null;

	var m_entities = null;

	var m_accordionsTBodies = null;

	var NODE_PROPERTIES_ENTITY_NAME = '##### NODE PROPERTIES #####';

	var GLOBAL_ENTITY_NAME = 'Global';

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.initialize = function ( elementId )
	{
		my.element = $( '#' + elementId )
			.css( 'position', 'fixed' )
			.css( 'top', '0' )
			.css( 'right', '0' )
			.show();

		showNothingSelected();

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.showFeaturesForNode = function ( newNode )
	{
		if( m_currentNode === null )
		{
			if( newNode === null )
			{
				// nothing selected now, or before, so just bail
				return;
			}
			else
			{
				m_currentNode = newNode;
				switch( newNode.nodeType )
				{
					case m_nodeModule.NodeType.State:
						showFeaturesForState( newNode );
						break;

					case m_nodeModule.NodeType.ConditionEvent:
						showFeaturesForConditionEvent( newNode );
						break;

					default:
						break;
				}
			}
		}
		else
		{
			if( newNode === null )
			{
				// de-selecting, so clear things out
				m_currentNode = null;
				showNothingSelected();
			}
			else if( newNode.nodeType === m_currentNode.nodeType && newNode.id === m_currentNode.id )
			{
				// same node, just ignore
				return;
			}
			else
			{
				m_currentNode = newNode;
				switch( newNode.nodeType )
				{
					case m_nodeModule.NodeType.State:
						showFeaturesForState( newNode );
						break;

					case m_nodeModule.NodeType.ConditionEvent:
						showFeaturesForConditionEvent( newNode );
						break;

					default:
						break;
				}
			}
		}

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function showNothingSelected()
	{
		my.element.empty();

		$( '<div/>' )
			.addClass( 'message' )
			.text( 'Select a node to view features.' )
			.appendTo( my.element );

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function showDownloadingFeatures()
	{
		my.element.empty();

		$( '<div/>' )
			.addClass( 'message' )
			.text( 'Downloading features, please wait...' )
			.appendTo( my.element );

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function populateAccordions()
	{
		// if the panel is already showing an accordion, destroy that first
		if( my.element.attr( 'class' ) !== undefined && my.element.attr( 'class' ).indexOf( 'ui-accordion' ) > -1 )
		{
			my.element.accordion( 'destroy' );
		}

		my.element.empty();

		// first also show the node properties
		addEntityAccordion( NODE_PROPERTIES_ENTITY_NAME );

		// next always show global, if it exists
		if( m_entities[GLOBAL_ENTITY_NAME] !== undefined )
		{
			addEntityAccordion( GLOBAL_ENTITY_NAME );
		}

		// now show the rest, in sorted order, so first get a list of the entities so we can sort them (remove the two we've already done)
		var entityNameList = [];
		var entityName;
		for( entityName in m_entities )
		{
			if( entityName !== NODE_PROPERTIES_ENTITY_NAME && entityName !== GLOBAL_ENTITY_NAME )
			{
				entityNameList.push( entityName );
			}
		}

		// sort them
		entityNameList.sort();

		// now add the rest of the entities
		var i;
		for( i = 0; i < entityNameList.length; ++i )
		{
			addEntityAccordion( entityNameList[i] );
		}

		my.element.accordion( { collapsible: true, } );

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function showFeaturesForState( newNode )
	{
		showDownloadingFeatures();

		// download the features
		m_currentNode = newNode;
		downloadStateFeatures();

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function showFeaturesForConditionEvent( newNode )
	{
		m_entities = {};
		m_accordionsTBodies = {};

		addNodePropertiesToEntities();

		populateAccordions();

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function addEntityAccordion( entityName )
	{
		var features = m_entities[entityName].features;

		var isNodeProperties = false;
		if( entityName === NODE_PROPERTIES_ENTITY_NAME )
		{
			entityName = 'Node Properties';
			isNodeProperties = true;
		}

		$( '<h3/>' )
			.text( entityName )
			.data( 'entity-name', entityName )
			.appendTo( my.element );

		var featureContainer = $( '<div/>' )
			.addClass( 'entity-container' )
			.data( 'entity-name', entityName )
			.appendTo( my.element );
		var featureTable = $( '<table/>' )
			.appendTo( featureContainer );
		m_accordionsTBodies[entityName] = $( '<tbody/>' )
			.appendTo( featureTable );

		var i;
		for( i = 0; i < features.length; ++i )
		{
			addEntityFeatureRow( features[i], m_accordionsTBodies[entityName], false );
		}

		$( '<button/>' )
			.text( 'Save' )
			.css( 'float', 'right' )
			.addClass( 'fsp-button' )
			.data( 'entityName', entityName )
			.click( isNodeProperties ? saveNodePropertiesClick : saveAccordionClick )
			.appendTo( featureContainer );

		return featureContainer;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function addEntityFeatureRow( feature, tableTBody )
	{
		var featureRow = $( '<tr/>', { id: 'feature-row-' + feature.id } )
			.addClass( $( tableTBody ).children().length % 2 === 0 ? 'odd' : 'even' )
			.appendTo( tableTBody );

		// add the key column
		var keyCell = $( '<td/>' )
			.appendTo( featureRow );

		keyCell.text( feature.key );

		// add the value column
		var valueCell = $( '<td/>', { id: 'feature-value-cell-' + feature.id } )
			.appendTo( featureRow );

		if( feature.readOnly )
		{
			valueCell.text( feature.value );
		}
		else
		{
			valueCell.append( createInputType( feature ) );
		}

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function createInputType( feature )
	{
		var inputElement = null;

		switch( feature.valueType )
		{
			case my.FeatureValueType.Text:
				inputElement = $( '<input/>', { type: 'text', id: 'feature-value-' + feature.id } )
					.addClass( 'featureTextEntry' )
					.val( feature.value )
					.data( 'feature', feature );
				break;

			case my.FeatureValueType.DateTime:
				inputElement = $( '<input/>', { type: 'text', id: 'feature-value-' + feature.id } )
					.addClass( 'featureDateTimeEntry' )
					.val( m_base.internal.formatDateTime( feature.value, true ) )
					.data( 'feature', feature );
				break;

			case my.FeatureValueType.Boolean:
				inputElement = $( '<input/>', { type: 'checkbox', id: 'feature-value-' + feature.id, name: 'feature-value-' + feature.id } )
					.prop( 'checked', feature.value )
					.data( 'feature', feature );
				break;

			case my.FeatureValueType.CountableQuantity:
				inputElement = $( '<input/>', { type: 'text', id: 'feature-value-' + feature.id } )
					.addClass( 'featureDecimalEntry' )
					.val( feature.value.toFixed( 2 ) )
					//.keyup( decimalFieldChanged )
					//.change( decimalFieldChanged )
					.data( 'feature', feature );
				break;

			case my.FeatureValueType.Probabilistic:
				inputElement = $( '<input/>', { type: 'text', id: 'feature-value-' + feature.id } )
					.addClass( 'featureDecimalEntry' )
					.val( feature.value.toFixed( 2 ) )
					//.keyup( decimalFieldChanged )
					//.change( decimalFieldChanged )
					.data( 'feature', feature );
				break;

			default:
				console.log( 'Unknown feature value type: %d', feature.valueType );
				break;
		}

		return inputElement;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function downloadStateFeatures()
	{
		if( m_currentNode === null )
		{
			return;
		}

		m_base.internal.addLogMessage(
			'Downloading features for '
			+ ( m_currentNode.nodeType === m_nodeModule.NodeType.ConditionEvent ? 'condition event' : 'state' )
			+ ' with id '
			+ m_currentNode.id
			+ '...'
			);

		// set up the data to send in the request
		var requestData =
		{
			q: 'getFeatures',
			id: m_currentNode.id,
			// debug: true,
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
					parseStateFeatures( response.features );
				}
				else
				{
					m_base.internal.showError( 'Error downloading features: ' + response.message );
				}

				return;
			},
			error: function ( XMLHttpRequest, textStatus )
			{
				m_base.internal.showError( 'Server error downloading features: ' + textStatus );

				return;
			},
		} );

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function addNodePropertiesToEntities()
	{
		// add the node properties as a special entity
		m_entities[NODE_PROPERTIES_ENTITY_NAME] = {
			features: [],
			newEntity: false
		};

		m_entities[NODE_PROPERTIES_ENTITY_NAME].features.push( {
			id: 'NODE-PROPERTIES-id',
			key: 'Id',
			value: m_currentNode.id,
			valueType: my.FeatureValueType.Text,
			readOnly: true,
		} );
		m_entities[NODE_PROPERTIES_ENTITY_NAME].features.push( {
			id: 'NODE-PROPERTIES-label',
			key: 'Label',
			value: m_currentNode.label,
			valueType: my.FeatureValueType.Text,
			propName: 'label',
		} );
		m_entities[NODE_PROPERTIES_ENTITY_NAME].features.push( {
			id: 'NODE-PROPERTIES-description',
			key: 'Description',
			value: m_currentNode.description,
			valueType: my.FeatureValueType.Text,
			propName: 'description',
		} );

		switch( m_currentNode.nodeType )
		{
			case m_nodeModule.NodeType.State:
				m_entities[NODE_PROPERTIES_ENTITY_NAME].features.push( {
					id: 'NODE-PROPERTIES-color',
					key: 'Color',
					value: m_currentNode.color,
					valueType: my.FeatureValueType.Text,
					propName: 'color',
				} );
				m_entities[NODE_PROPERTIES_ENTITY_NAME].features.push( {
					id: 'NODE-PROPERTIES-startTime',
					key: 'Start',
					value: m_currentNode.startTime,
					valueType: my.FeatureValueType.DateTime,
					propName: 'startTime',
				} );
				m_entities[NODE_PROPERTIES_ENTITY_NAME].features.push( {
					id: 'NODE-PROPERTIES-entTime',
					key: 'End',
					value: m_currentNode.endTime,
					valueType: my.FeatureValueType.DateTime,
					propName: 'endTime',
				} );
				m_entities[NODE_PROPERTIES_ENTITY_NAME].features.push( {
					id: 'NODE-PROPERTIES-isActive',
					key: 'Active',
					value: m_currentNode.isActive,
					valueType: my.FeatureValueType.Boolean,
					propName: 'isActive',
				} );

				break;

			case m_nodeModule.NodeType.ConditionEvent:
				m_entities[NODE_PROPERTIES_ENTITY_NAME].features.push( {
					id: 'NODE-PROPERTIES-type',
					key: 'Type',
					value: m_currentNode.type,
					valueType: my.FeatureValueType.Text,
					propName: 'type',
				} );
				break;

			default:
				return;
		}

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function parseStateFeatures( features )
	{
		m_entities = {};
		m_accordionsTBodies = {};

		addNodePropertiesToEntities();

		// now parse the rest of the features normally
		var i;
		for( i = 0; i < features.length; ++i )
		{
			if( m_entities[features[i].entityName] === undefined )
			{
				m_entities[features[i].entityName] = {
					features: [],
					newEntity: false
				};
			}

			features[i].value = parseFeatureValue( features[i].valueType, features[i].value );

			// add this feature to this entity
			m_entities[features[i].entityName].features.push( features[i] );
		}

		m_base.internal.addLogMessage( 'Loaded ' + features.length + ' features.' );

		populateAccordions();

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function parseFeatureValue( valueType, rawValue )
	{
		// parse the value to a data type
		switch( valueType )
		{
			case my.FeatureValueType.Text:
				return rawValue;

			case my.FeatureValueType.DateTime:
				return m_base.internal.parseDateTime( rawValue );

			case my.FeatureValueType.Boolean:
				return rawValue === 'true';

			case my.FeatureValueType.CountableQuantity:
				return parseFloat( rawValue );

			case my.FeatureValueType.Probabilistic:
				return parseFloat( rawValue );

			default:
				return null;
		}

		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function updateFeatureIds( addedFeatures )
	{
		var oldElements;
		var originalElementId;
		var updatedElementId;

		var addedFeatureIndex;
		var i;
		var oldIdAsString;
		for( addedFeatureIndex = 0; addedFeatureIndex < addedFeatures.length; ++addedFeatureIndex )
		{
			oldIdAsString = addedFeatures[addedFeatureIndex].originalId.toString();

			// update the DOM elements
			oldElements = $( "[id$='-" + addedFeatures[addedFeatureIndex].originalId + "']" );
			for( i = 0; i < oldElements.length; ++i )
			{
				originalElementId = $( oldElements[i] ).prop( 'id' );
				updatedElementId = originalElementId.substr( 0, originalElementId.length - oldIdAsString.length ) + addedFeatures[addedFeatureIndex].newId;

				$( oldElements[i] ).prop( 'id', updatedElementId );
			}

			// update the parsed entities
			for( i = 0; i < m_entities[addedFeatures[addedFeatureIndex].entityName].features.length; ++i )
			{
				if( m_entities[addedFeatures[addedFeatureIndex].entityName].features[i].id === addedFeatures[addedFeatureIndex].originalId )
				{
					m_entities[addedFeatures[addedFeatureIndex].entityName].features[i].id = addedFeatures[addedFeatureIndex].newId;
				}
			}
		}

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function saveAccordionClick( event )
	{
		var entityName = $( event.target ).data( 'entityName' );

		// set up the data to send in the request
		var requestData =
		{
			q: 'saveFeatures',
			nodeType: m_currentNode.nodeType,
			//id: m_currentNode.id,
			featuresJSON: null,
			// debug: true,
		};

		// if this is a new entity, save the name here
		var newEntityName;
		if( m_entities[entityName].newEntity )
		{
			// TODO -- verify the new name is not an existing name!

			// replace the temporary entity name with the one the user typed
			newEntityName = $( '#' + m_entities[entityName].entityNameTextboxId ).val();

			// if the entity was renamed, rename it in the lookup table
			if( newEntityName !== entityName )
			{
				m_entities[newEntityName] = m_entities[entityName];

				delete m_entities[entityName];

				entityName = newEntityName;
			}
		}

		var i;
		var newValue;
		var changedFeature;
		var changesFound;

		var changedFeatures = [];

		for( i = 0; i < m_entities[entityName].features.length; ++i )
		{
			changedFeature = { id: m_entities[entityName].features[i].id };
			changesFound = false;

			// get the current value of the feature
			if( m_entities[entityName].features[i].valueType === my.FeatureValueType.Boolean )
			{
				newValue = $( '#feature-value-' + m_entities[entityName].features[i].id ).prop( 'checked' );
			}
			else
			{
				newValue = parseFeatureValue(
					m_entities[entityName].features[i].valueType,
					$( '#feature-value-' + m_entities[entityName].features[i].id ).val()
					);
			}

			if( m_entities[entityName].features[i].id < 0 )
			{
				// new feature

				changedFeature.key = $( '#feature-key-' + m_entities[entityName].features[i].id ).val();
				changedFeature.valueType = parseInt( $( '#feature-valueType-' + m_entities[entityName].features[i].id ).val() );
				changedFeature.value = newValue;
				changedFeature.stateId = m_currentNode.id;
				changedFeature.entityName = entityName;
				changesFound = true;

				// mark the type as disabled now that it's going to be saved
				$( '#feature-valueType-' + m_entities[entityName].features[i].id ).prop( 'disabled', true );
			}
			else
			{
				// feature already exists, so check if the value has changed

				if( !checkFeatureValueChanged( m_entities[entityName].features[i], newValue ) )
				{
					changedFeature.value = newValue;
					changesFound = true;
				}
			}

			if( changesFound )
			{
				// if it was a DateTime, format appropriately
				if( m_entities[entityName].features[i].valueType === my.FeatureValueType.DateTime )
				{
					changedFeature.value = m_base.internal.formatDateTime( changedFeature.value, true ).toString();
				}
				else
				{
					changedFeature.value = changedFeature.value.toString();
				}

				// add it to the list of changed features
				changedFeatures.push( changedFeature );

				// also update the cached value
				m_entities[entityName].features[i].value = newValue;
			}
		}

		if( changedFeatures.length === 0 )
		{
			m_base.internal.addLogMessage( 'No changes found to save.' );
			return;
		}

		m_base.internal.addLogMessage( 'Saving ' + changedFeatures.length + ' changed feature' + ( changedFeatures.length === 1 ? '' : 's' ) + '...' );
		requestData.featuresJSON = JSON.stringify( changedFeatures );

		// kick off the request
		$.ajax( {
			url: 'explorer-backend',
			type: 'POST',
			data: requestData,
			success: function ( response )
			{
				if( response.success )
				{
					m_base.internal.addLogMessage( 'Features saved.' );
				}
				else
				{
					m_base.internal.showError( 'Error saving changes: ' + response.message );
				}

				return;
			},
			error: function ( XMLHttpRequest, textStatus )
			{
				m_base.internal.showError( 'Server error saving changes: ' + textStatus );

				return;
			},
		} );

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function saveNodePropertiesClick( event )
	{
		var changesFound = false;

		var i;
		var newValue;
		for( i = 0; i < m_entities[NODE_PROPERTIES_ENTITY_NAME].features.length; ++i )
		{
			if( !m_entities[NODE_PROPERTIES_ENTITY_NAME].features[i].readOnly )
			{
				if( m_entities[NODE_PROPERTIES_ENTITY_NAME].features[i].valueType === my.FeatureValueType.Boolean )
				{
					newValue = $( '#feature-value-' + m_entities[NODE_PROPERTIES_ENTITY_NAME].features[i].id ).prop( 'checked' );
				}
				else
				{
					newValue = parseFeatureValue(
						m_entities[NODE_PROPERTIES_ENTITY_NAME].features[i].valueType,
						$( '#feature-value-' + m_entities[NODE_PROPERTIES_ENTITY_NAME].features[i].id ).val()
						);
				}

				m_currentNode[m_entities[NODE_PROPERTIES_ENTITY_NAME].features[i].propName] = newValue;

				// but check if there are changes and save that separately
				if( !checkFeatureValueChanged( m_entities[NODE_PROPERTIES_ENTITY_NAME].features[i], newValue ) )
				{
					changesFound = true;
				}
			}
		}

		if( !changesFound )
		{
			m_base.internal.addLogMessage( 'No changes found to save.' );
			return;
		}

		m_base.internal.saveNodeProperties( m_currentNode, true );

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function checkFeatureValueChanged( feature, newValue )
	{
		if( feature.valueType === my.FeatureValueType.DateTime )
		{
			return feature.value.getTime() === newValue.getTime();
		}
		else
		{
			return feature.value === newValue;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	return my;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

} ) );

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
