package com.github.mati1979.play.hysterix.web;

import com.github.mati1979.play.hysterix.event.HysterixStatisticsEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created by mati on 06/06/2014.
 */
public class HysterixController extends Controller {

    private EventBus eventBus;

    public HysterixController(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public Result index() {
        final Chunks<String> chunks = new StringChunks() {

            public void onReady(final Chunks.Out<String> out) {
                eventBus.register(new Subscriber(out));
            }

        };

        return ok(chunks);
    }

    private class Subscriber {

        private Chunks.Out<String> out;

        private Subscriber(final Chunks.Out<String> out) {
            this.out = out;
        }

        @Subscribe
        public void onEvent(final HysterixStatisticsEvent event) {
            out.write(event.toString());
            out.write("\n");
            out.write("\n");
        }

    }

}
