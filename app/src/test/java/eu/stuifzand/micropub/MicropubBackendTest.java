package eu.stuifzand.micropub;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import eu.stuifzand.micropub.client.Client;
import eu.stuifzand.micropub.client.Post;
import eu.stuifzand.micropub.client.Response;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class MicropubBackendTest {

    @Test
    public void createPost() throws Exception {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(201).addHeader("Location", "http://example.com/post/1"));
        server.start();

        HttpUrl baseUrl = server.url("/micropub");
        Client client = new Client();

        Post post = new Post("Hello world");
        client.createPost(post, "token", baseUrl);
        Robolectric.flushBackgroundThreadScheduler();

        RecordedRequest request = server.takeRequest();
        assertEquals(request.getPath(), "/micropub");
        assertEquals(request.getHeader("Authorization"), "Bearer token");
        assertEquals(request.getMethod(), "POST");

        Response value = client.getResponse().getValue();
        assertTrue(value.isSuccess());
        assertTrue(value.getUrl().endsWith("/post/1"));
    }

    @Test
    public void createPostWithMicropubError() throws Exception {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(400));
        server.start();

        HttpUrl baseUrl = server.url("/micropub");
        Client client = new Client();

        Post post = new Post("Hello world");
        client.createPost(post, "token", baseUrl);
        Robolectric.flushBackgroundThreadScheduler();

        Response value = client.getResponse().getValue();
        assertFalse(value.isSuccess());
    }

    @Test
    public void loadConfigMediaEndpoint() throws Exception {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"media-endpoint\":\"http://example.com/media\", \"syndicate-to\":[]}"));
        server.start();

        HttpUrl baseUrl = server.url("/micropub");
        Client client = new Client();
        client.loadConfig(baseUrl);
        Robolectric.flushBackgroundThreadScheduler();

        assertEquals("http://example.com/media", client.getMediaEndpoint());
    }

    @Test
    public void loadConfigSyndicateTo() throws Exception {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"media-endpoint\":\"http://example.com/media\", \"syndicate-to\":[{\"uid\":\"test\",\"name\":\"Test\"}]}"));
        server.start();

        HttpUrl baseUrl = server.url("/micropub");
        Client client = new Client();
        client.loadConfig(baseUrl);
        Robolectric.flushBackgroundThreadScheduler();

        assertEquals("Test", client.syndicates.get(0).name.get());
        assertEquals("test", client.syndicates.get(0).uid.get());
    }

    @Test
    public void loadConfigParseError() throws Exception {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{"));
        server.start();

        HttpUrl baseUrl = server.url("/micropub");
        Client client = new Client();
        client.loadConfig(baseUrl);
        Robolectric.flushBackgroundThreadScheduler();
    }
}
