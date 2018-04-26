package eu.stuifzand.micropub;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import eu.stuifzand.micropub.client.MicropubConfigResponseCallback;
import eu.stuifzand.micropub.client.Syndication;

public class MicropubConfigTest {
    @Test public void testConfigParse(){
        String configJson = "{\"syndicate-to\":[{\"uid\":\"bridgy-publish_twitter\",\"name\":\"Twitter via Bridgy Publish\"},{\"uid\":\"bridgy-publish_facebook\",\"name\":\"Facebook via Bridgy Publish\"},{\"uid\":\"bridgy-publish_github\",\"name\":\"Github via Bridgy Publish\"}]}";
        JsonParser parser = new JsonParser();
        JsonElement configElement = parser.parse(configJson);

        ArrayList<Syndication> syndicates = new ArrayList<>();

        // Media endpoint
        JsonObject config = configElement.getAsJsonObject();
        JsonElement elem = config.get("media-endpoint");
        if (elem != null) {
            Assert.fail("media endpoint not available");
        }

        // Syndications.
        JsonArray arr = config.getAsJsonArray("syndicate-to");
        if (arr != null) {
            syndicates.clear();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject syn = arr.get(i).getAsJsonObject();
                syndicates.add(new Syndication(syn.get("uid").getAsString(), syn.get("name").getAsString()));
            }
        }

        Assert.assertEquals(syndicates.get(0).name.get(), "Twitter via Bridgy Publish");
        Assert.assertEquals(syndicates.get(0).uid.get(), "bridgy-publish_twitter");

        Assert.assertEquals(syndicates.get(1).name.get(), "Facebook via Bridgy Publish");
        Assert.assertEquals(syndicates.get(1).uid.get(), "bridgy-publish_facebook");

        Assert.assertEquals(syndicates.get(2).name.get(), "Github via Bridgy Publish");
        Assert.assertEquals(syndicates.get(2).uid.get(), "bridgy-publish_github");
    }
}
