package vizbook.web.facebook;

import java.io.IOException;
import java.text.Normalizer;
import java.util.*;

import org.json.*;
import com.google.code.facebookapi.*;

public class VizsterXMLWriter extends FacebookDataImportTask {
	
	private int DEBUG_LENGTH = -1;	// Fetch only upto DEBUG_LENGTH vertices, set to -1 otherwise
	
	public VizsterXMLWriter(FacebookJsonRestClient client, String name, String extension) {
		super(client, name, extension);		
	}

	//TODO: retrieve more information
	@SuppressWarnings("serial")
	private static HashMap<ProfileField, String> fields = new HashMap<ProfileField, String>() {{
	    put(ProfileField.UID, "uid");
	    put(ProfileField.NAME, "name");
	    put(ProfileField.LOCALE, "location");
	    put(ProfileField.BIRTHDAY, "age");
	    put(ProfileField.SEX, "gender");
	    put(ProfileField.RELATIONSHIP_STATUS, "status");
	    put(ProfileField.MEETING_SEX, "interested_in");
	    put(ProfileField.MEETING_FOR, "preference");
	    put(ProfileField.LOCALE, "location");
//	    put(ProfileField.HOMETOWN_LOCATION, "hometown");
//	    put(ProfileField.WORK_HISTORY, "occupation");
	    put(ProfileField.INTERESTS, "interests");
	    put(ProfileField.MUSIC, "music");
	    put(ProfileField.BOOKS, "books");
	    put(ProfileField.TV, "tvshows");
	    put(ProfileField.MOVIES, "movies");
//	    put(ProfileField.STATUS, "membersince"); 
	    put(ProfileField.ONLINE_PRESENCE, "lastlogin");
	    put(ProfileField.PROFILE_UPDATE_TIME, "lastmod");
	    put(ProfileField.ABOUT_ME, "about");
	    put(ProfileField.SIGNIFICANT_OTHER_ID, "want_to_meet");
	    put(ProfileField.PIC_SMALL, "photourl");
	}};	// TODO: Make attribute names public final static enums of Vizster

	@Override
	public void fetchData() {
		//TODO: Enforce code style
		final StringBuilder nodeBuilder = new StringBuilder(), edgeBuilder = new StringBuilder();
		
		try {
			int V = 0, E = 0;
			JSONArray friends = client.friends_get();
			ArrayList<Long> friendIds = new ArrayList<Long>();
			friendIds.add(client.users_getLoggedInUser());
			V = friends.length() + 1;	
			if(DEBUG_LENGTH > 0) V = Math.min(V, DEBUG_LENGTH);
			for(int i = 1; i < V; i++) {
				friendIds.add(friends.getLong(i-1));
			}
			
			JSONArray results = client.users_getInfo(friendIds, fields.keySet());
			
			//TODO: More informative logError(String msg, Error e)
			for(int i = 0; i < V; i++) {				
				try {					
					JSONObject u = results.getJSONObject(i);
					String name = u.getString(fields.get(ProfileField.NAME));					
									
					nodeBuilder.append("\t<node id=\"" + u.getLong(ProfileField.UID.fieldName()) + "\">");
					for(ProfileField pf : fields.keySet()) {
						String value = u.getString(pf.fieldName());
						if(value != null && value.length() > 0 && !value.equalsIgnoreCase("null"))
							nodeBuilder.append(String.format("\n\t\t<att name=\"%s\" value=\"%s\"/>", fields.get(pf), clean(value)));
					}			
					
					long friendId = friendIds.get(i);				
					
					ArrayList<Long> fakeList = new ArrayList<Long>();
					for(int j = 0; j < V; j++)
						fakeList.add(friendId);
								
					JSONArray areFriends = (JSONArray)client.friends_areFriends(fakeList, friendIds);
					int common = 0;
					for(int j = 0; j < areFriends.length(); j++) {
						try {							
							JSONObject areFriend = areFriends.getJSONObject(j);
							if(Boolean.parseBoolean(areFriend.getString("are_friends"))) {
								long id1 = areFriend.getLong("uid1"), id2 = areFriend.getLong("uid2");								
								common++;
								if(j >= i) {
									E++;
									edgeBuilder.append(String.format("\n\t<edge source=\"%d\" target=\"%d\"></edge>", id1, id2));
								}
							}
						} catch(JSONException je) {
							logError(je.getLocalizedMessage());
						}
					}
					nodeBuilder.append(String.format("\n\t\t<att name=\"%s\" value=\"%d\"/>", "nfriends", common));
					nodeBuilder.append("\n\t</node>");
					
					log(String.format("%d. Processed %s (%d common friends)", i, name, common));
					
				} catch(JSONException je) {
					logError(je.getLocalizedMessage());
				}
			}
			
			log("Finished writing graph of " + V + " friends (including self) and " + E + " edges");			
		} catch(Exception e) {
			logError(e.toString());
		} finally {
			try {
				write("<graph directed=\"0\">");
				write(nodeBuilder.toString());
				write(edgeBuilder.toString());
				write("</graph>");
			} catch (IOException e) {
				// TODO: Re-factor these nested error catching
				logError(e.toString());
			}			
		}
	}
	
	//TODO: make this a common regex
	private static String clean(String s) {
		s = s.replace("\n", " ").replaceAll("\r", " "); // Remove line breaks
		s = s.replaceAll("\\s+", " ").trim(); //Normalize white space		
		s = s.replaceAll("\\p{Punct}+", ","); //TODO: only replace < , &
		s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //Remove diacriticals
		//s = s.replaceAll("^\\p{Print}+", "?"); //Everything else becomes a question mark
		//s = StringEscape
		return s;
	}	
}