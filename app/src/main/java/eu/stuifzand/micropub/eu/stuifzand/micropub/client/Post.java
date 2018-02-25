package eu.stuifzand.micropub.eu.stuifzand.micropub.client;

import okhttp3.HttpUrl;

public class Post {
    private String name;
    private String content;
    private String[] categories;
    private HttpUrl inReplyTo;

    public Post(String content) {
        this.content = content;
        this.categories = new String[]{};
    }

    public Post(String name, String content) {
        this.name = name;
        this.content = content;
        this.categories = new String[]{};
    }

    public Post(String name, String content, String categories) {
        this.name = name;
        this.content = content;
        this.categories = categories.split("\\s+");
    }

    public Post(String name, String content, String categories, HttpUrl inReplyTo) {
        this(name, content, categories);
        this.inReplyTo = inReplyTo;
    }


    public String getContent() {
        return content;
    }

    public boolean hasName() {
        return this.name != null && !name.equals("");
    }
    public String getName() {
        return this.name;
    }

    public boolean hasInReplyTo() {
        return inReplyTo != null;
    }

    public String getInReplyTo() {
        return inReplyTo.toString();
    }

    public String[] getCategories() {
        return categories;
    }
}
