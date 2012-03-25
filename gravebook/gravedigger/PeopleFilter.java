/*
  @author = Sasho Gabrovski
  @editor = emacs -nw
*/


package gravedigger;

import java.util.Set;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class PeopleFilter {
    
    public static void filterPeople(String people_in, String people_out) {
        try {
            WArticle wa;
            String line;
            BufferedReader br = new BufferedReader(new FileReader(people_in));
            BufferedWriter bw = new BufferedWriter(new FileWriter(people_out));
            WArticle.setOut(bw);

            line = br.readLine();
            while (line != null) {
                wa = XMLParser.parsePerson(line);
                wa.writeOut(true);
                line = br.readLine();
            }

            br.close();
            bw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
        
}