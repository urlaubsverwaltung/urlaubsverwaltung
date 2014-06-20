package org.synyx.urlaubsverwaltung.restapi;

import org.apache.commons.io.IOUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sun.misc.BASE64Encoder;

import java.io.IOException;


/**
 * Works only with running web application. This is the reason why this test is set to {@code Ignored}, but is not
 * removed because of development purposes.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Ignore
public class BasicAuthenticationIntegrationTest {

    private static final String API_URL = "http://localhost:8080/urlaubsverwaltung/api/persons";

    private static final String USER_NAME = "";
    private static final String PASSWORD = "";

    private DefaultHttpClient httpClient;

    @Before
    public void setUp() {

        httpClient = new DefaultHttpClient();
    }


    @Test
    public void ensureBasicAuthenticationWorks() throws IOException {

        BASE64Encoder encoder = new BASE64Encoder();
        String auth = encoder.encode((USER_NAME + ":" + PASSWORD).getBytes());

        HttpGet httpGet = new HttpGet(API_URL);
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("Authorization", "Basic " + auth);

        HttpResponse httpResponse = httpClient.execute(httpGet);

        Assert.assertEquals("Wrong status code", 200, httpResponse.getStatusLine().getStatusCode());

        String content = IOUtils.toString(httpResponse.getEntity().getContent());
        Assert.assertTrue("Wrong content", content.contains("{\"response\":{\"persons\":[{"));
    }


    @Test
    public void ensureReturnsStatusCode401ForInvalidAuthentication() throws IOException {

        BASE64Encoder encoder = new BASE64Encoder();
        String auth = encoder.encode(("foo" + ":" + "bar").getBytes());

        HttpGet httpGet = new HttpGet(API_URL);
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("Authorization", "Basic " + auth);

        HttpResponse httpResponse = httpClient.execute(httpGet);

        Assert.assertEquals("Wrong status code", 401, httpResponse.getStatusLine().getStatusCode());
    }


    @Test
    public void ensureRedirectsToLoginPageWhenNotAuthenticated() throws IOException {

        HttpGet httpGet = new HttpGet(API_URL);
        httpGet.setHeader("Content-Type", "application/json");

        HttpResponse httpResponse = httpClient.execute(httpGet);

        Assert.assertEquals("Wrong status code", 200, httpResponse.getStatusLine().getStatusCode());

        String content = IOUtils.toString(httpResponse.getEntity().getContent());
        Assert.assertTrue("Wrong content",
            content.contains("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"));
    }


    @Test
    public void ensureReturnsStatusCode500WhenRequestingHtml() throws IOException {

        BASE64Encoder encoder = new BASE64Encoder();
        String auth = encoder.encode((USER_NAME + ":" + PASSWORD).getBytes());

        HttpGet httpGet = new HttpGet(API_URL);
        httpGet.setHeader("Accept", "text/html");
        httpGet.setHeader("Authorization", "Basic " + auth);

        HttpResponse httpResponse = httpClient.execute(httpGet);

        Assert.assertEquals("Wrong status code", 500, httpResponse.getStatusLine().getStatusCode());
    }
}
