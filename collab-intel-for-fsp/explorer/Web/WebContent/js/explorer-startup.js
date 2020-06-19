//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// This is the require.js configuration file for the waldo select view

require.config( {
	//baseUrl: 'js/lib',
	paths: {
		jquery: 'third-party/jquery-2.1.3',
		jqueryUI: 'third-party/jquery-ui-1.11.3.custom',

		explorerBase: 'explorer.base',
		explorerNode: 'explorer.node',
		explorerState: 'explorer.state',
		explorerConditionEvent: 'explorer.conditionEvent',

		explorerFeaturesPanel: 'explorer.featuresPanel',
		explorerStatusWindow: 'explorer.statusWindow',
		explorerMainCanvas: 'explorer.mainCanvas',
		explorerPreviewCanvas: 'explorer.previewCanvas',
	}
} );

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

requirejs(
	['jquery', 'jqueryUI', 'explorerBase', 'explorerState', 'explorerConditionEvent', 'explorerFeaturesPanel', 'explorerStatusWindow', 'explorerMainCanvas', 'explorerPreviewCanvas'],
	function ( jquery, jqueryUI, explorer, stateModule, conditionEventModule, featuresPanelModule, statusWindowModule, mainCanvasModule, previewCanvasModule )
	{
		$( document ).ready( function ()
		{
			explorer.initialize(
				'canvas-main',
				'canvas-preview',
				'features-panel',
				'status-window'
				);
			return;
		} );

		return;
	} );

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
