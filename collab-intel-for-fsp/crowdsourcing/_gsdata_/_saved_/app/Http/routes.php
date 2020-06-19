<?php

/*
|--------------------------------------------------------------------------
| Application Routes
|--------------------------------------------------------------------------
|
| Here is where you can register all of the routes for an application.
| It's a breeze. Simply tell Laravel the URIs it should respond to
| and give it the controller to call when that URI is requested.
|
*/

Route::get('/', 'HomeController@index');

Route::controllers([
	'auth' => 'Auth\AuthController',
	'password' => 'Auth\PasswordController',
]);

Route::group(['prefix' => 'api'], function () {

	Route::get('/questions/all', 'QuestionController@getAll');
	Route::post('/question/inputs', 'QuestionController@postInputs');
	Route::post('/question/explains', 'QuestionController@postExplains');
	Route::post('/explains/vote', 'QuestionController@postExplainsVote');

});