<?php namespace FSP\Http\Controllers;

use Auth;

use FSP\Domain\Questions\Question;
use FSP\Domain\Questions\Input;
use FSP\Domain\Questions\Explain;
use Illuminate\Http\Request;

class WebServiceController extends Controller {

	public function __construct()
	{
		$this->middleware('jwt.auth');
	}

    public function getResult(Question $question)
    {
        return response()->json($question->getConfidenceWeightedAverage(), 200);
    }

    public function getResults(Question $question)
    {
        $questions = $question->all();

        if($questions->isEmpty())
            return response()->json([], 200);

        $results = [];
        foreach($questions as $question)
            $results[] = array_merge(['questionId' => $question->id], $question->getConfidenceWeightedAverage());

        return response()->json($results, 200);
    }

    public function getAnswer(Question $question)
    {
        $inputs = $question->inputs()->get();

        if($inputs->isEmpty())
            return response()->json([], 200);

        return response()->json($inputs->toArray(), 200);
    }

    public function getAnswers(Question $question)
    {
        $questions = $question->all();

        if($questions->isEmpty())
            return response()->json([], 200);

        $results = [];
        foreach($questions as $question) {
            $iter = [
                'questionId' => $question->id,
                'answers' => []
            ];

            $inputs = $question->inputs()->get();

            if($inputs->isEmpty()) {
                $results[] = $iter;
            } else {
                $iter['answers'] = $inputs->toArray();
                $results[] = $iter;
            }
        }

        return response()->json($results, 200);
    }

    public function getQuestion(Question $question)
    {
        if(!$question || $question->questiontype != 'FEATURE')
            return response()->json([], 200);

        return response()->json($question->toArray(), 200);
    }

    public function getQuestions(Question $question)
    {
        $questions = $question->where('questiontype', 'FEATURE')->get();

        if($questions->isEmpty())
            return response()->json([], 200);

        return response()->json($questions->toArray(), 200);
    }
}
