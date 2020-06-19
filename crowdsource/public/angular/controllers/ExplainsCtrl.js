fsp.controller("ExplainsCtrl", ['$scope', '$sce', 'Questions',
    function ($scope, $sce, Questions) {

        $scope.alerts = [];

        $scope.submitting = false;

        $scope.textAnswer = '';
        $scope.textOption1 = '';
        $scope.textOption2 = '';
        $scope.radioExplains = {
            id: false
        };
        $scope.isOther = false;

        $scope.$parent.$watch('selectedQuestion', function (newVal) {
            if (newVal) {
                $scope.resetExplainsForm();
                $scope.alerts = [];
            }
        });

        $scope.makeHTML = function(string) {
            return $sce.trustAsHtml(string);
        };

        $scope.other = function (value) {
          $scope.isOther = value;
        };

        $scope.addSuccess = function() {
            $scope.alerts.push({
                type: 'success',
                msg: 'Success! Your answer has been submitted.'
            });
        };

        $scope.closeAlert = function(index) {
            $scope.alerts.splice(index, 1);
        };

        $scope.submitExplains = function(question) {

            if($scope.isOther){
                if($scope.textAnswer.length > 0 && $scope.textOption1.length > 0 && $scope.textOption2.length > 0) {
                    $scope.submitting = true;
                    Questions.submitExplains(question.id, $scope.textAnswer, $scope.textOption1, $scope.textOption2).success(function (result) {
                        question.details.explains.push(result);
                        $scope.addSuccess();
                        $scope.resetExplainsForm();
                    });
                }
            } else {
                if($scope.radioExplains.id) {
                    $scope.submitting = true;
                    Questions.submitExplainsVote($scope.radioExplains.id).success(function (result) {
                        $scope.addSuccess();
                        $scope.resetExplainsForm();
                    });
                }
            }

        };

        $scope.resetExplainsForm = function () {
            $scope.submitting = false;
            $scope.textAnswer = '';
            $scope.textOption1 = '';
            $scope.textOption2 = '';
            $scope.radioExplains.id = false;
            $scope.isOther = false;
        };


    }
]);