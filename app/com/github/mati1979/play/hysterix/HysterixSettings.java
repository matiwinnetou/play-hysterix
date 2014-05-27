package com.github.mati1979.play.hysterix;

public class HysterixSettings {

    private boolean fallbackEnabled = true;
    private boolean requestCacheEnabled = true;

    public boolean isFallbackEnabled() {
        return fallbackEnabled;
    }

    public void setFallbackEnabled(boolean fallbackEnabled) {
        this.fallbackEnabled = fallbackEnabled;
    }

    public boolean isRequestCacheEnabled() {
        return requestCacheEnabled;
    }

    public void setRequestCacheEnabled(boolean requestCacheEnabled) {
        this.requestCacheEnabled = requestCacheEnabled;
    }

}
