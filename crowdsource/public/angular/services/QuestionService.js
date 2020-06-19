fsp.factory("Questions", ['$http',
    function ($http) {

        return {


            // get all the dashboards
            all: function () {
                return $http.get('/api/questions/all');
            },

            //Create a new dashboard
            submitInputs: function (questionId, value, confidence) {
                return $http({
                    method: 'POST',
                    url: '/api/question/inputs',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    data: $.param({questionId: questionId, value: value, confidence: confidence})
                });
            },

            submitExplains: function (questionId, textAnswer, textOption1, textOption2) {
                return $http({
                    method: 'POST',
                    url: '/api/question/explains',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    data: $.param({questionId: questionId, textAnswer: textAnswer, textOption1: textOption1, textOption2: textOption2})
                });
            },

            submitExplainsVote: function (explainId) {
                return $http({
                    method: 'POST',
                    url: '/api/explains/vote',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    data: $.param({explainId: explainId})
                });
            }

        }

    }
]);