package JDBCWrapper;

import java.sql.*;

public class Database
{
  private String uri, username, password;

  public Database( String uri, String username, String password ) {
    this.uri = uri;
    this.username = username;
    this.password = password;
  }

  public Table getTable( String name ) {
    return new Table( this, name );
  }

  Connection getConnection() throws SQLException {
    Connection connection =
      DriverManager.getConnection( uri, username, password );
    return connection;
  }
}