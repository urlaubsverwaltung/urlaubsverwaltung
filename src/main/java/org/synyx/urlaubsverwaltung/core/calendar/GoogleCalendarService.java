package org.synyx.urlaubsverwaltung.core.calendar;

import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAuthorizationRequestUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.joda.time.DateTime;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * TODO: This class is in development! Test implementation for google calendar service.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class GoogleCalendarService {

    // TODO: set this properties in config.properties file

    private final String CLIENT_ID = "";
    private final String CLIENT_SECRET = "";
    private final String REDIRECT_URL = "";
    private final String SCOPE = "https://www.googleapis.com/auth/calendar";
    private final String CALENDAR_ID = "";
    private final String API_KEY = "";
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

        response = new GoogleAuthorizationCodeGrant(httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, code,
                REDIRECT_URL).execute();
    }


    private void getNewAccessToken() throws IOException {

        String url = "https://accounts.google.com/o/oauth2/token"
            + "&client_id=" + CLIENT_ID
            + "&client_secret=" + CLIENT_SECRET
            + "&refresh_token=" + response.refreshToken + "grant_type=refresh_token";

        // To obtain a new access token, make an HTTPs POST to this url

        // TODO: please change this

        HttpPost post = new HttpPost(url);

        HttpClient client = new DefaultHttpClient();
        org.apache.http.HttpResponse r = client.execute(post);

        System.out.println("Result of trying to obtain a new acces token:");
        System.out.println("Status: " + r.getStatusLine().getStatusCode());
    }


    private boolean isTokenValid() throws IOException {

        // check this url to test if token is expired
        // https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=ya29.AHES6ZQwox5uFTs6tkBBJd03N8nfBsjNoXBQFmyJVLOMjqY

        // if token is expired you get this:
        // {"error":"invalid_token"}
        // and status code 401

        int statusCode = doGet("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=" + response.accessToken);

        if (statusCode == 401) {
            // refresh token
            return false;
        }

        return true;
    }


    public void addEvent() throws IOException {

        if (!isTokenValid()) {
            System.out.println("Token is invalid, get a new one.");
            getNewAccessToken();
        }

        String summary = "Test " + DateTime.now().getHourOfDay() + ":" + DateTime.now().getMinuteOfHour();

        String json =
            "{'kind':'calendar#event','start':{'date': '2013-03-22'},'end': {'date': '2013-03-22'},'summary': '"
            + summary + "'}";

        doPost(json, response.accessToken);
    }


    private String getJson(Application a) {

        String startDate = a.getStartDate().toString("yyyy-MM-dd");
        String endDate = a.getEndDate().toString("yyyy-MM-dd");
        String summary = a.getPerson().getFirstName() + " " + a.getPerson().getLastName() + " Urlaub";

        String json = "{'kind':'calendar#event','start':{'date': '" + startDate + "'},'end': {'date': '" + endDate
            + "'},'summary': '" + summary + "'}";

        return json;
    }


    public void deleteEvent() {

        // TODO
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
