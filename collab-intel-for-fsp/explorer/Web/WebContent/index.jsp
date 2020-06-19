<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<!--
	Built on:     build.time
	SVN revision: svn.revision
-->
<head>
    <link rel="icon" type="image/png" href="img/full-tree.png" />
	<title>app.name app.version</title>
	<link rel="stylesheet" type="text/css" href="css/fsp.css" />
	<style type="text/css">
	
	#entries-container
	{
		padding: 0.25rem 0.5rem;
	}
	
	table
	{
		width: 90%;
		margin: 0.5rem 1rem;
	}
	
	td.buttonCell
	{
		padding: 2px;
	}
	
	button
	{
		background-color: #c00;
		color: white;
		border: none;
		outline: none;
		padding: 0.2rem 1rem;
		cursor: pointer;
	}
	
	#new-mission-name
	{
		width: 300px;
	}
	
	#new-mission-error-text
	{
		margin-top:	2em;
	}
	
	#app-version
	{
		position: absolute;
		top: 2px;
		right: 2px;
		color: #999;
		font-size: 80%
	}
	
	#main-container
	{
		height: 500px;
		width: 600px;
		margin: 100px auto 10px auto;
		background-color: #f4f4f4;
	}
	
	#header
	{
		background-color: #c00;
		padding: 0.5rem 1rem;
	}
	
	#header-text
	{
		color: white;
		font-size: 2rem;
		margin: 0;
	}
	
	</style>
	<script src="js/third-party/jquery-2.1.3.min.js"></script>
	<script src="js/explorer-select-mission.js"></script>
</head>
<body>
	<div id="app-version">${explorer.app.name} 0.1 alpha (SVN revision 85M)</div>
	<div id="main-container">
		<div id="header"><h1 id="header-text">Welcome to FSP Explorer</h1></div>
		<div id="entries-container"><span style="padding:0.5em">Loading entries, please wait...</span></div>
	</div>
	<div style="width:600px;margin:0 auto">
		<a target="_blank" href="http://ncsu-las.org/"><img src="img/LAS-logo.png" style="float:right" /></a>
		<a target="_blank" href="http://www.ara.com"><img src="img/ARA-logo-blue.png" style="float:left" /></a>
	</div>
</body>
</html>
