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

	my.selectedNodeChangedCallback = null;

	my.element = null;
	my.canvasContext = null;

	my.canvasWidth = 0;
	my.canvasHeight = 0;

	// holds the date of the left side of the canvas
	my.viewLeftTime = null;

	// holds the Y-value of the top of the canvas
	my.viewTop = 0;

	my.zoom = 1.0;

	// holds the display modes, these will get initialized later though
	my.normalDisplayMode = 0;
	my.currentDisplayMode = 0;

	my.backgroundColor = 'rgb(50,50,55)';
	my.nodeHighlightColor = 'rgb(250,250,75)';
	my.stateBackgroundColor = 'rgb(255,255,255)';
	my.stateTextColor = 'rgb(0,0,0)';
	my.connectingLineColor = 'rgb(245,205,50)';
	my.timeFrameIndicatorColor = 'rgb(200,200,200)';

	my.inactiveNodeColor = 'rgb(120,120,120)';
	my.inactiveNodeStateBackgroundColor = 'rgb(180,180,180)';
	my.inactiveNodeTextColor = 'rgb(40,40,40)';
	my.inactiveConnectingLineColor = 'rgb(120,120,120)';

	m_hourInPixels = 0;

	// holds the current font to use when drawing
	my.fontSize = 11;
	my.fontFace = 'Verdana';

	my.edgePadding = 4;

	my.minZoom = 0.05;
	my.maxZoom = 10.0;
	my.zoomMouseWheelStop = 0.08;

	my.textDrawMinimumZoom = 2.0;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// private properties

	var m_base = base;

	var m_isDragging = false;
	var m_dragStart = null;
	var m_dragLast = null;

	var REDRAW_LOOP_INTERVAL = 75;
	var m_redrawNeeded = true;
	var m_redrawLoopTimerId = null;

	var m_contextMenu = null;

	var m_hourInPixels = 1.0;

	var MILLISECONDS_IN_HOUR = 60 * 60 * 1000;

	var INDICATOR_MONTH_NAMES = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sept', 'Oct', 'Nov', 'Dec'];

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.initialize = function ( elementId )
	{
		my.element = $( '#' + elementId )
			.css( 'position', 'fixed' )
			.css( 'top', '0' )
			.css( 'left', '0' )
			.show();

		my.element.mousemove( onMouseMove );
		my.element.mouseup( onMouseUp );
		my.element.mousedown( onMouseDown );
		my.element.mouseout( onMouseOut );
		my.element.dblclick( onMouseDoubleClick );
		my.element.bind( 'contextmenu', runContextMenu );
		my.element[0].addEventListener( 'mousewheel', onMouseWheel, false );
		my.element[0].addEventListener( 'DOMMouseScroll', onMouseWheel, false );

		// bind a mouse-up handler on the document to catch when the mouse goes off the canvas when dragging
		$( document ).mouseup( onMouseUp );

		// bind the mouse click handler on the entire document for closing the context menu
		$( document ).click( closeContextMenu );

		my.canvasContext = my.element[0].getContext( '2d' );

		my.normalDisplayMode = m_base.DisplayMode.FullDetails;
		my.currentDisplayMode = my.normalDisplayMode;

		m_redrawLoopTimerId = window.setInterval( runRedraw, REDRAW_LOOP_INTERVAL );

		// create and initialize the context menu
		m_contextMenu = $( '<div/>', { id: 'main-canvas-context-menu' } )
			.addClass( 'context-menu' )
			.appendTo( $( 'body:first' ) );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.redraw = function ()
	{
		m_redrawNeeded = true;

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.resetView = function ()
	{
		if( m_base.internal.dataBounds === null )
		{
			return;
		}

		// use a padding of 2% for the base left/right padding

		m_hourInPixels = my.canvasWidth * 0.96 / ( ( m_base.internal.dataBounds.end.getTime() - m_base.internal.dataBounds.start.getTime() ) / MILLISECONDS_IN_HOUR );

		my.viewLeftTime = new Date( m_base.internal.dataBounds.start.getTime() - ( m_base.internal.dataBounds.end.getTime() - m_base.internal.dataBounds.start.getTime() ) * 0.02 );

		my.viewTop = my.canvasHeight / -2;

		my.setZoom( 1.0 );

		my.redraw();

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.setZoom = function ( zoom )
	{
		if( zoom > my.maxZoom )
		{
			zoom = my.maxZoom;
		}
		else if( zoom < my.minZoom )
		{
			zoom = my.minZoom;
		}

		if( zoom !== my.zoom )
		{
			my.zoom = zoom;

			my.currentDisplayMode = my.zoom < my.textDrawMinimumZoom
				? my.normalDisplayMode
				: m_base.DisplayMode.ShapeOnly;

			my.redraw();
		}

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.convertTimeToCanvasX = function ( dateTime )
	{
		return ( dateTime.getTime() - my.viewLeftTime.getTime() ) / MILLISECONDS_IN_HOUR * m_hourInPixels / my.zoom;
	};

	my.convertCanvasXToTime = function ( canvasX )
	{
		if( my.viewLeftTime === null )
		{
			return new Date();
		}
		else
		{
			return new Date( canvasX * my.zoom / m_hourInPixels * MILLISECONDS_IN_HOUR + my.viewLeftTime.getTime() );
		}
	};

	my.convertYLocationToCanvas = function ( rawYLocation )
	{
		return ( rawYLocation - my.viewTop ) / my.zoom;
	};

	my.convertCanvasYToLocation = function ( canvasY )
	{
		return canvasY * my.zoom + my.viewTop;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.adjustDistance = function ( distance, toCanvas )
	{
		if( distance === undefined || distance === null )
		{
			return distance;
		}

		if( toCanvas )
		{
			return distance / my.zoom;
		}
		else
		{
			return distance * my.zoom;
		}
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function onMouseMove( mouseEvent )
	{
		if( m_base.internal.rootState === null )
		{
			return;
		}

		var canvasPoint = mouseEventToCanvasPoint( mouseEvent );

		if( m_isDragging )
		{
			if( mouseEvent.which !== 1 )
			{
				m_isDragging = false;
			}
			else
			{
				my.viewLeftTime = new Date( my.viewLeftTime.getTime() + my.convertCanvasXToTime( m_dragLast.x ).getTime() - my.convertCanvasXToTime( canvasPoint.x ).getTime() );
				my.viewTop += my.adjustDistance( m_dragLast.y - canvasPoint.y, false );

				m_dragLast = canvasPoint;
			}
		}
		else
		{
			m_base.internal.hoveredNode = m_base.internal.rootState.findNodeAtLocation(
				my,
				canvasPoint.x,
				my.convertCanvasXToTime( canvasPoint.x ),
				my.convertCanvasYToLocation( canvasPoint.y )
				);

			// run the onHover if it exists, otherwise just set the cursor to the default
			if( m_base.internal.hoveredNode !== null && m_base.internal.hoveredNode.onHover !== undefined )
			{
				m_base.internal.hoveredNode.onHover( my );
			}
			else
			{
				my.element.css( 'cursor', 'default' );
			}
		}

		my.redraw();

		// the mouse up is attached to the canvas AND the document, so skip the other fire
		//mouseEvent.stopPropagation();

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function onMouseUp( mouseEvent )
	{
		var canvasPoint = mouseEventToCanvasPoint( mouseEvent );

		m_isDragging = false;

		// if the user has dragged (much) then ignore this as a click to select
		if( m_dragStart !== null && Math.abs( m_dragStart.x - canvasPoint.x ) <= 5 && Math.abs( m_dragStart.y - canvasPoint.y ) <= 5 )
		{
			var previouslySelectedNode = m_base.internal.selectedNode;
			m_base.internal.selectedNode = m_base.internal.rootState.findNodeAtLocation(
				my,
				canvasPoint.x,
				my.convertCanvasXToTime( canvasPoint.x ),
				my.convertCanvasYToLocation( canvasPoint.y )
				);

			if( my.selectedNodeChangedCallback !== null && m_base.internal.selectedNode !== previouslySelectedNode )
			{
				my.selectedNodeChangedCallback();
			}
		}

		my.redraw();

		// the mouse up is attached to the canvas AND the document, so skip the other fire
		//mouseEvent.stopPropagation();

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function onMouseDown( mouseEvent )
	{
		// we only care about the left mouse button
		if( mouseEvent.which !== 1 )
		{
			return;
		}

		if( m_base.internal.rootState === null )
		{
			return;
		}

		var canvasPoint = mouseEventToCanvasPoint( mouseEvent );

		m_isDragging = true;
		m_dragStart = canvasPoint;
		m_dragLast = canvasPoint;

		my.redraw();

		// the mouse up is attached to the canvas AND the document, so skip the other fire
		//mouseEvent.stopPropagation();

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function onMouseOut( mouseEvent )
	{
		m_base.internal.hoveredNode = null;

		my.redraw();

		// the mouse up is attached to the canvas AND the document, so skip the other fire
		//mouseEvent.stopPropagation();

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function onMouseDoubleClick( mouseEvent )
	{
		my.resetView();

		my.redraw();

		// the mouse up is attached to the canvas AND the document, so skip the other fire
		//mouseEvent.stopPropagation();

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function onMouseWheel( mouseEvent )
	{
		var canvasPoint = mouseEventToCanvasPoint( mouseEvent );

		// get the location of the mouse before the zoom change
		var viewXBeforeZoom = my.convertCanvasXToTime( canvasPoint.x ).getTime();
		var viewYBeforeZoom = my.convertCanvasYToLocation( canvasPoint.y );

		var delta = Math.max( -1, Math.min( 1, ( mouseEvent.wheelDelta || -mouseEvent.detail ) ) );

		if( delta === 0 )
		{
			return;
		}

		var newZoom = my.zoom;

		if( delta < 0 )
		{
			// zoom in
			newZoom += my.zoomMouseWheelStop;
		}
		else
		{
			// zoom out
			newZoom -= my.zoomMouseWheelStop;
		}

		// if no change, just bail
		if( newZoom === my.zoom )
		{
			return;
		}

		my.setZoom( newZoom );

		// get the location of the mouse after the zoom change
		var viewXAfterZoom = my.convertCanvasXToTime( canvasPoint.x ).getTime();
		var viewYAfterZoom = my.convertCanvasYToLocation( canvasPoint.y );

		// update the view so that the mouse is still over the same location
		my.viewLeftTime = new Date( my.viewLeftTime.getTime() + viewXBeforeZoom - viewXAfterZoom );
		my.viewTop += viewYBeforeZoom - viewYAfterZoom;

		// redraw the canvas
		my.redraw();

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function runContextMenu( mouseEvent )
	{
		closeContextMenu();

		var i;

		var alternateTrees;

		var menuItems = null;
		if( m_base.internal.hoveredNode !== null )
		{
			menuItems = m_base.internal.hoveredNode.createContextMenuItems();
		}
		else
		{
			menuItems = [];

			if( m_base.internal.showInactiveNodes )
			{
				menuItems.push(
					new m_base.internal.ContextMenuItem(
					'Only show active nodes',
					'img/collapsed-tree.png',
					false,
					function () { m_base.showInactiveNodes( false ); },
					null
					)
				);
			}
			else
			{
				menuItems.push(
					new m_base.internal.ContextMenuItem(
					'Show all nodes',
					'img/full-tree.png',
					false,
					function () { m_base.showInactiveNodes( true ); },
					null
					)
				);
			}

			// see if there are alternate trees
			alternateTrees = [];
			for( i = 0; i < m_base.internal.missions.length; ++i )
			{
				if( m_base.internal.missions[i].rootStateId !== m_base.internal.selectedTreeRootId )
				{
					alternateTrees.push( m_base.internal.missions[i] );
				}
			}

			if( alternateTrees.length > 0 )
			{
				// add a separator
				menuItems.push( new m_base.internal.ContextMenuItemSeparator() );

				// add the trees
				for( i = 0; i < alternateTrees.length; ++i )
				{
					menuItems.push(
						new m_base.internal.ContextMenuItem(
							'Switch to tree "' + alternateTrees[i].label + '"',
							'img/full-tree.png',
							false,
							function ( menuItem ) { m_base.internal.switchTrees( menuItem.rootStateId ); },
							alternateTrees[i]
							)
						);
				}
			}
		}

		if( menuItems === null || menuItems.length === 0 )
		{
			return;
		}

		var menuEntry;
		for( i = 0; i < menuItems.length; ++i )
		{
			menuEntry = $( '<div/>' )
				.addClass( 'context-menu-item' )
				.data( 'menu-item-data', menuItems[i] )
				.appendTo( m_contextMenu );

			if( menuItems[i].label )
			{
				$( '<div/>' )
					.addClass( 'context-menu-item-text' )
					.text( menuItems[i].label )
					.appendTo( menuEntry );

				if( menuItems[i].iconUri )
				{
					menuEntry.css( 'background-image', 'url(\'' + menuItems[i].iconUri + '\')' );
				}
			}
			else
			{
				// no text means this entry is a separator, so use an HTML <hr/>
				$( '<hr/>' )
					.appendTo( menuEntry );
			}

			// if this entry is enabled, setup the click handler, otherwise give it the disabled style
			if( menuItems[i].disabled )
			{
				menuEntry.addClass( 'context-menu-item-disabled' );
			}
			else
			{
				menuEntry.click( runContextMenuItem );
			}
		}

		// show the context menu
		m_contextMenu
			.data( 'target', m_base.internal.hoveredNode )
			.css( 'left', mouseEvent.clientX.toString() + 'px' )
			.css( 'top', mouseEvent.clientY.toString() + 'px' )
			.show();

		return false;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function runContextMenuItem()
	{
		var menuItem = $( this ).data( 'menu-item-data' );

		if( menuItem.action !== undefined )
		{
			menuItem.action( menuItem.data );
		}

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function closeContextMenu()
	{
		m_contextMenu
			.empty()
			.hide();

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function mouseEventToCanvasPoint( mouseEvent )
	{
		var rect = my.element[0].getBoundingClientRect();

		return {
			x: ( mouseEvent.clientX - rect.left ),
			y: ( mouseEvent.clientY - rect.top )
		};
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function drawTimeFrameIndicators()
	{
		if( m_base.internal.dataBounds === null )
		{
			return;
		}

		var viewRightTime = my.convertCanvasXToTime( my.canvasWidth );

		var viewDayCount = ( viewRightTime.getTime() - my.viewLeftTime.getTime() ) / ( MILLISECONDS_IN_HOUR * 24 );
		var indicatorMonthIncrement = 1 + Math.floor( viewDayCount / 200 );

		var timeFrameIndicators = [];

		var anchorMonth = m_base.internal.dataBounds.start.getMonth();
		var anchorYear = m_base.internal.dataBounds.end.getFullYear();

		var workingMonth;
		var workingYear;

		// first go from the anchor to the right side of the screen
		workingMonth = anchorMonth;
		workingYear = anchorYear;
		var indicatorX = my.convertTimeToCanvasX( new Date( workingYear, workingMonth, 1, 0, 0, 0, 0 ) );

		// the -100 is because the text is off the right side of the line, so even if the line isn't shown, possibly show the text
		var highlightSection = true;
		while( indicatorX < my.canvasWidth )
		{
			if( indicatorX >= -100 && indicatorX < my.canvasWidth )
			{
				timeFrameIndicators.push( {
					x: indicatorX,
					month: workingMonth,
					year: workingYear,
					highlightSection: highlightSection,
					rightFacing: true,
				} );
				highlightSection = !highlightSection;
			}

			workingMonth += indicatorMonthIncrement;
			while( workingMonth >= 12 )
			{
				workingMonth -= 12;
				++workingYear;
			}

			indicatorX = my.convertTimeToCanvasX( new Date( workingYear, workingMonth, 1, 0, 0, 0, 0 ) );
		}

		// now go from the anchor to the left side of the screen
		workingMonth = anchorMonth - indicatorMonthIncrement;
		workingYear = anchorYear;
		while( workingMonth < 0 )
		{
			workingMonth += 12;
			--workingYear;
		}
		indicatorX = my.convertTimeToCanvasX( new Date( workingYear, workingMonth, 1, 0, 0, 0, 0 ) );
		highlightSection = false;
		while( indicatorX > -100 )
		{
			if( indicatorX >= -100 && indicatorX < my.canvasWidth )
			{
				timeFrameIndicators.unshift( {
					x: indicatorX,
					month: workingMonth,
					year: workingYear,
					highlightSection: highlightSection,
					rightFacing: false,
				} );
				highlightSection = !highlightSection;
			}

			workingMonth -= indicatorMonthIncrement;
			while( workingMonth < 0 )
			{
				workingMonth += 12;
				--workingYear;
			}

			indicatorX = my.convertTimeToCanvasX( new Date( workingYear, workingMonth, 1, 0, 0, 0, 0 ) );
		}

		// now draw the entries

		my.canvasContext.save();

		my.canvasContext.lineWidth = 1;
		my.canvasContext.fillStyle = my.timeFrameIndicatorColor;

		my.canvasContext.font = my.fontSize + 'pt ' + my.fontFace;

		var i;
		var indicatorLeftX;
		var indicatorWidth;
		for( i = 0; i < timeFrameIndicators.length; ++i )
		{
			if( timeFrameIndicators[i].rightFacing )
			{
				indicatorLeftX = timeFrameIndicators[i].x;
				indicatorWidth = i === timeFrameIndicators.length - 1
					? my.canvasWidth - indicatorLeftX
					: timeFrameIndicators[i + 1].x - indicatorLeftX;
			}
			else
			{
				if( i === timeFrameIndicators.length - 1 )
				{
					indicatorLeftX = 0;
					indicatorWidth = timeFrameIndicators[i].x;
				}
				else
				{
					indicatorLeftX = timeFrameIndicators[i].x;
					indicatorWidth = timeFrameIndicators[i + 1].x - timeFrameIndicators[i].x;
				}
			}

			if( timeFrameIndicators[i].highlightSection )
			{
				my.canvasContext.fillStyle = 'rgba(200,200,200,0.05)';
				my.canvasContext.fillRect( indicatorLeftX, 0, indicatorWidth, my.canvasHeight );
			}

			my.canvasContext.fillStyle = my.timeFrameIndicatorColor;
			my.canvasContext.fillText(
				INDICATOR_MONTH_NAMES[timeFrameIndicators[i].month] + ' ' + timeFrameIndicators[i].year,
				timeFrameIndicators[i].x + 2,
				my.canvasHeight - 4
				);
		}

		my.canvasContext.restore();

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function runRedraw()
	{
		if( !m_redrawNeeded )
		{
			return;
		}

		//var start = new Date();

		my.canvasContext.save();

		// wipe the background
		my.canvasContext.fillStyle = my.backgroundColor;
		my.canvasContext.fillRect( 0, 0, my.canvasWidth, my.canvasHeight );

		if( m_base.internal.rootState === null )
		{
			my.canvasContext.font = my.fontSize + 'pt ' + my.fontFace;
			my.canvasContext.fillStyle = 'white';
			my.canvasContext.fillText( 'Loading state space, please wait...', 10, 20 );
		}
		else
		{
			// draw the time frame indicators
			drawTimeFrameIndicators();

			// now draw the nodes on top
			m_base.internal.rootState.draw( my, false );
		}

		// finish up
		my.canvasContext.restore();

		//var end = new Date();
		//console.log( end.getTime() - start.getTime() );

		m_redrawNeeded = false;

		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	return my;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

} ) );

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
