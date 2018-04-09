
/**
 * @author Robert Johnson
 * @file MUTARA.java
 * @use user types in drug the want to find, then gets back
 *      top 10 unexpected temporal associated reactions
 */
package mutara;

import JDBCWrapper.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;



import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MUTARA {
    static String dbURL = "jdbc:mysql://********"; //define connection URL
    static String username = "*******";
    static String password = "********";
    
    static String demoTable = ("drug08Q1");
    static String drugTable = ("drug08Q1");
    static String ADRTable = ("drug08Q1");
    
    //reference period, in months
    static int reference_period = 12;
        
        
    public static void main(String[] args) {
        //Gets the antededent. (searched for drug)
     
        Scanner scanner = new Scanner(System.in);
        System.out.println("what drug do you want to find ADRs for?");
        String drug = scanner.nextLine();
        System.out.println("Scanning now for " + drug);
        
        //Sets up database connection
        Database db = db_connect();

        //Finds and prints out the databas echaracteristics we need, 
        //such as male:female ratio or average age
        patient_Data_Characteristics(drug, db);
        
        //Gets everyone who uses searched for drug, and saves them as a user sequence of events.
        ArrayList user = get_user(drug, db);
        Map<String, TreeMap<LocalDate, ArrayList<String>>> users = convert_user_to_sequence(drug,user,db);

        //Prunes out expected reactons as per MUTARA algorithm
        Map<String, TreeMap<LocalDate, ArrayList<String>>> pruned_users = prune_expected(users);
        
        //Calculates our unexpected temporal associations
        Map<String, Double> unexpected_supports = calculate_unexpected_support(drug,pruned_users, db);
        
        display_UTAR(unexpected_supports, 10);

    }//end of main
    
    
    //Connects to the databse and returns a DataBase object
    public static Database db_connect()
    {
        
        try {
            Class.forName("com.mysql.jdbc.Driver"); // load JDBC driver
        } catch (Exception e) {
            System.out.println("Failed to load JDBC/ODBC driver.");
        }
        Database Db = new Database(dbURL, username, password);
        return Db;
    }//end of db_connect
    
    
    public static void patient_Data_Characteristics(String drugName ,Database db)
    {
        String statement =  "SELECT demo08Q1.* " +
                            "FROM demo08Q1, drug08Q1  " +
                            "WHERE AGE != 0  AND DRUGNAME LIKE '" + drugName + "' " +
                            "AND demo08Q1.ISR = drug08Q1.ISR " +
                            "GROUP BY ISR; ";

        Table tbl = new Table(db, drugTable);
        RowSet rs = null;
        try {
            rs = tbl.executeSQL(statement);
        } catch (Exception e) {
            System.err.println("problems getting rows: "+ e.toString());
        }

        int num_patients = rs.length();
        double average_age = get_average_age(rs);
        double std_age = get_std_age(rs, average_age);
        double male_female_ratio = get_male_female_ratio(rs);
        System.out.println("Total number of patients taking " + drugName +  " is:");
        System.out.println(num_patients);
        System.out.println("Average age among " + drugName + " users is:");
        System.out.println(average_age);
        System.out.println("The standard deviation of this age is:");
        System.out.println(std_age);
        System.out.println("The male to female ratio among " + drugName + " users is :");
        System.out.println(male_female_ratio);
  
    
    }//end of patient_data_characteristics

    public static double get_average_age(RowSet rs)
    {
        Row row;
        Double sum = 0.0;
        for(int i = 0; i < rs.length(); i++){
            row = rs.get(i);
            sum = sum + convert_age(row);
        } 
        Double avg = sum/rs.length();
        return avg;
    }//end of get_average_age



    public static double get_std_age(RowSet rs, double mean)
    {
        Double std;
        Double sum = 0.0;
        Row row;

        for(int i = 0; i < rs.length(); i++){
            row = rs.get(i);
            sum += Math.pow((convert_age(row)-mean),2);
        }
        std = Math.sqrt(sum/(rs.length() - 1));
        return std;

      } //end of get_std_age
    
    
    public static Double convert_age(Row row)
        {    
        switch (row.get("AGE_COD")){
                case "DEC":
                    return Double.valueOf(row.get("AGE"))*10;  
                case "YR":
                    return Double.valueOf(row.get("AGE"));         
                case "MON":
                    return Double.valueOf(row.get("AGE"))/12;
                case "WK":
                    return Double.valueOf(row.get("AGE"))/52;
                case "DY":
                    return Double.valueOf(row.get("AGE"))/365;
                case "HR":
                    return Double.valueOf(row.get("AGE"))/8760;

            }
        return 0.0;
    }//end of convert_age
  
    //Might be some problems, due to not every report being male or female
    public static Double get_male_female_ratio(RowSet rs)
    {
        Double male_sum = 0.0;
        Double female_sum = 0.0;
        Row row;
        
        for(int i = 0; i < rs.length(); i++){
            row = rs.get(i);
            if (row.get("GNDR_COD").equals("M"))
                male_sum++;
            else
                female_sum++;
        }    
        return male_sum/female_sum;
    }
  

    //added method to get the total number of patients  -Ryan
    public static int get_num_of_patients(RowSet rs) 
    {
        return rs.length();
    }
    
    //Gets RowSet of user and nonuser from database
    public static ArrayList get_user(String drugName, Database db)
    {
        //Gets case number of people who use the drug
        String statement =  "SELECT CASE_NUM " +
                    "FROM drug08Q1, demo08Q1  " +
                    "WHERE DRUGNAME LIKE '" + drugName + "' " +
                    "AND drug08Q1.ISR = demo08Q1.ISR;";
        
        RowSet user = null;
        Table tbl = new Table(db, drugTable);
         
        //gets users
        try
        {
            user = tbl.executeSQL(statement);
        }catch (Exception e) {
            System.err.println("problems getting rows: "+ e.toString());
        }

        //converts rowset to an Arraylist of Case numbers;
       ArrayList<String> user_list = new ArrayList<>();
       Row row = null;
       for(int i = 0; i < user.length(); i++)
       {
           row = user.get(i);
           user_list.add(row.get("CASE_NUM"));
       }
       
       return user_list;
    }
    

    
    //Converts the user and non-user RowSets into a map of users (sorted by case number)
    public static Map<String, TreeMap<LocalDate, ArrayList<String>>> convert_user_to_sequence(String drugName, ArrayList user_list, Database db)
    {
        //Statement to get all users that take the drug
        String statement =  "SELECT REPT_DT, CASE_NUM, PT " +
                    "FROM drug08Q1, demo08Q1, reac08Q1 " +
                    "WHERE DRUGNAME LIKE '" + drugName + "%' " +
                    "AND drug08Q1.ISR = demo08Q1.ISR " +
                    "AND drug08Q1.ISR = reac08Q1.ISR;";
  
        RowSet ISRs = null;
        Table tbl = new Table(db, drugTable);
        //gets everyone
        try
        {
            ISRs = tbl.executeSQL(statement);
        }catch (Exception e) {
            System.err.println("problems getting rows: "+ e.toString());
        }

        //Sorts people by those who took the drug and those who didn't.
        //Sorts by case number.
        Map<String, TreeMap<LocalDate, ArrayList<String>>> users = new HashMap();
        Row row = null;
        for (int i = 0; i < ISRs.length(); i++)
        {
            row = ISRs.get(i);
            
            //first check if the date is null.
            //data is meaningless is date is null. Cant make a sequence from it
            if(row.get("REPT_DT") != null && !row.get("REPT_DT").equals(""))
            {
                //Makes our user map.
                //Key = Case number
                //Value = Treemap of dates and reations
                if (user_list.contains(row.get("CASE_NUM")))
                {
                    //new user. Adds new treemap with date and reaction
                    if (!users.containsKey(row.get("CASE_NUM")))
                    {
                        TreeMap<LocalDate,ArrayList<String>> t = new TreeMap();
                        ArrayList<String> reactions = new ArrayList();
                        reactions.add(row.get("PT"));
                        t.put(get_localDate(row.get("REPT_DT")),reactions);
                        users.put(row.get("CASE_NUM"), t);

                    }
                    //existing user. Updates treemap with additional date and reaction
                    else
                    {
                        LocalDate ld = get_localDate(row.get("REPT_DT"));
                        TreeMap<LocalDate, ArrayList<String>> t = users.get(row.get("CASE_NUM"));
                       
                        if(t.containsKey(ld))
                        {
                            t.get(ld).add(row.get("PT"));
                        }
                        else
                        {
                            ArrayList<String> a = new ArrayList();
                            a.add(row.get("PT"));
                            t.put(ld, a);
                        }  
                        users.put(row.get("CASE_NUM"), t);

                    }

                }

            }//end of row.get("REPT_DT") != null
            
        }//end of person ISR loop
        return users;
    }
    
    
    //Converts the date in our database to a sortable LocalDate format
    public static LocalDate get_localDate(String date)
    {
        //format recieved from database.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate ld = LocalDate.parse(date, formatter);
        return ld;
    }
     
    
    //method that prunes out reactions that occur within our reference period
    //Might not work for our dataset, but still making it
    //Will add in test people later to prove it works
    //For now, have our reference period be set to 1 year
    //Our test period is the whole list, given that we do not have a lot of data.
    public static Map<String, TreeMap<LocalDate, ArrayList<String>>> prune_expected(Map<String, TreeMap<LocalDate, ArrayList<String>>> users)
    {
        //Return Map. Populate it with pruned results
        Map<String, TreeMap<LocalDate, ArrayList<String>>> pruned_results = new HashMap();
        
        //Runs through each case number in the map
        for (String case_num : users.keySet())
        {
            //Gets the treemap of dates and reactions
            TreeMap<LocalDate,ArrayList<String>> t = users.get(case_num);
            //Makes an array list to store reactions of a single user
            ArrayList<String> expected_reactions = new ArrayList();
            //makes a treemap to store unexpected reactions to be put in pruned_results
            TreeMap<LocalDate, ArrayList<String>> unexpected_reactions = new TreeMap();

            //runs through each date in the treemap.
            for (LocalDate rept_dt : t.keySet())
            {
                //For each date, store the reactions that happened. For later dates, remove reactions that happened in earlier dates
                //gets array list of reactions for a given date
                ArrayList<String> reactions = t.get(rept_dt);
                
                //loops through reactions at the date
                for (int i = 0; i < reactions.size(); i++)
                {
                    if (!expected_reactions.contains(reactions.get(i)))
                    {    
                        //populates unexpected reactions
                        //We don't want to have our first reports be unexpected, so only add after 1
                        if(i > 1)
                        {
                            if(unexpected_reactions.containsKey(rept_dt))
                            {
                                unexpected_reactions.get(rept_dt).add(reactions.get(i));
                            }
                            else
                            {
                                ArrayList<String> a = new ArrayList();
                                a.add(reactions.get(i));
                                unexpected_reactions.put(rept_dt, a);
                            }    
                        }
                        
                        //we saw the event. so lets make it expected.
                        expected_reactions.add(reactions.get(i));
                    }

                }//done looping through reactions for a single date
            }//done looping through all dates for a single patient
            pruned_results.put(case_num, unexpected_reactions);
            
        }//done looping through each case in the users map
        
        return pruned_results;
    }
    
    
    
    public static Map<String, Double> calculate_unexpected_support(String drugName, Map<String, TreeMap<LocalDate, ArrayList<String>>> users, Database db)
    {
        Map<String, Double> reaction_occurance = new HashMap();
        int num_subsequences = 0;
        
        //Count total occurance of each reaction
        //divide by total subsequences
        
        //runs through each user
        for (String case_num : users.keySet())
        {
            //Gets the treemap of dates and reactions
            TreeMap<LocalDate,ArrayList<String>> t = users.get(case_num);
            //runs through each date for each user
            for (LocalDate rept_dt : t.keySet())
            {
                //gets array list of reactions for a given date
                ArrayList<String> reactions = t.get(rept_dt);
                //runs through each reation for each date
                for (int i = 0; i < reactions.size(); i++)
                {
                    String reaction = reactions.get(i);
                    //if we have seen it before, increment its map value
                    if (reaction_occurance.containsKey(reaction))
                    {
                        Double tmp = reaction_occurance.get(reaction);
                        reaction_occurance.put(reaction, tmp + 1);
                    }
                    //else add a new entry
                    else
                    {
                        reaction_occurance.put(reaction, 1.0);
                    }
                }                
                
                num_subsequences++;
            }
        }
        
        //Computes the support by dividing the count by the # of subsequences
        for (String reaction : reaction_occurance.keySet())
        {
            Double tmp = reaction_occurance.get(reaction);
            reaction_occurance.put(reaction, tmp/num_subsequences);
        }

        //Use some stuff I found online to sort map
        Map<String, Double> sorted_map = sortByValues(reaction_occurance);
        return sorted_map;
    }//end of calculate support
    
    //using a sorting method I found online
    //https://beginnersbook.com/2013/12/how-to-sort-hashmap-in-java-by-keys-and-values/
    private static HashMap sortByValues(Map map) { 
       List list = new LinkedList(map.entrySet());
       // Defined Custom Comparator here
       Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
               return ((Comparable) ((Map.Entry) (o2)).getValue())
                  .compareTo(((Map.Entry) (o1)).getValue());
            }
       });

       // Here I am copying the sorted list in HashMap
       // using LinkedHashMap to preserve the insertion order
       HashMap sortedHashMap = new LinkedHashMap();
       for (Iterator it = list.iterator(); it.hasNext();) {
              Map.Entry entry = (Map.Entry) it.next();
              sortedHashMap.put(entry.getKey(), entry.getValue());
       } 
       return sortedHashMap;
  }//end of SortByValues
    
    public static void display_UTAR(Map<String, Double> map, int num)
    {
        Set set = map.entrySet();
        Iterator iterator = set.iterator();       
        int i = 0;
        while(iterator.hasNext() && i <= num)
        {
            Map.Entry me = (Map.Entry)iterator.next();
            System.out.println("The support of " + me.getKey() + " is " + me.getValue());
            i++;
        }
    }

}//end of class
