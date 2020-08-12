package eu.stuifzand.micropub;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.stuifzand.micropub.client.Post;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class PostViewModelTest {
    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])(((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*))",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    @Test
    public void parseUrl() {
        String text = "HELLO WORLD: https://medium.com/@abangkis/architecture-components-livedata-and-fusedlocationprovider-de46580d0481 and text after";
        String url = "https://medium.com/@abangkis/architecture-components-livedata-and-fusedlocationprovider-de46580d0481";

        Matcher matcher = urlPattern.matcher(text);
        assertTrue(matcher.find());
        String group = matcher.group(1);
        assertEquals(url, group);
    }

    /**
     * The default post status is not set.
     */
    @Test
    public void postStatusNotSet() {
        PostViewModel postModel = new PostViewModel();
        Post post = postModel.getPost();
        assertFalse(post.hasPostStatus());
    }

    /**
     * When the post status is set we should get with getPost.
     */
    @Test
    public void postStatusSetToDraft() {
        PostViewModel postModel = new PostViewModel();
        postModel.postStatus.set("draft");
        Post post = postModel.getPost();
        assertEquals("draft", post.getPostStatus());
    }
}
