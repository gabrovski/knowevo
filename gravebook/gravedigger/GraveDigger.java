/*
  @author = Sasho Gabrovski
  @editor = emacs -nw
*/


package gravedigger;

import java.io.FileWriter;
import java.io.BufferedWriter;

public class GraveDigger {
    
    public static void main(String[] args) {
        //XMLParser.DEBUG = true;
        //XMLParser.DEBUG_LIMIT = 1000;
        filterXml("people_articles_unfiltered.txt", 
                  "people_titles.ser", 
                  "people_articles_filtered.txt");
    }

    public static void parseXml(String art_out, String ser_out, String xml_in) {
        try {
            BufferedWriter bw = 
                new BufferedWriter(new FileWriter(art_out));

            WArticle.setOut(bw);
            XMLParser.parse(xml_in);
            bw.close();

            WArticle.serializePeopleTitles(ser_out);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void filterXml(String art_in, String ser_in, String art_out) {
        WArticle.loadPeopleTitles(ser_in);
        PeopleFilter.filterPeople(art_in, art_out);
    }
}