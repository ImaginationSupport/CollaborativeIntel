//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

var EXPLORER_URI_PREFIX = 'explorer.jsp?id=';

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

$( document ).ready( function ()
{
	// kick off the request
	$.ajax( {
		url: 'explorer-backend',
		type: 'GET',
		data: {
			q: 'getMissions'
		},
		success: populateMissions,
		error: function ( XMLHttpRequest, textStatus, errorThrown )
		{
			showError( 'Server error downloading missions: ' + textStatus );

			return;
		},
	} );

	return;
} );

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function populateMissions( response )
{
	if( !response.success )
		showError( 'Error downloading missions: ' + response.message );

	var i;
	var container = $( '#entries-container' )
		.empty();

	$( '<div/>' )
		.text( 'Please select the mission you wish to use:' )
		.appendTo( container );

	var table;
	var tbody;
	var row;
	var buttonCell;

	if( response.success )
	{
		table = $( '<table/>' )
			.appendTo( container );
		tbody = $( '<tbody/>' )
			.appendTo( table );

		for( i = 0; i < response.missions.length; ++i )
		{
			row = $( '<tr/>' )
				.appendTo( tbody );
			if( i % 2 === 0 )
				row.addClass( 'odd' );

			$( '<td/>' )
				.text( response.missions[i].label )
				.appendTo( row );

			buttonCell = $( '<td/>' )
				.addClass( 'buttonCell' )
				.appendTo( row );

			$( '<button/>' )
				.addClass( 'fsp-button' )
				.text( 'Go!' )
				.data( 'fakeLinkURI', EXPLORER_URI_PREFIX + response.missions[i].rootId )
				.click( function ( event ) { window.location.href = $( event.target ).data( 'fakeLinkURI' ); } )
				.appendTo( buttonCell );
		}

		// now add the row to create the new root node
		row = $( '<tr/>' )
			.appendTo( tbody );
		if( i % 2 === 0 )
			row.addClass( 'odd' );
		buttonCell = $( '<td/>', { colspan: 2 } )
			.css( 'padding-left', '0' )
			.appendTo( row );
		$( '<button/>' )
			.text( 'Create a new mission!' )
			.addClass( 'fsp-button' )
			.click( runCreateNewMission )
			.appendTo( buttonCell );
	}
	else
	{
		container
			.html( '<span style="color:#800">Error downloading missions: ' + response.message + '</span>' );
	}

	return;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function runCreateNewMission()
{
	var container = $( '#entries-container' )
		.empty();

	var form = $( '<form/>' )
		.submit( runCreateNewMissionSubmit )
		.appendTo( container );

	$( '<div/>' )
		.css( 'margin-top', '1rem' )
		.text( 'Name for the new mission:' )
		.appendTo( form );
	var inputBox = $( '<input/>', { type: 'text', id: 'new-mission-name' } )
		.appendTo( form );

	$( '<br/>' )
		.appendTo( form );
	$( '<br/>' )
		.appendTo( form );

	$( '<button/>', { type: 'submit' } )
		.text( 'Create mission!' )
		.appendTo( form );

	$( '<div/>', { id: 'new-mission-error-text' } )
		.addClass( 'error-message' )
		.hide()
		.appendTo( container );

	inputBox.focus();

	return;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function runCreateNewMissionSubmit()
{
	var missionName = $( '#new-mission-name' ).val().trim();

	if( missionName.length === 0 )
	{
		$( '#new-mission-error-text' )
			.text( 'Mission must have a name!' )
			.show();
		return;
	}

	// kick off the request to create the mission
	$.ajax( {
		url: 'explorer-backend',
		type: 'POST',
		data: {
			q: 'createMission',
			title: missionName,
		},
		success: function ( response )
		{
			if( !response.success )
				showError( 'Error creating new mission: ' + response.message );

			window.location.href = EXPLORER_URI_PREFIX + response.id;

			return;
		},
		error: function ( XMLHttpRequest, textStatus, errorThrown )
		{
			showError( 'Server error creating new mission: ' + textStatus );

			return;
		},
	} );

	return false;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function showError( message )
{
	var container = $( '#entries-container' )
		.empty();

	$( '<div/>' )
		.addClass( 'error-message' )
		.text( message )
		.appendTo( container );

	return;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
