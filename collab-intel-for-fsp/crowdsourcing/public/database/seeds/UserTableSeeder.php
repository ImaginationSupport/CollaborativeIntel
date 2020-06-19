<?php

use FSP\User;
use Illuminate\Database\Seeder;
use Illuminate\Database\Eloquent\Model;

class UserTableSeeder extends Seeder {

	/**
	 * Run the database seeds.
	 *
	 * @return void
	 */
	public function run()
	{
		DB::table('users')->delete();
		DB::table('password_resets')->delete();

		$demo = new User;
		$demo->name = 'demo';
		$demo->email = 'demo2015@ara.com';
		$demo->password = bcrypt('demo2015');
		$demo->save();
	}

}
