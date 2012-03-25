/*
  @author = Sasho Gabrovski
  @editor = emacs -nw
*/


package gravedigger;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class XMLParser {

    public static boolean DEBUG = false;
    public static int DEBUG_LIMIT = 100;

    private static final Pattern TITLE_PAT = Pattern.compile("<title>(.+?)</title>");
    private static final Pattern IMAGE_PAT = Pattern.compile("^\\| *image.+?= (.+)");
    private static final Pattern ID_PAT    = Pattern.compile("<id>(.+?)</id>");
    private static final Pattern LINK_PAT  = Pattern.compile("\\[+([^:]+?)\\]+"); 
    private static final Pattern BIRTH_PAT = Pattern.compile("^\\| *birth_date.+?= \\{\\{.*?\\|(\\d+).*?\\}\\}");
    private static final Pattern DEATH_PAT = Pattern.compile("^\\| *death_date.+?= \\{\\{.*?\\|(\\d+).*?\\}\\}");
    private static final Pattern CATEG_PAT = Pattern.compile("^\\[\\[Category:(.+?)\\]");

    private static final Pattern PTITLE_PAT  = Pattern.compile("title=\"(.+?)\"");
    private static final Pattern PIMAGE_PAT  = Pattern.compile("image=\"(.+?)\"");
    private static final Pattern PID_PAT     = Pattern.compile("id=\"(.+?)\"");
    private static final Pattern PLINK_PAT   = Pattern.compile("links=\"(.+?)\"");
    private static final Pattern PBIRTH_PAT  = Pattern.compile("birth=\"(.+?)\"");
    private static final Pattern PDEATH_PAT  = Pattern.compile("death=\"(.+?)\"");
    private static final Pattern PCATEG_PAT  = Pattern.compile("categories=\"(.+?)\"");

   
    public static void parse(String path) {
        WArticle wa;
        BufferedReader br;
        String line;

        String curr;
        Matcher m;

        long people_count = 0;
        long article_count = 0;

        try {
            br = new BufferedReader(new FileReader(path));
            
            wa = new WArticle();

            //process line by line
            line = br.readLine();
            while (line != null) {
                
                //save result if valid and start over
                m = TITLE_PAT.matcher(line);
                if (m.find()) {
                    article_count++;

                    if (wa.isValid()) {
                        people_count++;

                        /* for debugging */
                        if (DEBUG) {
                            if (people_count < DEBUG_LIMIT && wa.isValid()) {
                                System.out.print(wa.getTitle()+" "+wa.getId()+" "+
                                                 wa.getImage()+" "+wa.getBirth()+" "+
                                                 wa.getDeath()+" ");
                            
                                Iterator<String> it = wa.getLinks().iterator();
                                while (it.hasNext()) 
                                    System.out.print(it.next()+" ");
                            
                                System.out.println(" ");
                                for (String s: wa.getCategories())
                                    System.out.print(s+" ");
                                System.out.println("\n");
                            }
                            else if (people_count == DEBUG_LIMIT)
                                break;
                        }
                        
                        
                        wa.writeOut(false);
                        wa = new WArticle();
                        
                        if (article_count % 1000 == 0)
                            System.out.println(article_count+" "+people_count);
                       
                    }

                    curr = m.group(1);
                    wa.setTitle(curr);
                }

                m = IMAGE_PAT.matcher(line);
                if (m.find()) {
                    curr = m.group(1);
                    wa.setImage(curr);
                }
                
                m = ID_PAT.matcher(line);
                if (wa.getId() == WArticle.NO_ID && m.find()) {
                    curr = m.group(1);
                    wa.setId(Long.parseLong(curr));
                }

                m = BIRTH_PAT.matcher(line);
                if (m.find()) {
                    curr = m.group(1);
                    wa.setBirth(Integer.parseInt(curr));
                }

                m = DEATH_PAT.matcher(line);
                if (m.find()) {
                    curr = m.group(1);
                    wa.setDeath(Integer.parseInt(curr));
                }

                m = LINK_PAT.matcher(line);
                while (m.find()) {
                    try                 { curr = m.group(1).split("\\|")[0]; }
                    catch (Exception e) { continue; }

                    wa.getLinks().add(curr);
                }

                //check for person article
                m = CATEG_PAT.matcher(line);
                while (m.find()) {
                    try                 { curr = m.group(1).split("\\|")[0]; }
                    catch (Exception e) { continue; }

                    if ( !wa.isPerson() && 
                         (curr.contains("births") || curr.contains("deaths")))
                        wa.setPerson(true);

                    wa.getCategories().add(curr);
                }

                line = br.readLine();
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static WArticle parsePerson(String line) {
        WArticle wa = new WArticle();
        Matcher m;
        String curr;

        wa.setPerson(true);

        m = PTITLE_PAT.matcher(line);
        if (m.find()) {
            curr = m.group(1);
            wa.setTitle(curr);
        }

        m = PIMAGE_PAT.matcher(line);
        if (m.find()) {
            curr = m.group(1);
            wa.setImage(curr);
        }

        m = PID_PAT.matcher(line);
        if (m.find()) {
            curr = m.group(1);
            wa.setId(Long.parseLong(curr));
        }

        m = PBIRTH_PAT.matcher(line);
        if (m.find()) {
            curr = m.group(1);
            wa.setBirth(Integer.parseInt(curr));
        }

        m = PDEATH_PAT.matcher(line);
        if (m.find()) {
            curr = m.group(1);
            wa.setDeath(Integer.parseInt(curr));
        }

        m = PLINK_PAT.matcher(line);
        if (m.find()) {
            curr = m.group(1);
            for (String s: curr.split("\\|")) 
                if (!s.equals(""))
                    if (WArticle.isPerson(s))
                        wa.getPeopleLinks().add(s);
                    else
                        wa.getOtherLinks().add(s);
        }
        
        m = PCATEG_PAT.matcher(line);
        if (m.find()) {
            curr = m.group(1);
            for (String s: curr.split("\\|"))
                wa.getCategories().add(s);
        }

        return wa;
    }
}