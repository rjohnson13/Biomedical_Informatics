/*
 *@author Robert Johnson
* @file Assignment11.java
*/

package assignment11;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.math.*;
import org.apache.commons.lang3.ArrayUtils;
//import used to count # of substring matches in string
import org.apache.commons.lang3.StringUtils;




public class Assignment11 {

    public static void main(String[] args) throws IOException {
        
        //Reads all Cuis whose scores are larger than 700 from a metamap file folder
        final File MetaMapFolder = new File("C:/data/Assignment11/MetaMapFiles");

        //Gets Query from txt file and reads it as a String
        String QueryFilePath = "C:/data/Assignment11/query.txt";
        String Query = getStringFromFile(QueryFilePath);
        String[] conceptsArray = Query.split(" ");
        
        //Gets our Relevant text names
        String goldStandardFilePath = "C:/data/Assignment11/goldstandard.txt";
        String goldStandard = getStringFromFile(goldStandardFilePath);

 
        
        //Sets up some arrays and ints
        final File CuiFolder = new File("C:/data/Assignment11/CuiFiles");
        int numCuiFiles = CuiFolder.listFiles().length; 
        int numConcepts = conceptsArray.length;
        Integer[][] containsArray = new Integer[numCuiFiles][numConcepts];
        int[] conceptsInDoc = new int[numCuiFiles]; 
        Map<BigDecimal,String> fileNamesAndWeights = new HashMap<>();
        int[] docsThatContainConcept = new int[numConcepts];
        String[] fileNames = new String[numCuiFiles];
        
        //Goes through each Cui files and maps each occurance of each concept
        int j = 0;
        for (final File fileEntry : CuiFolder.listFiles()) {
            String CuiFilePath = fileEntry.getAbsolutePath();
            String Cuis = getStringFromFile(CuiFilePath);
            for(int i = 0; i < numConcepts; i++)
            {    //Counts amount of matches of each concept to each Cui file 
                 //and stores it in array
                containsArray[j][i] = Cuis.split(conceptsArray[i]).length-1;
                
                //Gets number of docs that contain a concept
                if(containsArray[j][i] > 0)
                    docsThatContainConcept[i]++;
            }
            //Gets number of concepts in a Cui file
            conceptsInDoc[j] = Cuis.split(" ").length;
            fileNames[j] = CuiFilePath.substring(30);
            j++;    
        }

        //Computes the A*TF*IDF for each file and stores in an array
        BigDecimal[] ATFIDFArray = new BigDecimal[numCuiFiles];
        for(int i = 0; i < numCuiFiles; i++)
        {
            BigDecimal sumTFIDF = new BigDecimal(0);
            for(int k = 0; k < numConcepts; k++)
            {
                BigDecimal T = new BigDecimal(containsArray[i][k]);
                BigDecimal F = new BigDecimal(conceptsInDoc[i]);
                T = T.divide(F, 1000, RoundingMode.HALF_DOWN);
                BigDecimal IDF = new BigDecimal(Math.log(numCuiFiles/docsThatContainConcept[k]));
                BigDecimal TFIDF = T.multiply(IDF);

                sumTFIDF = sumTFIDF.add(TFIDF);
            }    
            ATFIDFArray[i] = sumTFIDF;
        }

        //maps file name to A*TF*IDF weight value
        for(int i = 0; i < numCuiFiles; i++)
        {
            fileNamesAndWeights.put(ATFIDFArray[i],fileNames[i]);
        }

        //Sorts items by Weight by turing it into a tree map.
        Map<BigDecimal,String> sortedMap = new TreeMap<>(Collections.reverseOrder());
        sortedMap.putAll(fileNamesAndWeights);
        
        //Converts sorted map into List that we can index and reference.
        List<String> list = new ArrayList<>(sortedMap.values());
        
        
        //Finds P@10
        double p10 = 0;
        for(int i = 0; i <10; i++)
        {
            if (goldStandard.contains(list.get(i)))
                p10++;
        }
        double Pat10 = (p10/10);
        
        //Finds P@20
        double p20 = 0;
        for(int i = 0; i <20; i++)
        {
            System.out.println(list.get(i));
            
            if (goldStandard.contains(list.get(i)))
                p20++;
        }
        Double Pat20 = (p20/20);
        
        //Finds AveP
        double paverage = 0;
        double AveP = 0;
        for(int i = 0; i <list.size(); i++)
        {
            if (goldStandard.contains(list.get(i)))
            {
                paverage++;
                AveP = AveP+(paverage/i);
            }
            
        }

        AveP = AveP/(paverage);
        
        System.out.print("P@10 = ");
        System.out.println(Pat10);
        System.out.print("P@20 = ");
        System.out.println(Pat20);
        System.out.print("AveP = ");
        System.out.println(AveP);
    }
    
    
    

    public static String getStringFromFile(String filePath) throws IOException{
        InputStream is = new FileInputStream(filePath);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();
        
        while(line != null){
            sb.append(line).append("\n");
            line = buf.readLine();
        }
        
        String fileString = sb.toString();
        return fileString;
    }
    
   
    
    
    //Recieved from assignment. Not original code
    public static void convertMmFileToCuiFiles(String metaMapFilePath, String cuiFilePath) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(new
        FileInputStream(metaMapFilePath)));
        File cuiFile = new File(cuiFilePath);
        PrintWriter out = new PrintWriter(cuiFile);
        String cui = null;
        String line = null;
        boolean sentenceReadFlag = false;
        try{
            while (true)
            {
                if(!sentenceReadFlag)
                    line = br.readLine();
                if (line == null) // end of file
                {
                    break;
                } else if (line.trim().length() == 0)
                {
                    continue;
                } else
                {
                    StringTokenizer st = new StringTokenizer(line);
                    String token = st.nextToken();
                    if(token.equals("Processing")) //output new line after processing every line
                    {
                        if(cui != null)
                            out.print("\n");
                        line = br.readLine();
                        sentenceReadFlag = false;
                        if(line.trim().length() == 0)
                            continue;
                        st = new StringTokenizer(line);
                        token = st.nextToken();
                    }
                    if (token.equals("Phrase:"))
                    {
                        line = br.readLine(); //read next line after the Phrase line
                        if(line.startsWith("Processing"))//it's done for one sentence
                        {
                            sentenceReadFlag = true;
                            continue;
                        }
                        else
                            sentenceReadFlag = false;
                        if(line.trim().length() != 0)
                        {
                            while(true){ // read multiple CUIs
                                line = br.readLine();
                                if(line.trim().length() == 0)//it's done for one phrase
                                    break;
                                if(line.startsWith("Processing"))//it's done for one sentence
                                {
                                    sentenceReadFlag = true;
                                    break;
                                }
                                if(line.contains(":")) //valid lines containing a CUI
                                {
                                    st = new StringTokenizer(line);
                                    token = st.nextToken();
                                    if (Integer.parseInt(token)<=1000 && Integer.parseInt(token)>=700)
                                    {
                                        token = st.nextToken();
                                        if(token.startsWith("C")){
                                            StringTokenizer st1 = new StringTokenizer(token,":");
                                            cui = st1.nextToken();
                                            out.print(cui.replace('C', ' ')); // obtain CUI
                                        }
                                        else{break;} // exclude "E" lines
                                    }
                                }
                                else
                                continue;
                            }
                        } else { continue; }
                    }
                }   
            }
        } catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
        }       
    out.close();
    br.close();
    }  
    
}
