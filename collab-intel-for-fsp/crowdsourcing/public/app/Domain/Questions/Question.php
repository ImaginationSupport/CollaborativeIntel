<?php namespace FSP\Domain\Questions;

use Illuminate\Database\Eloquent\Model;

class Question extends Model {

	/**
	 * The database table used by the model.
	 *
	 * @var string
	 */
	protected $table = 'ciquestions';

	public $timestamps = false;

	const QUESTION_TYPE_INPUT = 'FEATURE';
	const QUESTION_TYPE_EXPLAINS = 'CONDITION';

	public function condition()
	{
		return $this->hasOne('FSP\Domain\Questions\Condition', 'questionid');
	}

	public function explains()
	{
		return $this->hasMany('FSP\Domain\Questions\Explain', 'questionid');
	}

	public function inputs()
	{
		return $this->hasMany('FSP\Domain\Questions\Input', 'questionid');
	}

	public function details()
	{
		$details = [
			'inputs' => [],
			'explains' => [],
			'condition' => null
		];

		if($this->questiontype == self::QUESTION_TYPE_INPUT)
		{
			$inputs = $this->inputs()->get();
			if(!$inputs->isEmpty())
				$details['inputs'] = $inputs->toArray();
		}
		elseif ($this->questiontype == self::QUESTION_TYPE_EXPLAINS)
		{
			$condition = $this->condition()->first();
			if($condition)
				$details['condition'] = $condition->toArray();

			$explains = $this->explains()->get();
			if(!$explains->isEmpty())
				$details['explains'] = $explains->toArray();
		}

		return $details;
	}

	public function getConfidenceWeightedAverage()
	{
		$answers = $this->inputs()->get();

		if($answers->isEmpty())
			return ['result' => null, 'confidence' => null];

		$answersWeighted = [];
		$confidencesWeighted = [];
		$weightedAmount = [];
		foreach($answers as $answer) {
			if(is_numeric($answer->value)){
				$value = floatval($answer->value);
				$confidence = floatval($answer->confidence);
				$answersWeighted[] = $value * $confidence;
				$confidencesWeighted[] = $confidence * $confidence;
				$weightedAmount[] = $confidence;
			}
		}

		$aSum = array_sum($answersWeighted);
		$cSum = array_sum($confidencesWeighted);
		$wSum = array_sum($weightedAmount);

		if($wSum === 0){
			$r = 0;
			$c = 0;
		} else {
			$r = $aSum/$wSum;
			$c = $cSum/$wSum;
		}

		return ['result' => $r, 'confidence' => $c];
	}

}
