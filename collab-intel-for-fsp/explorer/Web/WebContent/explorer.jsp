<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<!--
	Built on:     build.time
	SVN revision: svn.revision
-->
<head>
    <title>app.name app.version</title>
    <link rel="icon" type="image/png" href="img/full-tree.png" />
	<link rel="stylesheet" type="text/css" href="css/third-party/jquery-ui.css" />
	<link rel="stylesheet" type="text/css" href="css/third-party/jquery-ui.structure.css" />
	<link rel="stylesheet" type="text/css" href="css/fsp.css" />
	<link rel="stylesheet" type="text/css" href="css/explorer.css" />
	<script data-main="js/explorer-startup" src="js/third-party/require.js"></script>
</head>
<body>
	<canvas id="canvas-main"></canvas>
	<div id="features-panel">Loading features panel, please wait...</div>
	<canvas id="canvas-preview"></canvas>
	<div id="status-window">Loading status, please wait...</div>
	<div style="position:absolute;bottom:2px;right:4px;color:#666;font-size:80%">${explorer.app.name} 0.1 alpha (SVN revision 85M)</div>
</body>
</html>
