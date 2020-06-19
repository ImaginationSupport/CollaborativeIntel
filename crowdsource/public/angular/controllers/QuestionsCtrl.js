fsp.controller("QuestionsCtrl", ['$scope', 'Questions',
    function ($scope, Questions) {

        $scope.template = '/angular/templates/Questions.html';

        $scope.questions = [];
        $scope.selectedQuestion = {};

        Questions.all().success(function(data) {

            angular.forEach(data, function(question){
                question.selected = false;
            });

            $scope.questions = data;

        });

        $scope.selected = function(question) {

            angular.forEach($scope.questions, function(question){
               question.selected = false;
            });

            question.selected = true;
            $scope.selectedQuestion = question;

        }

    }
]);