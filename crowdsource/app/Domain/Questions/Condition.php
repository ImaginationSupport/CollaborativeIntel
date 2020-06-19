<?php namespace FSP\Domain\Questions;

use Illuminate\Database\Eloquent\Model;

class Condition extends Model {


	/**
	 * The database table used by the model.
	 *
	 * @var string
	 */
	protected $table = 'ciconditions';

	public $timestamps = false;

	public function question()
	{
		return $this->belongsTo('FSP\Domain\Questions\Question', 'questionid', 'id');
	}

}
