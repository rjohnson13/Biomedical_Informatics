package JDBCWrapper;

import java.sql.*;
import java.util.*;

public class Table
{
  private Database database;
  private String name;

  public Table( Database database, String name ) {
    this.database = database;
    this.name = name;
  }

  private RowSet execute( String criteria ) throws SQLException {
    Connection con = database.getConnection();
    Statement st = con.createStatement();
    ResultSet rs = st.executeQuery( "select * from "+name+
      (criteria==null?"":(" where "+criteria)) );
    ResultSetMetaData rsmd = rs.getMetaData();
    RowSet rows = new RowSet();
    int cols = rsmd.getColumnCount();
    while (rs.next()) {
      Row row = new Row();
      for (int i=0; i<cols; ++i) {
        String name = rsmd.getColumnName( i+1 );
        String value = rs.getString( i+1 );
        
       
        if(value == null) {
        	value = new String("");
        	//System.out.println("NULL value is changed to empty string !!"); 
        }
        
        row.put( name, value );
      }
      rows.add( row );
    }
    con.close();
    return rows;
  }

	/**
	 *  select specific columns
	 *  parameter colNames: contains all items as well as their occurence frequencies
	 */
  private RowSet execute( String criteria, ArrayList colNames) throws SQLException {
    Connection con = database.getConnection();
    Statement st = con.createStatement();
    
    String query = new String("select ");
    
    // add all columns
    for(int i=0; i<colNames.size();i++) {
    	
    	query = query + " [" + (String)colNames.get(i) + "]";
    }
	query = query + " from " + name;
	//System.out.println("Query command :: " + query + (criteria==null?"":(" where "+criteria))); 
	ResultSet rs = st.executeQuery( query +
      (criteria==null?"":(" where "+criteria)) );

    //ResultSet rs = st.executeQuery( "select * from "+name+
      //(criteria==null?"":(" where "+criteria)) );
      
    ResultSetMetaData rsmd = rs.getMetaData();
    RowSet rows = new RowSet();
    int cols = rsmd.getColumnCount();
    while (rs.next()) {
      Row row = new Row();
      for (int i=0; i<cols; ++i) {
        String name = rsmd.getColumnName( i+1 );
        String value = rs.getString( i+1 );
        
        
        if(value == null) {
        	value = new String("");
        	//System.out.println("NULL value is changed to empty string !!"); 
        }
        
        row.put( name, value );
      }
      rows.add( row );
    }
    con.close();
    return rows;
  }

  public Row getRow( String criteria ) throws SQLException {
    RowSet rs = execute( criteria );
    return rs.get( 0 );
  }

  public RowSet getRows( String criteria ) throws SQLException {
    RowSet rs = execute( criteria );
    return rs;
  }

  
  public RowSet getRows( String criteria, ArrayList colNames) throws SQLException {
    RowSet rs = execute( criteria, colNames );
    return rs;
  }
  

  public RowSet getRows() throws SQLException {
    return getRows( null );
  }

  
  public void putRows(RowSet rows)throws SQLException {
  	for(int i=0; i<rows.length(); i++) {
  		putRow(rows.get(i));
  	}
  }


  public void putRow( Row row ) throws SQLException {
    putRow( row, null );
  }

  public void putRow( Row row, String conditions ) throws SQLException {
    String ss = "";
    if (conditions==null) {
      ss = "INSERT INTO "+name+" VALUES (";
      for (int i=0; i<row.length(); ++i) {
        String v = row.get( i );
        ss += "'"+v+"'";
        if (i != row.length()-1)
          ss += ", ";
      }
      ss += ")";
    } else {
      ss = "UPDATE "+name+" SET ";
      for (int i=0; i<row.length(); ++i) {
        String k = row.getKey( i );
        String v = row.get( i );
        ss += k+"='"+v+"'";
        if (i != row.length()-1)
          ss += ", ";
      }
      ss += " WHERE ";
      ss += conditions;
    }

    //System.out.println( "SS "+ss );
    Connection con = database.getConnection();
    Statement st = con.createStatement();
    st.executeUpdate( ss );
    st.close(); 
    con.close(); 
  }
  
  
    
  	/**
	 *  Function created by Robert.
	 *  parameter sql: an entire SQL statement
         *  return: a RowSet object from the SQL Statement
	 */
    public RowSet executeSQL( String sql ) throws SQLException 
    {
        Connection con = database.getConnection();
        Statement st = con.createStatement();
        ResultSet result = st.executeQuery(sql);

        RowSet rows = new RowSet();
        ResultSetMetaData rsmd = result.getMetaData();
        int cols = rsmd.getColumnCount();
        while (result.next()) {
            Row row = new Row();
            for (int i=0; i<cols; ++i) {
                String name = rsmd.getColumnName( i+1 );
                String value = result.getString( i+1 );
        
       
                if(value == null) {
                    value = new String("");
     
        }
        row.put( name, value );
      }
      rows.add( row );
    }
    con.close();
    return rows;
    }
   
}
