package eu.stuifzand.micropub;

import org.junit.Test;

import eu.stuifzand.micropub.client.Post;
import okhttp3.HttpUrl;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PostTest {
    @Test public void postWithContent() {
        Post post = new Post("hello world");
        assertEquals(post.getContent(), "hello world");
        String[] categories = post.getCategories();
        assertEquals(categories.length, 0);
    }
    @Test public void postWithNameAndContent() {
        Post post = new Post("title", "content");
        assertEquals(post.getName(), "title");
        assertEquals(post.getContent(), "content");
    }

    @Test public void postWithNameContentAndEmptyCategories() {
        Post post = new Post("title", "content", "");
        assertEquals(post.getName(), "title");
        assertEquals(post.getContent(), "content");
        String[] categories = post.getCategories();
        assertEquals(0, categories.length);
        assertFalse(post.hasInReplyTo());
        assertFalse(post.hasPhoto());
    }

    @Test public void emptyHasNoPhoto() {
        Post post = new Post("Content");
        assertFalse(post.hasPhoto());
    }

    @Test public void postWithInReplyTo() {
        Post post = new Post("title", "content", "", HttpUrl.parse("http://example.com"));
        assertTrue(post.hasInReplyTo());
        assertEquals("http://example.com/", post.getInReplyTo());
    }
}
