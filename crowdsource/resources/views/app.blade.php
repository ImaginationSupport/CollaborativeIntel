<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>ARA FSP</title>

	<link href="{{ asset('/css/app.css') }}" rel="stylesheet">
	<link href="{{ asset('/bower_components/bootstrap/dist/css/bootstrap.min.css') }}" rel="stylesheet">
	<link href="{{ asset('/bower_components/angular-rangeslider/angular.rangeSlider.css') }}" rel="stylesheet" >

	<!-- Fonts -->
	<link href='//fonts.googleapis.com/css?family=Roboto:400,300' rel='stylesheet' type='text/css'>

	<!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
	<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
	<!--[if lt IE 9]>
		<script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
		<script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
	<![endif]-->
</head>
<body>

	<nav class="navbar navbar-default">
		<div class="container">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
					<span class="sr-only">Toggle Navigation</span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="#">ARA Collaborative Intelligence Crowd Interface</a>
			</div>

			<div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
				<ul class="nav navbar-nav navbar-right">
					@if (Auth::guest())
						<li><a href="{{ url('/auth/login') }}">Login</a></li>
						<li><a href="{{ url('/auth/register') }}">Register</a></li>
					@else
						<li class="dropdown">
							<a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">{{ Auth::user()->name }} <span class="caret"></span></a>
							<ul class="dropdown-menu" role="menu">
								<li><a href="{{ url('/auth/logout') }}">Logout</a></li>
							</ul>
						</li>
					@endif
				</ul>
			</div>
		</div>
	</nav>

	@yield('content')

	<footer class="footer">
		<div class="container">
			<p class="text-muted">&copy; 2015 Applied Research Associates, Inc. All Rights Reserved.</p>
		</div>
	</footer>
	<!-- Scripts -->
	<script src="{{ asset('/bower_components/jquery/dist/jquery.js') }}"></script>
	<script src="{{ asset('/bower_components/bootstrap/dist/js/bootstrap.min.js') }}"></script>
	<script src="{{ asset('/bower_components/moment/min/moment.min.js') }}"></script>
	<script src="{{ asset('/bower_components/angular/angular.min.js') }}"></script>
	<script src="{{ asset('/bower_components/angular-resource/angular-resource.min.js') }}"></script>
	<script src="{{ asset('/bower_components/angular-bootstrap/ui-bootstrap.min.js') }}"></script>
	<script src="{{ asset('/bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js') }}"></script>
	<script src="{{ asset('/bower_components/angular-rangeslider/angular.rangeSlider.js') }}"></script>
	<!-- Angular Scripts -->
	<script src="{{ asset('/angular/app.modules.js') }}"></script>
	<script src="{{ asset('/angular/controllers/ExplainsCtrl.js') }}"></script>
	<script src="{{ asset('/angular/controllers/InputsCtrl.js') }}"></script>
	<script src="{{ asset('/angular/controllers/QuestionsCtrl.js') }}"></script>
	<script src="{{ asset('/angular/directive/ExplainsDirective.js') }}"></script>
	<script src="{{ asset('/angular/directive/InputsDirective.js') }}"></script>
	<script src="{{ asset('/angular/services/QuestionService.js') }}"></script>
</body>
</html>
