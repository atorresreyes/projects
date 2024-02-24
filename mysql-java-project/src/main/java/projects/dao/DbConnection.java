package projects.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import projects.exception.DbException;


public class DbConnection {
	
	private static final String HOST = "localhost";
	private static final int PORT = 3306;
	private static final String USER = "projects";
	private static final String PASSWORD = "projects";
	private static final String SCHEMA = "projects";
	
	
	
	public static Connection getConnection() {
		String uri = String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s&useSSL=false", HOST, PORT, SCHEMA, USER, PASSWORD);
		
		try {
			Connection myConnection = DriverManager.getConnection(uri);
			System.out.println("Connection Successful!");
			return myConnection;
		} catch (SQLException e) {
			System.out.println("Error: Connection Failed.");
			throw new DbException(e);
		}
	
	}
	
	/*
	 * 

2.     In the DbConnection class, create a method named getConnection(). It should be public and static and should return a java.sql.Connection object. In the getConnection() method:

a.     Create a String variable named uri that contains the MySQL connection URI.

b.     Call DriverManager to obtain a connection. Pass the connection string (URI) to DriverManager.getConnection().

c.      Surround the call to DriverManager.getConnection() with a try/catch block. The catch block should catch SQLException.

d.     Print a message to the console (System.out.println) if the connection is successful.

e.     Print an error message to the console if the connection fails. Throw a DbException if the connection fails.

	 */
	
	
	
	

} //public class end
