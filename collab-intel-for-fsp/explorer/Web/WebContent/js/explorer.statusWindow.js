﻿//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

define( ['jquery', 'explorerBase'], ( function ( $, base )
{
	'use strict';

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// public properties

	var my = {};

	my.element = null;

	my.maxLogMessages = 30;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// private properties

	var m_base = base;

	var m_logMessages = [];

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.initialize = function ( elementId )
	{
		my.element = $( '#' + elementId )
			.css( 'position', 'fixed' )
			.css( 'bottom', '0' )
			.css( 'right', '0' )
			.empty()
			.show();

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.addLogMessage = function ( message )
	{
		while( m_logMessages.length > my.maxLogMessages )
			m_logMessages.shift();

		m_logMessages.push( { message: message, timestamp: new Date() } );

		my.refreshStatusWindow();

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	my.refreshStatusWindow = function ()
	{
		var statusText = '';

		var i;
		for( i = 0; i < m_logMessages.length ; ++i )
		{
			if( m_logMessages[i].timestamp.getHours() < 10 )
				statusText += '0';
			statusText += m_logMessages[i].timestamp.getHours() + ':';

			if( m_logMessages[i].timestamp.getMinutes() < 10 )
				statusText += '0';
			statusText += m_logMessages[i].timestamp.getMinutes() + ':';

			if( m_logMessages[i].timestamp.getSeconds() < 10 )
				statusText += '0';
			statusText += m_logMessages[i].timestamp.getSeconds()
				+ ' '
				+ m_logMessages[i].message
				+ '<br/>';
		}

		my.element
			.html( statusText )
			.scrollTop( my.element[0].scrollHeight );

		return;
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	return my;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

} ) );

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
