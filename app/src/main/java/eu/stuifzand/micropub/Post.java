package eu.stuifzand.micropub;

import okhttp3.FormBody;

public class Post {
    private String content;

    public Post(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void applyToFormBodyBuilder(FormBody.Builder builder) {
        builder.add("content", content);
    }
}
