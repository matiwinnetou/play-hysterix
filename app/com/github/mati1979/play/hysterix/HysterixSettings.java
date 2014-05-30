package com.github.mati1979.play.hysterix;

public class HysterixSettings {

    private boolean fallbackEnabled = true;
    private boolean requestCacheEnabled = true;

    private boolean requestLogInspect = false;
    private int requestLogInspectTimeoutInMs = 5000;

    public boolean isFallbackEnabled() {
        return fallbackEnabled;
    }

    public boolean isRequestCacheEnabled() {
        return requestCacheEnabled;
    }

    public boolean isRequestLogInspect() {
        return requestLogInspect;
    }

    public int getRequestLogInspectTimeoutInMs() {
        return requestLogInspectTimeoutInMs;
    }

    @Override
    public String toString() {
        return "HysterixSettings{" +
                "fallbackEnabled=" + fallbackEnabled +
                ", requestCacheEnabled=" + requestCacheEnabled +
                ", requestLogInspect=" + requestLogInspect +
                ", requestLogInspectTimeoutInMs=" + requestLogInspectTimeoutInMs +
                '}';
    }

    public static class Builder {

        private HysterixSettings hysterixSettings;

        private Builder() {
            hysterixSettings = new HysterixSettings();
        }

        public Builder withFallbackEnabled(final boolean fallbackEnabled) {
            hysterixSettings.fallbackEnabled = fallbackEnabled;
            return this;
        }

        public Builder withRequestCacheEnabled(final boolean requestCacheEnabled) {
            hysterixSettings.requestCacheEnabled = requestCacheEnabled;
            return this;
        }

        public Builder withRequestLogInspect(final boolean hysterixLogInspect) {
            hysterixSettings.requestLogInspect = hysterixLogInspect;
            return this;
        }

        public Builder withRequestLogInspectTimeoutInMs(final int hysterixLogInspectTimeoutInMs) {
            hysterixSettings.requestLogInspectTimeoutInMs = hysterixLogInspectTimeoutInMs;
            return this;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public HysterixSettings build() {
            return hysterixSettings;
        }

    }

}
