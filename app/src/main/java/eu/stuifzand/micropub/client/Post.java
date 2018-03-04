package eu.stuifzand.micropub.client;

import okhttp3.HttpUrl;

public class Post {
    private String name;
    private String content;
    private HttpUrl inReplyTo;
    private String[] categories;
    private String[] syndicationUids;
    private String photo;

    public Post(String content) {
        this.content = content;
        this.categories = new String[]{};
        this.syndicationUids = new String[]{};
    }

    public Post(String name, String content) {
        this.name = name;
        this.content = content;
        this.categories = new String[]{};
        this.syndicationUids = new String[]{};
    }

    public Post(String name, String content, String categories) {
        this.name = name;
        this.content = content;
        this.categories = categories.split("\\s+");
        this.syndicationUids = new String[]{};
    }

    public Post(String name, String content, String categories, HttpUrl inReplyTo) {
        this(name, content, categories);
        this.inReplyTo = inReplyTo;
        this.syndicationUids = new String[]{};
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

    public void setSyndicationUids(String[] syndicationUids) {
        this.syndicationUids = syndicationUids;
    }

    public String[] getSyndicationUids() {
        return syndicationUids;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhoto() {
        return photo;
    }

    public boolean hasPhoto() {
        return this.photo != null;
    }
}
