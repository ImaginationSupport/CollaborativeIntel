//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

var QUESTION_TYPE_LABELS =
[
	'Unknown',
	'Integer',
	'Choices'
];

var AnswerSortColumns =
{
	Value: 0,
	Confidence: 1,
	Added: 2,
};

var g_currentAnswers = null;
var g_currentAnswerSortColumn = AnswerSortColumns.Added;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

$( document ).ready( function ()
{
	refreshQuestions();

	return;
} );

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function refreshQuestions()
{
	// kick off the request
	$.ajax( {
		url: 'crowd-backend',
		type: 'GET',
		data: {
			q: 'getQuestions'
		},
		success: populateQuestions,
		error: function ( XMLHttpRequest, textStatus )
		{
			showError( 'Server error downloading tree root state information: ' + textStatus );

			return;
		},
	} );

	return;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function populateQuestions( response )
{
	var i;
	var container = $( '#left-column' )
		.empty();

	var table;
	var thead;
	var tbody;
	var tr;
	var cell;

	if( response.success )
	{
		table = $( '<table/>' )
			.appendTo( container );
		thead = $( '<thead/>' )
			.appendTo( table );
		tr = $( '<tr/>' )
			.appendTo( thead );
		$( '<th/>' )
			.text( 'Question' )
			.appendTo( tr );
		$( '<th/>' )
			.text( 'Existing Answers' )
			.appendTo( tr );
		$( '<th/>' )
			.text( 'Actions' )
			.appendTo( tr );

		tbody = $( '<tbody/>' )
			.appendTo( table );

		for( i = 0; i < response.questions.length; ++i )
		{
			tr = $( '<tr/>' )
				.addClass( i % 2 === 0 ? 'odd' : 'even' )
				.appendTo( tbody );

			// question text column
			cell = $( '<td/>' )
				.text( response.questions[i].question )
				.appendTo( tr );
			if( !response.questions[i].active )
			{
				cell.addClass( 'inactive' );
			}

			// existing answers column
			$( '<td/>' )
				.addClass( 'center-align' )
				.text( response.questions[i].answers )
				.appendTo( tr );

			// actions
			cell = $( '<td/>' )
				.addClass( 'cell' )
				.addClass( 'center-align' )
				.appendTo( tr );
			$( '<button/>' )
				.addClass( 'fsp-button' )
				.css( 'width', '100%' )
				.text( 'Edit' )
				.data( 'question', response.questions[i] )
				.click( editQuestion )
				.appendTo( cell );
			$( '<button/>' )
				.addClass( 'fsp-button' )
				.css( 'width', '100%' )
				.text( 'View answers' )
				.data( 'question', response.questions[i] )
				.click( viewAnswers )
				.appendTo( cell );
		}

		// now add the row to create the new root node
		tr = $( '<tr/>' )
			.appendTo( tbody );
		tr.addClass( i % 2 === 0 ? 'odd' : 'even' );

		cell = $( '<td/>', { colspan: 5 } )
			.css( 'padding-left', '0' )
			.appendTo( tr );
		$( '<button/>' )
			.addClass( 'fsp-button' )
			.text( 'Create a new question!' )
			.click( runNewQuestion )
			.appendTo( cell );
	}
	else
	{
		container
			.html( '<span style="color:#800">Error downloading questions: ' + response.message + '</span>' );
	}

	return;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function viewAnswers()
{
	var question = $( event.target ).data( 'question' );

	$( '#right-column' )
		.empty();

	$( '<div>' )
		.text( 'Loading existing answers, please wait...' );

	// kick off the request
	$.ajax( {
		url: 'crowd-backend',
		type: 'GET',
		data: {
			q: 'getExistingAnswers',
			id: question.id
		},
		question: question,
		beforeSend: function ( jqXHR, settings )
		{
			jqXHR.question = settings.question;

			return;
		},
		success: populateExistingAnswers,
		error: function ( XMLHttpRequest, textStatus )
		{
			showError( 'Server error downloading existing answers: ' + textStatus );

			return;
		},
	} );

	return;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function populateExistingAnswers( response, responseRaw, jqXHR )
{
	var container = $( '#right-column' )
		.empty();

	var sectionBody = $( '<div/>' )
		.addClass( 'section-body' )
		.appendTo( container );

	var question = jqXHR.question;

	var i;
	var table;
	var thead;
	var tbody;
	var tr;
	var cell;

	if( response.success )
	{
		g_currentAnswers = response.answers;
		g_currentAnswerSortColumn = AnswerSortColumns.Added;
		sortAnswers();

		$( '<button/>' )
			.addClass( 'fsp-button' )
			.css( 'margin-top', '1rem' )
			.css( 'margin-bottom', '1rem' )
			.text( 'Generate' )
			.data( 'question', question )
			.click( setupGenerateAnswers )
			.appendTo( sectionBody );

		if( g_currentAnswers.length == 0 )
		{
			$( '<div/>' )
				.text( 'No answers currently exist.' )
				.appendTo( sectionBody );
		}
		else
		{
			table = $( '<table/>' )
				.addClass( 'data-table' )
				.appendTo( sectionBody );
			thead = $( '<thead/>' )
				.appendTo( table );

			tr = $( '<tr/>' )
				.appendTo( thead );
			$( '<th/>' )
				.text( 'Value' )
				.appendTo( tr );
			$( '<th/>' )
				.text( 'Confidence' )
				.appendTo( tr );
			$( '<th/>' )
				.text( 'Date' )
				.appendTo( tr );
			$( '<th/>' )
				.text( 'Actions' )
				.appendTo( tr );

			tbody = $( '<tbody/>' )
				.appendTo( table );

			for( i = 0; i < g_currentAnswers.length; ++i )
			{
				tr = $( '<tr/>' )
					.addClass( i % 2 === 0 ? 'odd' : 'even' )
					.appendTo( tbody );

				$( '<td/>' )
					.text( g_currentAnswers[i].value )
					.appendTo( tr );
				$( '<td/>' )
					.addClass( 'right-align' )
					.text( g_currentAnswers[i].confidence.toFixed( 2 ) )
					.appendTo( tr );
				$( '<td/>' )
					.addClass( 'right-align' )
					.text( g_currentAnswers[i].date )
					.appendTo( tr );
				cell = $( '<td/>' )
					.addClass( 'right-align' )
					.appendTo( tr );
				$( '<button/>' )
					.addClass( 'fsp-button' )
					.css( 'margin-left', '1rem' )
					.text( 'Edit' )
					//.click( setupGenerateAnswers )
					.appendTo( cell );
				$( '<button/>' )
					.addClass( 'fsp-button' )
					.css( 'margin-left', '0.5rem' )
					.text( 'Delete' )
					//.click( setupGenerateAnswers )
					.appendTo( cell );
			}
		}
	}
	else
	{
		sectionBody.html( '<span style="color:#800">Error downloading questions: ' + response.message + '</span>' );
	}

	return;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function editQuestion( event )
{
	var question = $( event.target ).data( 'question' );

	var container = $( '#right-column' )
		.empty()
		.data( 'current-question', question );

	var i;

	// question setup
	$( '<div/>' )
		.addClass( 'section-subhead' )
		.css( 'margin-top', '0' )
		.text( 'Question setup' )
		.appendTo( container );
	var sectionBody = $( '<div/>' )
		.addClass( 'section-body' )
		.appendTo( container );

	var table = $( '<table/>' )
		.appendTo( sectionBody );
	var tbody = $( '<tbody/>' )
		.appendTo( table );

	// label
	var tr = $( '<tr/>' )
		.appendTo( tbody );
	$( '<td/>' )
		.text( 'Label:' )
		.appendTo( tr );
	var inputCell = $( '<td/>' )
		.appendTo( tr );
	$( '<input/>', { type: 'text' } )
		.css( 'width', '400px' )
		.val( question.label )
		.appendTo( inputCell );

	// question type
	tr = $( '<tr/>' )
		.appendTo( tbody );
	$( '<td/>' )
		.text( 'Question type:' )
		.appendTo( tr );
	inputCell = $( '<td/>' )
		.appendTo( tr );
	var inputControl = $( '<select/>', { id: 'right-column-question-type' } )
		.appendTo( inputCell );
	for( i = 1; i < QUESTION_TYPE_LABELS.length; ++i )
	{
		$( '<option/>' )
			.text( QUESTION_TYPE_LABELS[i] )
			.val( i )
			.appendTo( inputControl );
	}

	// active
	tr = $( '<tr/>' )
		.appendTo( tbody );
	$( '<td/>' )
		.text( 'Active:' )
		.appendTo( tr );
	inputCell = $( '<td/>' )
		.appendTo( tr );
	$( '<input/>', { type: 'checkbox' } )
		.appendTo( inputCell );

	// context
	$( '<div/>' )
		.addClass( 'section-subhead' )
		.text( 'Context' )
		.appendTo( container );
	sectionBody = $( '<div/>' )
		.addClass( 'section-body' )
		.appendTo( container );
	$( '<textarea/>' )
		.val( question.context )
		.appendTo( sectionBody );

	// question
	$( '<div/>' )
		.addClass( 'section-subhead' )
		.text( 'Question' )
		.appendTo( container );
	sectionBody = $( '<div/>' )
		.addClass( 'section-body' )
		.appendTo( container );
	$( '<textarea/>' )
		.val( question.question )
		.appendTo( sectionBody );

	$( '<button/>' )
		.addClass( 'fsp-button' )
		.css( 'margin-top', '2rem' )
		.text( 'Save' )
		.data( 'question', question )
		.click( saveQuestion )
		.appendTo( sectionBody );

	return;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function saveQuestion()
{
	//var question = $( event.target ).data( 'question' );

	$( '#right-column' )
		.empty();

	return;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function setupGenerateAnswers( event )
{
	var question = $( event.target ).data( 'question' );

	var container = $( '#right-column' )
		.empty()
		.data( 'current-question', question );

	// context
	$( '<div/>' )
		.addClass( 'section-subhead' )
		.text( 'Context' )
		.appendTo( container );
	$( '<div/>' )
		.addClass( 'section-body' )
		.text( question.context.length === 0 ? '(empty)' : question.context )
		.appendTo( container );

	// question
	$( '<div/>' )
		.addClass( 'section-subhead' )
		.text( 'Question' )
		.appendTo( container );
	$( '<div/>' )
		.addClass( 'section-body' )
		.text( question.question.length === 0 ? '(empty)' : question.question )
		.appendTo( container );

	// generate
	$( '<div/>' )
		.addClass( 'section-subhead' )
		.text( 'Generate' )
		.appendTo( container );
	var sectionBody = $( '<div/>' )
		.addClass( 'section-body' )
		.appendTo( container );

	var table = $( '<table/>' )
		.appendTo( sectionBody );
	var tbody = $( '<tbody/>' )
		.appendTo( table );

	// number of answers to generate
	var tr = $( '<tr/>' )
		.appendTo( tbody );
	$( '<td/>' )
		.text( 'Number of answers to generate:' )
		.appendTo( tr );
	var inputCell = $( '<td/>' )
		.appendTo( tr );
	$( '<input/>', { type: 'text', id: 'right-column-count' } )
		.addClass( 'right-align' )
		.val( 20 )
		.appendTo( inputCell );	

	// value header
	tr = $( '<tr/>' )
		.appendTo( tbody );
	$( '<td/>', { colspan: 2 } )
		.css( 'padding-top', '1rem' )
		.text( 'Value to generate:' )
		.appendTo( tr );

	// min value
	tr = $( '<tr/>' )
		.appendTo( tbody );
	$( '<td/>' )
		.css( 'padding-left', '2rem' )
		.text( 'Min:' )
		.appendTo( tr );
	inputCell = $( '<td/>' )
		.appendTo( tr );
	$( '<input/>', { type: 'text', id: 'right-column-min' } )
		.addClass( 'right-align' )
		.val( 0 )
		.appendTo( inputCell );

	// max value
	tr = $( '<tr/>' )
		.appendTo( tbody );
	$( '<td/>' )
		.css( 'padding-left', '2rem' )
		.text( 'Max:' )
		.appendTo( tr );
	inputCell = $( '<td/>' )
		.appendTo( tr );
	$( '<input/>', { type: 'text', id: 'right-column-max' } )
		.addClass( 'right-align' )
		.val( 100 )
		.appendTo( inputCell );

	// existing answers header
	tr = $( '<tr/>' )
		.appendTo( tbody );
	$( '<td/>', { colspan: 2 } )
		.css( 'padding-top', '1rem' )
		.text( 'Existing answers:' )
		.appendTo( tr );

	// existing answers count
	tr = $( '<tr/>' )
		.appendTo( tbody );
	$( '<td/>' )
		.css( 'padding-left', '2rem' )
		.text( 'Count:' )
		.appendTo( tr );
	inputCell = $( '<td/>' )
		.text( question.answers )
		.appendTo( tr );

	// remove existing answers
	tr = $( '<tr/>' )
		.appendTo( tbody );
	$( '<td/>' )
		.css( 'padding-left', '2rem' )
		.text( 'Remove existing first:' )
		.appendTo( tr );
	inputCell = $( '<td/>' )
		.appendTo( tr );
	$( '<input/>', { type: 'checkbox', id: 'right-column-remove-existing' } )
		.addClass( 'right-align' )
		.appendTo( inputCell );

	// generate button
	$( '<button/>' )
		.text( 'Generate!' )
		.css( 'margin-top', '1rem' )
		.addClass( 'fsp-button' )
		.click( runGenerateAnswers )
		.appendTo( sectionBody );

	return;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function runGenerateAnswers()
{
	var count = $( '#right-column-count' ).val();
	var valueMin = $( '#right-column-min' ).val();
	var valueMax = $( '#right-column-max' ).val();
	var removeExisting = $( '#right-column-remove-existing' ).prop( 'checked' );

	var container = $( '#right-column' )
		.empty();

	var question = container.data( 'current-question' );

	$( '<div/>' )
		.addClass( 'simple-message-holder' )
		.text( 'Generating answers, please wait...' )
		.appendTo( container );

	// kick off the request
	$.ajax( {
		url: 'crowd-backend',
		type: 'POST',
		data: {
			q: 'generateAnswersInteger',
			id: question.id,
			count: count,
			min: valueMin,
			max: valueMax,
			removeExisting: removeExisting
		},
		question: question,
		beforeSend: function ( jqXHR, settings )
		{
			jqXHR.question = settings.question;

			return;
		},
		success: function ( XMLHttpRequest, textStatus, errorThrown )
		{
			populateExistingAnswers( XMLHttpRequest, textStatus, errorThrown );
			refreshQuestions();

			return;
		},
		error: function ( XMLHttpRequest, textStatus )
		{
			showError( 'Server error downloading existing answers: ' + textStatus );

			return;
		},
	} );

	return;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function runNewQuestion()
{
	return;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function sortAnswers()
{
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
