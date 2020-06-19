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
	<link rel="stylesheet" type="text/css" href="css/crowd.css" />
	<script src="js/third-party/jquery-2.1.3.min.js"></script>
	<script src="js/crowd-admin.js"></script>
</head>
<body>
	<div id="app-version">${crowd.app.name} 0.1 alpha (SVN revision 85M)</div>
	<div id="main-container">
		<div id="left-column-holder" class="column-holder">
			<div class="column-title">Questions</div>
			<div id="left-column">
				<div class="simple-message-holder">Loading questions, please wait...</div>
			</div>
		  </div>
		<div id="right-column-holder" class="column-holder">
			<div class="column-title">Setup</div>
			<div id="right-column">
				<div class="simple-message-holder">Select the question to the left.</div>
			</div>
		  </div>
	</div>
	<div id="footer">
		<a target="_blank" href="http://www.ara.com"><img src="img/ARA-logo-blue.png" class="logo-image" /></a>
		<a target="_blank" href="http://ncsu-las.org/"><img src="img/LAS-logo.png" class="logo-image" /></a>
	</div>
</body>
</html>
