//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

define( ['jquery', 'explorerBase'], ( function ( $, base )
{
	'use strict';

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// public properties

	var my = {};

	my.NodeType =
	{
		Unknown: 0,
		State: 1,
		ConditionEvent: 2,
	};

	my.nodeVerticalPaddingFull = 4;
	my.nodeVerticalPaddingCollapsed = 8;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// private properties

	var m_base = base;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerNode = function ()
	{
		this.childNodes = [];
		this.parentNode = null;

		this.yFull = 0;
		this.heightFull = 0;

		this.yCollapsed = 0;
		this.heightCollapsed = 0;

		this.childTreeHeightFull = -1;
		this.childTreeHeightCollapsed = -1;

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerNode.prototype.toString = function ()
	{
		return 'Node: ...';
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerNode.prototype.calculateChildStateLocations = function ()
	{
		// first clear out the cached
		this.resetCachedChildTreeHeights();

		this.calculateChildTreeHeights();

		// now recalculate the heights
		this.calculateChildStateLocationsHelper();

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerNode.prototype.calculateChildStateLocationsHelper = function ()
	{
		if( this.childNodes.length === 0 )
			return;

		var i;

		var workingYFull = Math.floor( this.yFull + this.heightFull / 2 - this.childTreeHeightFull / 2 );
		var workingYCollapsed = Math.floor( this.yCollapsed + this.heightCollapsed / 2 - this.childTreeHeightCollapsed / 2 );

		// now space out the nodes accordingly
		for( i = 0; i < this.childNodes.length; ++i )
		{
			if( this.childNodes[i].isActive || m_base.internal.showInactiveNodes )
			{
				this.childNodes[i].yFull = workingYFull + ( this.childNodes[i].childTreeHeightFull - this.heightFull ) / 2;
				this.childNodes[i].yCollapsed = workingYCollapsed + ( this.childNodes[i].childTreeHeightCollapsed - this.heightCollapsed ) / 2;

				workingYFull += this.childNodes[i].childTreeHeightFull + my.nodeVerticalPaddingFull;
				workingYCollapsed += this.childNodes[i].childTreeHeightCollapsed + my.nodeVerticalPaddingCollapsed;
			}
		}

		// now recurse into the child nodes
		for( i = 0; i < this.childNodes.length; ++i )
			if( this.childNodes[i].isActive || m_base.internal.showInactiveNodes )
				this.childNodes[i].calculateChildStateLocationsHelper();

		return;
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerNode.prototype.resetCachedChildTreeHeights = function ()
	{
		this.childTreeHeightFull = -1;
		this.childTreeHeightCollapsed = -1;

		// now recurse into the child nodes
		var i;
		for( i = 0; i < this.childNodes.length; ++i )
			if( this.childNodes[i].isActive || m_base.internal.showInactiveNodes )
				this.childNodes[i].resetCachedChildTreeHeights();

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerNode.prototype.calculateChildTreeHeights = function ()
	{
		// first calculate the tree heights for all of the child trees
		var i;

		for( i = 0; i < this.childNodes.length; ++i )
			if( this.childNodes[i].isActive || m_base.internal.showInactiveNodes )
				this.childNodes[i].calculateChildTreeHeights();

		// now calculate the tree size for this node

		this.childTreeHeightFull = 0;
		this.childTreeHeightCollapsed = 0;
		for( i = 0; i < this.childNodes.length; ++i )
		{
			if( this.childNodes[i].isActive || m_base.internal.showInactiveNodes )
			{
				if( this.childTreeHeightFull > 0 )
				{
					this.childTreeHeightFull += my.nodeVerticalPaddingFull;
					this.childTreeHeightCollapsed += my.nodeVerticalPaddingCollapsed;
				}

				this.childTreeHeightFull += this.childNodes[i].childTreeHeightFull;
				this.childTreeHeightCollapsed += this.childNodes[i].childTreeHeightCollapsed;
			}
		}

		if( this.childTreeHeightFull < this.heightFull )
			this.childTreeHeightFull = this.heightFull;
		if( this.childTreeHeightCollapsed < this.heightCollapsed )
			this.childTreeHeightCollapsed = this.heightCollapsed;

		return;
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerNode.prototype.findDataBounds = function ()
	{
		var bounds = {
			start: null,
			end: null,
		};

		switch( this.nodeType )
		{
			case my.NodeType.State:
				bounds.start = this.startTime;
				bounds.end = this.endTime;
				break;

			case my.NodeType.ConditionEvent:
				bounds.start = this.timeAt;
				bounds.end = this.timeAt;
				break;

			default:
				debugger;
		}

		var i;
		var childBounds;
		for( i = 0; i < this.childNodes.length; ++i )
		{
			if( this.childNodes[i].isActive || m_base.internal.showInactiveNodes )
			{
				childBounds = this.childNodes[i].findDataBounds();

				if( childBounds.start < bounds.start )
					bounds.start = childBounds.start;
				
				if( childBounds.end > bounds.end )
					bounds.end = childBounds.end;
			}
		}

		return bounds;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerNode.prototype.findNodeAtLocation = function ( canvasModule, locationX, locationTime, locationY )
	{
		if( this.hitTest( canvasModule, locationX, locationTime, locationY ) )
			return this;

		var i;
		var found;
		for( i = 0; i < this.childNodes.length; ++i )
		{
			if( this.childNodes[i].isActive || m_base.internal.showInactiveNodes )
			{
				found = this.childNodes[i].findNodeAtLocation( canvasModule, locationX, locationTime, locationY );
				if( found !== null )
					return found;
			}
		}

		return null;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.ExplorerNode.prototype.findNodeWithId = function ( nodeType, id )
	{
		if( this.nodeType === nodeType && this.id === id )
			return this;

		var i;
		var found;
		for( i = 0; i < this.childNodes.length; ++i )
		{
			found = this.childNodes[i].findNodeWithId( nodeType, id );
			if( found !== null )
				return found;
		}

		return null;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	return my;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

} ) );

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
