package com.github.mati1979.play.hysterix;

public class HysterixSettings {

    private boolean fallbackEnabled = true;
    private boolean requestCacheEnabled = true;

    private boolean logRequestStatistics = false;
    private int logRequestStatisticsTimeoutMs = 5000; //t seconds

    private boolean logGlobalStatistics = true;

    public boolean isLogGlobalStatistics() {
        return logGlobalStatistics;
    }

    public boolean isFallbackEnabled() {
        return fallbackEnabled;
    }

    public boolean isRequestCacheEnabled() {
        return requestCacheEnabled;
    }

    public boolean isLogRequestStatistics() {
        return logRequestStatistics;
    }

    public int getLogRequestStatisticsTimeoutMs() {
        return logRequestStatisticsTimeoutMs;
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

        public Builder withLogGlobalStatistics(final boolean logGlobalStatistics) {
            hysterixSettings.logGlobalStatistics = logGlobalStatistics;
            return this;
        }

        public Builder withLogRequestStatistics(final boolean logRequestStatistics) {
            hysterixSettings.logRequestStatistics = logRequestStatistics;
            return this;
        }

        public Builder withLogRequestStatisticsTimeoutMs(final int logRequestStatisticsTimeoutMs) {
            hysterixSettings.logRequestStatisticsTimeoutMs = logRequestStatisticsTimeoutMs;
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
