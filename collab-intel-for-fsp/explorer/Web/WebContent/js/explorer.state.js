//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

define( ['jquery', 'explorerBase', 'explorerNode'], ( function ( $, base, node )
{
	'use strict';

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// public properties

	var my = {};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// private properties

	var m_base = base;
	var m_node = node;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerState = function ( sourceData )
	{
		this.nodeType = m_node.NodeType.State;
		this.id = sourceData.id;
		this.label = sourceData.label;
		this.description = sourceData.description;
		this.isActive = sourceData.active;
		this.color = sourceData.color;
		this.startTime = new Date( sourceData.start );
		this.endTime = new Date( sourceData.end );
		this.conditionEventId = sourceData.conditionEventId;

		return;
	};

	// set the bas class before setting up the rest of the methods...
	my.ExplorerState.setBaseClass( m_node.ExplorerNode );

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerState.prototype.toString = function ()
	{
		return 'State: ...';
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerState.prototype.calculateSize = function ( canvasModule )
	{
		// NOTE: this calculates the sizes in pixels at a 1.0 zoom

		// now figure out the heights
		this.heightFull = canvasModule.edgePadding * 2 + ( canvasModule.fontSize + canvasModule.edgePadding ) * 4;
		this.heightCollapsed = canvasModule.edgePadding * 2 + canvasModule.fontSize;

		var i;
		for( i = 0; i < this.childNodes.length; ++i )
		{
			if( this.childNodes[i].isActive || m_base.internal.showInactiveNodes )
			{
				this.childNodes[i].calculateSize( canvasModule );
			}
		}

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerState.prototype.draw = function ( canvasModule, drawAsInactive )
	{
		var displayFull = canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails;
		var isHovered = m_base.internal.hoveredNode !== null
			&& m_base.internal.hoveredNode.nodeType === this.nodeType
			&& m_base.internal.hoveredNode.id === this.id;
		var isSelected = m_base.internal.selectedNode !== null
			&& m_base.internal.selectedNode.nodeType === this.nodeType
			&& m_base.internal.selectedNode.id === this.id;

		var textLines = [];

		// set up the text lines
		textLines.push( { leftColumn: 'Start:', rightColumn: m_base.internal.formatDateTime( this.startTime, false ) } );
		if( this.endTime !== this.startTime )
		{
			textLines.push( { leftColumn: 'End:', rightColumn: m_base.internal.formatDateTime( this.endTime, false ) } );
		}
		textLines.push( { leftColumn: 'Status:', rightColumn: this.isActive ? 'Active' : 'Inactive' } );

		// pre-calculate many of the values at the current zoom levt
		var adjustedEdgePadding = canvasModule.adjustDistance( canvasModule.edgePadding, true );
		var adjustedFontSize = canvasModule.adjustDistance( canvasModule.fontSize, true );

		var adjustedLeft = canvasModule.convertTimeToCanvasX( this.startTime );
		var adjustedRight = canvasModule.convertTimeToCanvasX( this.endTime );

		var adjustedHeightCollapsed = canvasModule.adjustDistance( this.heightCollapsed, true );
		var adjustedHeightFull = canvasModule.adjustDistance( this.heightFull, true );
		var adjustedHeight = displayFull ? adjustedHeightFull : adjustedHeightCollapsed;

		var adjustedY = canvasModule.convertYLocationToCanvas( displayFull ? this.yFull : this.yCollapsed ) - adjustedHeight / 2;

		var leftColumnWidth = 0;

		var i;

		// draw the connecting line to the parent condition event
		var parentConnectingPoint;
		if( this.parentNode !== null )
		{
			parentConnectingPoint = this.parentNode.getConnectingPointCanvas( canvasModule );

			canvasModule.canvasContext.lineWidth = 1;
			canvasModule.canvasContext.strokeStyle = this.isActive && !drawAsInactive
				? canvasModule.connectingLineColor
				: canvasModule.inactiveConnectingLineColor;

			canvasModule.canvasContext.beginPath();
			canvasModule.canvasContext.moveTo(
				parentConnectingPoint.x,
				parentConnectingPoint.y
				);
			canvasModule.canvasContext.lineTo(
				adjustedLeft,
				adjustedY + adjustedHeight / 2
				);
			canvasModule.canvasContext.stroke();
		}

		// fill the label background
		canvasModule.canvasContext.fillStyle = isHovered || isSelected
			? canvasModule.nodeHighlightColor
			: this.isActive && !drawAsInactive
				? 'rgb(' + this.color + ')'
				: canvasModule.inactiveNodeColor;
		canvasModule.canvasContext.fillRect(
			adjustedLeft,
			adjustedY,
			adjustedRight - adjustedLeft,
			adjustedHeightCollapsed
			);

		// draw the label text
		if(
			canvasModule.currentDisplayMode === m_base.DisplayMode.ShapeWithTitle
			|| canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails
			)
		{
			canvasModule.canvasContext.font = ( displayFull ? 'bold ' : '' ) + adjustedFontSize + 'px ' + canvasModule.fontFace;
			canvasModule.canvasContext.fillStyle = this.isActive && !drawAsInactive
				? canvasModule.stateTextColor
				: canvasModule.inactiveNodeTextColor;
			canvasModule.canvasContext.fillText(
				this.label,
				adjustedLeft + adjustedEdgePadding,
				adjustedY + adjustedEdgePadding + adjustedFontSize
				);
		}

		if( displayFull )
		{
			// draw the background
			canvasModule.canvasContext.fillStyle = isHovered || isSelected
				? canvasModule.nodeHighlightColor
				: this.isActive && !drawAsInactive
					? canvasModule.stateBackgroundColor
					: canvasModule.inactiveNodeStateBackgroundColor;
			canvasModule.canvasContext.fillRect(
				adjustedLeft,
				adjustedY + adjustedHeightCollapsed - 1,
				adjustedRight - adjustedLeft,
				adjustedHeightFull - adjustedHeightCollapsed
				);

			// draw the other lines
			canvasModule.canvasContext.font = adjustedFontSize + 'px ' + canvasModule.fontFace;
			canvasModule.canvasContext.fillStyle = this.isActive && !drawAsInactive
				? canvasModule.stateTextColor
				: canvasModule.inactiveNodeTextColor;

			// draw the left column and calulate how wide it needs to be
			for( i = 0; i < textLines.length; ++i )
			{
				canvasModule.canvasContext.fillText(
					textLines[i].leftColumn,
					adjustedLeft + adjustedEdgePadding,
					adjustedY + adjustedEdgePadding + ( adjustedEdgePadding + adjustedFontSize ) * ( i + 2 )
					);

				leftColumnWidth = Math.max( leftColumnWidth, canvasModule.canvasContext.measureText( textLines[i].leftColumn ).width );
			}

			// draw the right column
			for( i = 0; i < textLines.length; ++i )
			{
				canvasModule.canvasContext.fillText(
					textLines[i].rightColumn,
					adjustedLeft + leftColumnWidth + adjustedEdgePadding * 2,
					adjustedY + adjustedEdgePadding + ( adjustedEdgePadding + adjustedFontSize ) * ( i + 2 )
					//adjustedRight - adjustedLeft + adjustedEdgePadding * 2 - leftColumnWidth
					);
			}
		}

		canvasModule.canvasContext.restore();

		for( i = 0; i < this.childNodes.length; ++i )
		{
			if( this.childNodes[i].isActive || m_base.internal.showInactiveNodes )
			{
				this.childNodes[i].draw( canvasModule, drawAsInactive || !this.isActive );
			}
		}

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerState.prototype.getConnectingPointCanvas = function ( canvasModule )
	{
		var adjustedHeightCollapsed = canvasModule.adjustDistance( this.heightCollapsed, true );
		var adjustedHeightFull = canvasModule.adjustDistance( this.heightFull, true );
		//var adjustedHeight = canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails ? adjustedHeightFull : adjustedHeightCollapsed;

		return {
			x: canvasModule.convertTimeToCanvasX( this.endTime ),
			y: canvasModule.convertYLocationToCanvas( canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails ? this.yFull : this.yCollapsed )
		};
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerState.prototype.hitTest = function ( canvasModule, locationX, locationTime, locationY )
	{
		var yToUse = canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails ? this.yFull : this.yCollapsed;
		var heightToUse = canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails ? this.heightFull : this.heightCollapsed;

		return locationTime >= this.startTime
			&& locationTime <= this.endTime
			&& locationY >= yToUse - heightToUse / 2
			&& locationY <= yToUse + heightToUse / 2;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerState.prototype.onHover = function ( canvasModule )
	{
		//canvasModule.element.css( 'cursor', 'ew-resize' );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerState.prototype.createContextMenuItems = function ()
	{
		var menuItems = [];

		// set active/inactive
		if( this.isActive )
		{
			menuItems.push( new m_base.internal.ContextMenuItem( 'Set inactive', 'img/disabled-state.png', false, this.setInactive, this ) );
		}
		else
		{
			menuItems.push( new m_base.internal.ContextMenuItem( 'Set active', 'img/new-state.png', false, this.setActive, this ) );
		}

		// add condition event
		menuItems.push( new m_base.internal.ContextMenuItemSeparator() );

		// add condition event
		menuItems.push( new m_base.internal.ContextMenuItem( 'Add condition event', 'img/new-condition-event.png', false, this.addChild, this ) );

		// delete state
		menuItems.push( new m_base.internal.ContextMenuItem( 'Delete state', 'img/delete.png', false, this.deleteNode, this ) );

		return menuItems;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerState.prototype.addChild = function ( menuItemTarget )
	{
		m_base.internal.addNode( menuItemTarget.nodeType, menuItemTarget.id );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerState.prototype.deleteNode = function ( menuItemTarget )
	{
		m_base.internal.deleteNode( menuItemTarget.nodeType, menuItemTarget.id );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerState.prototype.setInactive = function ( menuItemTarget )
	{
		menuItemTarget.isActive = false;
		m_base.internal.saveNodeProperties( menuItemTarget, false );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerState.prototype.setActive = function ( menuItemTarget )
	{
		menuItemTarget.isActive = true;
		m_base.internal.saveNodeProperties( menuItemTarget, false );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	return my;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

} ) );

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
