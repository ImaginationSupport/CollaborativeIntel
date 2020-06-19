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

	my.ExplorerConditionEvent = function ( sourceData )
	{
		this.nodeType = m_node.NodeType.ConditionEvent;

		this.id = sourceData.id;
		this.label = sourceData.label;
		this.description = sourceData.description;
		this.isActive = sourceData.active;
		this.color = sourceData.color;
		this.timeAt = new Date( sourceData.timeAt );
		this.stateId = sourceData.stateId;

		return;
	};

	// set the base class before setting up the rest of the methods...
	my.ExplorerConditionEvent.setBaseClass( m_node.ExplorerNode );

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerConditionEvent.prototype.calculateSize = function ( canvasModule )
	{
		// NOTE: this calculates the sizes in pixels at a 1.0 zoom

		this.heightFull = 20;
		this.heightCollapsed = canvasModule.edgePadding * 2 + canvasModule.fontSize;

		var i;
		for( i = 0; i < this.childNodes.length; ++i )
		{
			this.childNodes[i].calculateSize( canvasModule );
		}

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerConditionEvent.prototype.draw = function ( canvasModule, drawAsInactive )
	{
		var displayFull = canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails;
		var isHovered = m_base.internal.hoveredNode !== null
			&& m_base.internal.hoveredNode.nodeType === this.nodeType
			&& m_base.internal.hoveredNode.id === this.id;
		var isSelected = m_base.internal.selectedNode !== null
			&& m_base.internal.selectedNode.nodeType === this.nodeType
			&& m_base.internal.selectedNode.id === this.id;

		//var adjustedEdgePadding = canvasModule.adjustDistance( canvasModule.edgePadding, true );
		//var adjustedFontSize = canvasModule.adjustDistance( canvasModule.fontSize, true );

		var adjustedSize = canvasModule.adjustDistance( displayFull ? this.heightFull : this.heightCollapsed, true );
		var adjustedLeft = canvasModule.convertTimeToCanvasX( this.timeAt ) - adjustedSize / 2;
		var adjustedY = canvasModule.convertYLocationToCanvas( displayFull ? this.yFull : this.yCollapsed ) - adjustedSize / 2;

		var i;

		canvasModule.canvasContext.save();

		// draw the connecting line
		var parentConnectingPoint = null;
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
				adjustedY + adjustedSize / 2
				);
			canvasModule.canvasContext.stroke();
		}

		// draw the diamond
		canvasModule.canvasContext.fillStyle = isHovered || isSelected
			? canvasModule.nodeHighlightColor
			: this.isActive && !drawAsInactive
				? 'rgb(' + this.color + ')'
				: canvasModule.inactiveNodeColor;
		canvasModule.canvasContext.beginPath();
		canvasModule.canvasContext.moveTo(
			adjustedLeft + adjustedSize / 2,
			adjustedY
			);
		canvasModule.canvasContext.lineTo(
			adjustedLeft + adjustedSize,
			adjustedY + adjustedSize / 2
			);
		canvasModule.canvasContext.lineTo(
			adjustedLeft + adjustedSize / 2,
			adjustedY + adjustedSize
			);
		canvasModule.canvasContext.lineTo(
			adjustedLeft,
			adjustedY + adjustedSize / 2
			);
		canvasModule.canvasContext.lineTo(
			adjustedLeft + adjustedSize / 2,
			adjustedY
			);
		canvasModule.canvasContext.fill();

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

	my.ExplorerConditionEvent.prototype.getConnectingPointCanvas = function ( canvasModule )
	{
		var adjustedSize = canvasModule.adjustDistance( canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails ? this.heightFull : this.heightCollapsed, true );
		var adjustedLeft = canvasModule.convertTimeToCanvasX( this.timeAt ) - adjustedSize / 2;
		var adjustedY = canvasModule.convertYLocationToCanvas( canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails ? this.yFull : this.yCollapsed ) - adjustedSize / 2;

		return {
			x: adjustedLeft + adjustedSize,
			y: adjustedY + adjustedSize / 2
		};
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerConditionEvent.prototype.hitTest = function ( canvasModule, locationX, locationTime, locationY )
	{
		var adjustedSize = canvasModule.adjustDistance( canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails ? this.heightFull : this.heightCollapsed, true );
		var adjustedLeft = canvasModule.convertTimeToCanvasX( this.timeAt ) - adjustedSize / 2;
		//var adjustedY = canvasModule.convertYLocationToCanvas( canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails ? this.yFull : this.yCollapsed ) - adjustedSize / 2;

		//var heightToUse = canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails ? this.heightFull : this.heightCollapsed;

		var yToUse = canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails ? this.yFull : this.yCollapsed;
		var heightToUse = canvasModule.currentDisplayMode === m_base.DisplayMode.FullDetails ? this.heightFull : this.heightCollapsed;

		//console.log( 'me: %d  / cursor: %d', yToUse, locationY );

		//console.log(
		//	'x: %s  | y: %s',
		//	locationX < adjustedLeft || locationX > adjustedLeft + adjustedSize ? 'NO' : 'yes!',
		//	locationY < adjustedY || locationY > adjustedY + adjustedSize ? 'NO' : 'yes!'
		//	);

		if(
			locationX < adjustedLeft
			|| locationX > adjustedLeft + adjustedSize
			|| locationY < yToUse - heightToUse / 2
			|| locationY > yToUse + heightToUse / 2
			)
		{
			return false;
		}

		return true;

		/*
				// determine if the point is inside the polygon

				// the algorithm works by casting a ray through the polygon, in this case we use a horizontal one (the direction doesn't matter)
				// so we are only looking at the y-coordinate.  we loop through all of the points and count up the times that
				// see: http://alienryderflex.com/polygon/

				var oddNodes = false;
				var j = this.points.length - 1;

				var adjustedI = null;
				var adjustedJ = null;

				for( i = 0; i < this.points.length; ++i )
				{
					adjustedI = this.points[i].getAdjustedLocation();
					adjustedJ = this.points[j].getAdjustedLocation();

					if(
						( adjustedI.y < canvasPoint.y && adjustedJ.y >= canvasPoint.y || adjustedJ.y < canvasPoint.y && adjustedI.y >= canvasPoint.y )
						&& ( adjustedI.x <= canvasPoint.x || adjustedJ.x <= canvasPoint.x )
						&& ( adjustedI.x + ( canvasPoint.y - adjustedI.y ) / ( adjustedJ.y - adjustedI.y ) * ( adjustedJ.x - adjustedI.x ) < canvasPoint.x )
					)
					{
						oddNodes = !oddNodes;
					}

					j = i;
				}

				return oddNodes;
		*/
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerConditionEvent.prototype.createContextMenuItems = function ()
	{
		var menuItems = [];

		// set active/inactive
		if( this.isActive )
		{
			menuItems.push( new m_base.internal.ContextMenuItem( 'Set inactive', 'img/disabled-condition-event.png', false, this.setInactive, this ) );
		}
		else
		{
			menuItems.push( new m_base.internal.ContextMenuItem( 'Set active', 'img/new-condition-event.png', false, this.setActive, this ) );
		}

		// add condition event
		menuItems.push( new m_base.internal.ContextMenuItemSeparator() );

		// add state
		menuItems.push( new m_base.internal.ContextMenuItem( 'Add state', 'img/new-state.png', false, this.addChild, this ) );

		// delete condition event
		menuItems.push( new m_base.internal.ContextMenuItem( 'Delete condition event', 'img/delete.png', false, this.deleteNode, this ) );

		return menuItems;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerConditionEvent.prototype.addChild = function ( menuItemTarget )
	{
		m_base.internal.addNode( menuItemTarget.nodeType, menuItemTarget.id );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerConditionEvent.prototype.deleteNode = function ( menuItemTarget )
	{
		m_base.internal.deleteNode( menuItemTarget.nodeType, menuItemTarget.id );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerConditionEvent.prototype.setInactive = function ( menuItemTarget )
	{
		menuItemTarget.isActive = false;
		m_base.internal.saveNodeProperties( menuItemTarget, false );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerConditionEvent.prototype.setActive = function ( menuItemTarget )
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
