<?php namespace FSP\Domain\Questions;

use Illuminate\Database\Eloquent\Model;

class Input extends Model {

	/**
	 * The database table used by the model.
	 *
	 * @var string
	 */
	protected $table = 'cicrowdinput';

	public $timestamps = false;

	public function user()
	{
		return $this->belongsTo('FSP\User');
	}
}
