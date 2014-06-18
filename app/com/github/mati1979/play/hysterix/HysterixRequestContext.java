package com.github.mati1979.play.hysterix;

public class HysterixRequestContext {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixRequestContext.class);

    private final HysterixContext hysterixContext;
    private final HysterixRequestCacheHolder hysterixRequestCacheHolder;
    private final HysterixRequestLog hysterixRequestLog;

    public HysterixRequestContext(final HysterixContext hysterixContext,
                                  final HysterixRequestCacheHolder hysterixRequestCacheHolder,
                                  final HysterixRequestLog hysterixRequestLog) {
        this.hysterixContext = hysterixContext;
        this.hysterixRequestCacheHolder = hysterixRequestCacheHolder;
        this.hysterixRequestLog = hysterixRequestLog;
    }

    @Override
    protected void finalize() throws Throwable {
        logger.debug("HysterixRequestContext...garbage collecting...");
    }

    public HysterixRequestCacheHolder getHysterixRequestCacheHolder() {
        return hysterixRequestCacheHolder;
    }

    public HysterixRequestLog getHysterixRequestLog() {
        return hysterixRequestLog;
    }

    public HysterixContext getHysterixContext() {
        return hysterixContext;
    }

    public static HysterixRequestContext create(final HysterixContext hysterixContext) {
        logger.debug("Creating new HysterixContext.");

        final HysterixRequestLog hysterixRequestLog = new HysterixRequestLog(hysterixContext);
        final HysterixRequestCacheHolder requestCacheHolder = new HysterixRequestCacheHolder();

        return new HysterixRequestContext(hysterixContext, requestCacheHolder, hysterixRequestLog);
    }

}
