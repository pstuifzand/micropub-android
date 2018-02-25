package eu.stuifzand.micropub.client;

public class Response {
    private boolean success;
    private String url;

    protected Response(boolean success) {
        this.success = success;
    }

    protected Response(boolean success, String url) {
        this.success = success;
        this.url = url;
    }

    public static Response failed() {
        return new Response(false);
    }

    public static Response successful(String url) {
        return new Response(true, url);
    }

    public boolean isSuccess() {
        return success;
    }
}
