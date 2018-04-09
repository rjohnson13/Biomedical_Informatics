package JDBCWrapper;

import java.util.*;

public class Row
{
  private ArrayList ordering = new ArrayList();
  private HashMap hashmap = new HashMap();

  public Row() {
  }

  public void put( String name, String value ) {
    if (!hashmap.containsKey( name))
      ordering.add( name );
    hashmap.put( name, value );
  }

  public int length() {
    return hashmap.size();
  }

  public String get( String name ) {
    return (String)hashmap.get( name );
  }

  public String get( int which ) {
    String key = (String)ordering.get( which );
    return (String)hashmap.get( key );
  }

  public String getKey( int which ) {
    String key = (String)ordering.get( which );
    return key;
  }

}
