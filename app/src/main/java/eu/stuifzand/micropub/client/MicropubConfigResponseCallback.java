package eu.stuifzand.micropub.client;

import com.google.gson.JsonElement;

public interface MicropubConfigResponseCallback {
    void handleResponse(JsonElement object);
}
