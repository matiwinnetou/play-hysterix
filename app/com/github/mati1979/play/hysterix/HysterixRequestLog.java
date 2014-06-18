package com.github.mati1979.play.hysterix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.F;
import scala.concurrent.Future;

import java.util.Collection;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

public class HysterixRequestLog {

    static final int MAX_STORAGE = 1000;

    private static final Logger logger = LoggerFactory.getLogger(HysterixRequestLog.class);

    private LinkedBlockingQueue<HysterixCommand<?>> executedCommands = new LinkedBlockingQueue<>(MAX_STORAGE);

    private LinkedBlockingQueue<scala.concurrent.Promise<Collection<HysterixCommand<?>>>> promises = new LinkedBlockingQueue<>();

    private final HysterixContext hysterixContext;

    public HysterixRequestLog(final HysterixContext hysterixContext) {
        this.hysterixContext = hysterixContext;
        if (hysterixContext.getHysterixSettings().isLogRequestStatistics()) {
            scheduleTimerTask();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        logger.debug("HysterixRequestLog...garbage collecting...");
    }

    private void scheduleTimerTask() {
        final Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                notifyPromises();
            }

        }, hysterixContext.getHysterixSettings().getLogRequestStatisticsTimeoutMs());
    }

    public void addExecutedCommand(final HysterixCommand<?> command) {
        if (!executedCommands.offer(command)) {
            logger.debug("commands.size:" + executedCommands.size());
            logger.warn("RequestLog ignoring command after reaching limit of " + MAX_STORAGE);
        }
    }

    private void notifyPromises() {
        logger.debug("Notifying interested parties, partiesCount:" + promises.size());
        promises.stream().forEach(p -> p.success(getExecutedCommands()));
    }

    public void markWebRequestEnd() {
        logger.debug("WebRequest ends.");
        notifyPromises();
    }

    public Collection<HysterixCommand<?>> getExecutedCommands() {
        return Collections.unmodifiableCollection(executedCommands);
    }

    public F.Promise<Collection<HysterixCommand<?>>> executedCommands() {
        if (!hysterixContext.getHysterixSettings().isLogRequestStatistics()) {
            throw new RuntimeException("Cannot inspect log, you have to enable request log inspect via hystrix settings");
        }
        scala.concurrent.Promise<Collection<HysterixCommand<?>>> promise =
                scala.concurrent.Promise$.MODULE$.<Collection<HysterixCommand<?>>>apply();

        promises.add(promise);

        final Future<Collection<HysterixCommand<?>>> future = promise.future();

        return F.Promise.wrap(future);
    }

//    @Subscribe
//    public void onEvent(final HysterixCommandEvent hysterixCommandEvent) {
//        logger.debug("HysterixRequestLog.onEvent:" + hysterixCommandEvent);
//        addExecutedCommand(hysterixCommandEvent.getHysterixCommand());
//    }

}
