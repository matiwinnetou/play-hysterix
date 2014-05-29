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

    private final HysterixSettings hysterixSettings;

    public HysterixRequestLog(final HysterixSettings hysterixSettings) {
        this.hysterixSettings = hysterixSettings;
        if (hysterixSettings.isRequestLogInspect()) {
            scheduleTimerTask();
        }
    }

    private void scheduleTimerTask() {
        final Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                promises.stream().forEach(p -> p.success(getExecutedCommands()));
            }

        }, hysterixSettings.getRequestLogInspectTimeoutInMs());
    }

    /* package */void addExecutedCommand(final HysterixCommand<?> command) {
        if (!executedCommands.offer(command)) {
            logger.warn("RequestLog ignoring command after reaching limit of " + MAX_STORAGE);
        }
    }

    public Collection<HysterixCommand<?>> getExecutedCommands() {
        return Collections.unmodifiableCollection(executedCommands);
    }

    public F.Promise<Collection<HysterixCommand<?>>> executedCommands() {
        if (!hysterixSettings.isRequestLogInspect()) {
            throw new RuntimeException("cannot inspect log, you have to enable request log inspect via hystrix settings");
        }
        scala.concurrent.Promise<Collection<HysterixCommand<?>>> promise =
                scala.concurrent.Promise$.MODULE$.<Collection<HysterixCommand<?>>>apply();

        promises.add(promise);

        final Future<Collection<HysterixCommand<?>>> future = promise.future();

        return F.Promise.wrap(future);
    }

}
