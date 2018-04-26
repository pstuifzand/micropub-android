package eu.stuifzand.micropub;

import org.junit.Assert;
import org.junit.Test;

import okhttp3.HttpUrl;

public class UrlTest {
    @Test
    public void testBuildUrl() {
        String configKey = "config";
        HttpUrl micropubBackend = HttpUrl.parse("https://tiny.n9n.us/?micropub=endpoint");
        HttpUrl backend = micropubBackend.newBuilder()
                .setQueryParameter("q", configKey)
                .build();

        Assert.assertEquals(backend.toString(), "https://tiny.n9n.us/?micropub=endpoint&q=config");
    }
}
