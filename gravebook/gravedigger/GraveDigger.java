/*
  @author = Sasho Gabrovski
  @editor = emacs -nw
*/


package gravedigger;

import java.io.FileWriter;
import java.io.BufferedWriter;

public class GraveDigger {
    
    public static void main(String[] args) {
        XMLParser.DEBUG = true;
        XMLParser.DEBUG_LIMIT = 1000;
        parse_xml("test-people_articles_unfiltered.txt", 
                  "test-people_titles.ser", 
                  "../enwiki-latest-pages-articles.xml");
    }

    public static void parse_xml(String art_out, String ser_out, String xml_in) {
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

}