<?php namespace FSP\Http\Controllers;

use Auth;

use FSP\Domain\Questions\Question;
use FSP\Domain\Questions\Input;
use FSP\Domain\Questions\Explain;
use Illuminate\Http\Request;

class QuestionController extends Controller {

	public function __construct()
	{
		$this->middleware('auth');
	}

	public function getAll(Question $question)
	{
		$questions = $question->where('active', 1)->get();

		$rtn = [];
		if(!$questions->isEmpty())
		{
			foreach($questions as $question)
			{
				$details = $question->details();
				$arr = $question->toArray();
				$arr['details'] = $details;
				$rtn[] = $arr;
			}
		}

		return response()->json($rtn, 200);
	}

	public function postInputs(Request $request)
	{
		$input = new Input;
		$input->questionid = $request->input('questionId');
		$input->user = Auth::id();
		$input->value = $request->input('value');
		$input->date = new \DateTime();
		$input->confidence = $request->input('confidence');
		$input->save();

		return response()->json(['result' => 'success'], 200);
	}

	public function postExplains(Request $request)
	{
		$explain = new Explain;
		$explain->questionid = $request->input('questionId');
		$explain->eventlabel = $request->input('textAnswer');
		$explain->eventdesc = '';
		$explain->option1 = $request->input('textOption1');
		$explain->option2 = $request->input('textOption2');
		$explain->votes = 1;
		$explain->save();

		return response()->json($explain->toArray(), 200);
	}

	public function postExplainsVote(Request $request, Explain $explain)
	{
		$explain = $explain->find($request->input('explainId'));
		$explain->votes = $explain->votes + 1;
		$explain->save();

		return response()->json(['result' => 'success'], 200);
	}

}
