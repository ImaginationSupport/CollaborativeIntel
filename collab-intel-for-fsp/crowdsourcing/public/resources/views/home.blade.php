@extends('app')

@section('content')
<div class="container" ng-app="fsp">
    <div class="row" ng-controller="QuestionsCtrl" ng-include="template"></div>
</div>
@endsection
