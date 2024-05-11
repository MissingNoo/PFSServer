package pfs;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class ConexaoMySQL {
	public static String status = Server.getTimeStamp() + "[MySQL]  Not connected to database!";
	public static Connection connection;
	
	public ConexaoMySQL() {
		
	}
	
	public static java.sql.Connection getConexaoMySQL() {
		connection = null;
		try {
			String driverName = "com.mysql.cj.jdbc.Driver";
			Class.forName(driverName);
			
			String serverName = "localhost"; 
			String mydatabase = "PFS";
			String url = "jdbc:mysql://" + serverName + "/" + mydatabase;
			String username = "root";
			String password = "";
			connection = DriverManager.getConnection(url, username, password);
			if (connection != null) {
				status = (Server.getTimeStamp() + "[MySQL] Connected to database!");
			} else {
				status = (Server.getTimeStamp() + "[MySQL] Can't connect to database");
			}
			return connection;
		} catch (ClassNotFoundException e) {
			System.out.println(Server.getTimeStamp() + "[MySQL] Driver not Found!");
			return null;
		} catch (SQLException e) {
			System.out.println(Server.getTimeStamp() + "[MySQL] Can't connect to database.");
			return null;
		}
	}
	
	//Método que retorna o status da sua conexão//
	public static String statusConection() {
		return status;
	}
	
	//Método que fecha sua conexão//
	public static boolean FecharConexao() {
		try {
			ConexaoMySQL.getConexaoMySQL().close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
	//Método que reinicia sua conexão//
	public static java.sql.Connection ReiniciarConexao() {
		FecharConexao();
		return ConexaoMySQL.getConexaoMySQL();
	}
	
	static Statement stmt = null;
	static ResultSet rs = null;
	
	public static boolean login(String usr, String password) {
		boolean loggedin = false;
		String query = "SELECT * FROM accounts WHERE user = \"" + usr + "\"";
		try (Statement stmt = connection.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
			  	String pass = rs.getString("password");
				if (pass.toString().compareTo(password.toString()) == 0) {
					loggedin = true;
				}
				else {
					
				}
			}
		}
		catch (SQLException ex){
		}
		return loggedin;
	}

	public static String getCharacters(String username){
		String characters = "";
		String query = "SELECT characters FROM accounts WHERE user = \"" + username + "\"";
		try (Statement stmt = connection.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
			  	characters = rs.getString("characters");
			}
		}
		catch (SQLException ex){
		}
		return characters;
	}
}