/*
 *@author Robert Johnson
* @file GenericBatch.java
*/
package assignment10;
import java.io.*;
import gov.nih.nlm.nls.skr.*;

//imports for converting html file to txt file
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.safety.Whitelist;

public class Assignment10 {
    
    public static String htmlparser(String html) {
        if(html==null)
            return html;
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        String cleanhtml = Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        String resultString = cleanhtml.replaceAll("[^\\x00-\\x7F]", "");
        return resultString;
}

    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        //parses our .html files and converts them to .txt files
        final File folder = new File("C:/data/htmlFiles");
        for (final File fileEntry : folder.listFiles()) {
            String filePath = fileEntry.getAbsolutePath();
            
            //Gets new file path name for text files
            String x = filePath.replace("htmlFiles","htmlTxtFiles");
            String newFilePath = x.replace(".html",".txt");
            
            //parses html data
            String content = new Scanner(new File(filePath)).useDelimiter("\\Z").next();
            String text = htmlparser(content);

            //writes parsed data to text files
            File newFile = new File(newFilePath);
            PrintWriter out = null;
            try {
                out = new PrintWriter(newFile);
                if(text != null) {
                    out.println(text);
                    out.flush();
                    out.close();
                }
            else{
                System.err.println("Html file is empty");
            }
            } catch (FileNotFoundException ex) {
                System.err.println("File Not Found - " + ex);
            }    
        } //end of html parser. 
        
        
        // NOTE: You MUST specify an email address because it is used for
        //       logging purposes.
        String username = "********";
        String password = "********";
        String email = "********";
        
        
        final File folder2 = new File("C:/data/htmlTxtFiles");
        for (final File fileEntry : folder2.listFiles()) {
            String filePath = fileEntry.getAbsolutePath();
            
            //Gets the results from each html file.
            GenericObject myGenericObj = new GenericObject(username, password);
            myGenericObj.setField("Email_Address", email);
            myGenericObj.setFileField("UpLoad_File", filePath);
            myGenericObj.setField("Batch_Command", "metamap13 -E -I -m -O -r 700");
            myGenericObj.setField("BatchNotes", "SKR Web API test");
            myGenericObj.setField("SilentEmail", true);
            String results = myGenericObj.handleSubmission();
                    
            //Writes results to txt files in MeteMapFiles folder
            filePath = filePath.replace("htmlTxtFiles","MetaMapFiles");
            File newFile = new File(filePath);
            PrintWriter out = null;
            try {
                out = new PrintWriter(newFile);
                if(results != null) {
                    out.println(results);
                    out.flush();
                    out.close();
                }
            else
            {
                System.err.println("MetaMap Results Were Not Returned From Server !!!");
            }
            } catch (FileNotFoundException ex) {
                System.err.println("File Not Found - " + ex);
            }  
        } 
   
        GenericObject myGenericObj = new GenericObject(username, password);

        
        myGenericObj.setField("Email_Address", email);
        myGenericObj.setFileField("UpLoad_File", "C:/data/sample.txt");
        myGenericObj.setField("Batch_Command", "metamap13 -E -I -m -O -r 700");
        myGenericObj.setField("BatchNotes", "SKR Web API test");
        myGenericObj.setField("SilentEmail", true);

  
    }
}
