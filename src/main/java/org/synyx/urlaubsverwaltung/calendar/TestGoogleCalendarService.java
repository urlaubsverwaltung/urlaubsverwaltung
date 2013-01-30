
package org.synyx.urlaubsverwaltung.calendar;

import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAuthorizationRequestUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Test implementation for google calendar service.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class TestGoogleCalendarService {
    
    private final String CLIENT_ID = "315787839390-bd33ftir4vacke605mcb3kvreoq17uf4.apps.googleusercontent.com";
    private final String CLIENT_SECRET = "-3c-FYcHHZysRgAjG3Y93PxL";
    private final String REDIRECT_URL = "urn:ietf:wg:oauth:2.0:oob";
    private final String SCOPE = "https://www.googleapis.com/auth/calendar";
    
    private final String CALENDAR_ID = "rbsvabg6l0h2b7t5t44462vouo%40group.calendar.google.com";
    private final String API_KEY = "AIzaSyASpM0lqp1oje-S4jT45loAC2rebeSbWyA";
    
    
    private AccessTokenResponse response;

    public void setUp() throws IOException {

        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();

        String authorizationUrl = new GoogleAuthorizationRequestUrl(CLIENT_ID, REDIRECT_URL, SCOPE).build();

        System.out.println("Go to the following link in your browser:");
        System.out.println(authorizationUrl);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("What is the authorization code?");
        String code = in.readLine();

        response = new GoogleAuthorizationCodeGrant(httpTransport, jsonFactory,
                CLIENT_ID, CLIENT_SECRET, code, REDIRECT_URL).execute();

    }
    
    private void getNewAccessToken() throws IOException {
        
        String url = "https://accounts.google.com/o/oauth2/token" + 
                "&client_id=" + CLIENT_ID + 
                "&client_secret=" + CLIENT_SECRET + 
                "&refresh_token=" + response.refreshToken + "grant_type=refresh_token";
        
        //  To obtain a new access token, make an HTTPs POST to this url
        
        // TODO: please change this
        
        HttpPost post = new HttpPost(url);

        HttpClient client = new DefaultHttpClient();
        org.apache.http.HttpResponse r = client.execute(post);

        System.out.println("Status: " + r.getStatusLine().getStatusCode());
        
        
    }
    
    private boolean isTokenValid() throws IOException {
        
        // check this url to test if token is expired
        // https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=ya29.AHES6ZQwox5uFTs6tkBBJd03N8nfBsjNoXBQFmyJVLOMjqY
        
        // if token is expired you get this:
        // {"error":"invalid_token"}
        // and status code 401
        
        int statusCode = doGet("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=" + response.accessToken);
        
        if(statusCode == 401) {
            // refresh token
            return false;
        }
        
        return true;
    }
    
    public void addEvent() throws IOException {
        
        if(!isTokenValid()) {
            getNewAccessToken();
        }
        
        String json = readFile("src/main/resources/request.json");
        
        doPost(json, response.accessToken);
        
        
    }
    
    private String getJson() {
        
//        {"kind":"calendar#event","start":{"date": "2013-01-28"},"end": {"date": "2013-01-28"},"summary": "Mein Test Termin",
        
        return "";
    }
    
    public void deleteEvent() {
        // TODO
    }

    private String readFile(String file) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    }

    public void doPost(String json, String token) throws IOException {

        StringEntity stringEntity = new StringEntity(json, "UTF-8");
        stringEntity.setContentType("application/json");

        String url = "https://www.googleapis.com/calendar/v3/calendars/"
                + CALENDAR_ID + "/events?key=" + API_KEY;

        HttpPost post = new HttpPost(url);
        post.setEntity(stringEntity);
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Authorization", "OAuth " + token);

        HttpClient client = new DefaultHttpClient();
        org.apache.http.HttpResponse r = client.execute(post);

        System.out.println("Done POST for " + url);
        System.out.println("Got status: " + r.getStatusLine().getStatusCode());

    }
    
    public int doGet(String url) throws IOException {
        
        HttpGet get = new HttpGet(url);
        HttpClient client = new DefaultHttpClient();
        org.apache.http.HttpResponse r = client.execute(get);
        
        return r.getStatusLine().getStatusCode();
    }

}
