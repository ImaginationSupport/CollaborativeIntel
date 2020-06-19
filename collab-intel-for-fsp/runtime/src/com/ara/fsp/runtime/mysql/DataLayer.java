//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DataLayer {
	
	private static DataLayer self=null;
	private Connection connect=null;
	private String jdbc=null;
	
	public void setJdbc(String jdbc){
		this.jdbc=jdbc;
	}
	
	private DataLayer(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("ERROR: unable to load JDBC Driver.");
			e.printStackTrace();

		}
	}
	
	public void setConnectionFromJNDI( final Connection existingConnection )
	{
		if( existingConnection == null )
			return;
		
		connect = existingConnection;
		
		return;
	}
	
	public Connection connect() {
		if (connect==null){
			try{
				connect = DriverManager.getConnection(jdbc);
			} catch (SQLException e) {
				System.err.println("ERROR: unable to connect to db: "+jdbc);
				e.printStackTrace();
			}
		}
		return connect;
	}
	
	public void close() {
		if (connect != null) {
			try {
				connect.close();
			} catch (SQLException e) {
				System.err.println("ERROR: Cannot close DB connection.");
				e.printStackTrace();
			}
		}
	}

	public static DataLayer getInstance(){
		if (self==null)
			self=new DataLayer();
		return self;
	}
	
}
