package JDBCWrapper;

import java.util.*;

public class RowSet
{
  private ArrayList arraylist = new ArrayList();

  public RowSet() {
  }

  public void add( Row row ) {
    arraylist.add( row );
  }

  public int length() {
    return arraylist.size();
  }

  public Row get( int which ) {
    return (Row)arraylist.get( which );
  }
  
  public void mergeRowSet(RowSet rows)
  {
  	for(int i=0; i<rows.length(); i++) {
  		
  		arraylist.add(rows.get(i));
  	}
  }
}
