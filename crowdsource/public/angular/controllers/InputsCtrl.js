fsp.controller("InputsCtrl", ['$scope', '$sce', 'Questions',
    function ($scope, $sce, Questions) {

        $scope.confidence = {
            range: {
                min: 0,
                max: 100
            },
            min: 0,
            max: 50
        };

        $scope.alerts = [];

        $scope.submitting = false;

        $scope.textInput = '';

        $scope.$parent.$watch('selectedQuestion', function (newVal) {
            if (newVal) {
                $scope.resetInputsForm();
                $scope.alerts = [];
            }
        });

        $scope.makeHTML = function(string) {
            return $sce.trustAsHtml(string);
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

        $scope.submitInputs = function(question) {

            $scope.submitting = true;
            Questions.submitInputs(question.id, $scope.textInput, $scope.confidence.max).success(function(result){
                $scope.addSuccess();
                $scope.resetInputsForm();
            });

        };

        $scope.resetInputsForm = function () {
            $scope.submitting = false;
            $scope.textInput = '';
            $scope.confidence.max = 50;
        };

    }
]);