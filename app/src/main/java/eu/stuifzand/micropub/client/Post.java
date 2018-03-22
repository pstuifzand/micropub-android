package eu.stuifzand.micropub.client;

import okhttp3.HttpUrl;

public class Post {
    private String name;
    private String content;
    private HttpUrl inReplyTo;
    private String[] categories;
    private String[] syndicationUids;
    private String photo;
    private HttpUrl likeOf;
    private HttpUrl bookmarkOf;

    public Post(String content) {
        if (content.equals("")) {
            this.content=null;
        } else {
            this.content = content;
        }
        this.categories = new String[]{};
        this.syndicationUids = new String[]{};
    }

    public Post(String name, String content) {
        this(content);
        this.categories = new String[]{};
        this.name = name;
        this.syndicationUids = new String[]{};
    }

    public Post(String name, String content, String categories) {
        this(name, content);
        this.categories = categories.split("\\s+");
        if (this.categories.length == 1 && this.categories[0].length() == 0) {
            this.categories = new String[]{};
        }
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

    public boolean hasLikeOf() {
        return this.likeOf != null;
    }

    public void setLikeOf(HttpUrl likeOf) {
        this.likeOf = likeOf;
    }

    public String getLikeOf() {
        return likeOf.toString();
    }

    public boolean hasContent() {
        return content != null;
    }

    public void setBookmarkOf(HttpUrl bookmarkOf) {
        this.bookmarkOf = bookmarkOf;
    }
    public boolean hasBookmarkOf() {
        return bookmarkOf!=null;
    }
    public String getBookmarkOf() {
        return bookmarkOf.toString();
    }
}
