/*
  @author = Sasho Gabrovski
  @editor = emacs -nw
*/

package gravedigger;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;


public class WArticle {

    public  static final long NO_ID = -1;
    public  static final int NO_YEAR = -1;
    private static final Set<String> people_titles = new HashSet<String>();

    private static BufferedWriter out;

    private String title;
    private String image;
    private long id;
    private boolean person;

    private int birth;
    private int death;

    private Set<String> links;
    private List<String> people_links;
    private List<String> other_links;
    private List<String> categories;

    public WArticle() { 
        birth = death = NO_YEAR;
        id = NO_ID;

        links = new HashSet<String>();
        people_links  = new ArrayList<String>();
        other_links  = new ArrayList<String>();
        categories   = new ArrayList<String>();
    }
    
    public boolean isValid() {
        return getTitle() != null && isPerson();
    }

    public static void setOut(BufferedWriter bfw) {
        out = bfw;
    }
    
    //check if valid first from caller
    public void writeOut(boolean filtered) {        
        try {
            out.write("<title=\""+getTitle()+"\" id=\""+getId()+
                      "\" image=\""+getImage()+"\" birth=\""+getBirth()+
                      "\" death=\""+getDeath()+"\" categories=\"");

            for (String s: getCategories())
                out.write(s+"|");
            
            if (filtered) {
                out.write("\" people_links=\"");

                for (String s: getPeopleLinks())
                    out.write(s+"|");
                out.write("\" other_links=\"");
                
                for (String s: getOtherLinks())
                    out.write(s+"|");
                out.write("\">\n");
            }

            else {
                out.write("\" links=\"");
                Iterator<String> it = getLinks().iterator();
                while (it.hasNext())
                    out.write(it.next()+"|");

                out.write("\">\n");
                people_titles.add(getTitle());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void serializePeopleTitles(String path) {
        try {
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(path));
            out.writeObject(people_titles);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Getters and Setters */

    public void setTitle(String s)              { title = s;           } 
    public void setImage(String s)              { image = s;           }
    public void setId(long l)                   { id = l;              }
    public void setBirth(int i)                 { birth = i;           }
    public void setDeath(int i)                 { death = i;           }
    public void setLinks(Set<String> s)         { links = s;           }
    public void setPeopleLinks(List<String> l)  { people_links = l;    }
    public void setOtherLinks(List<String> l)   { other_links = l;     }
    public void setCategories(List<String> l)   { categories = l;      }
    public void setPerson(boolean b)            { person = b;          }


    public String getTitle()                    { return title;        }
    public String getImage()                    { return image;        }
    public long getId()                         { return id;           }
    public int getBirth()                       { return birth;        }
    public int getDeath()                       { return death;        }
    public Set<String> getLinks()               { return links;        }
    public List<String> getPeopleLinks()        { return people_links; }
    public List<String> getOtherLinks()         { return other_links;  }
    public List<String> getCategories()         { return categories;   }
    public boolean isPerson()                   { return person;       }
}