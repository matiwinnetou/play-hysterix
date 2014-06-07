package com.github.mati1979.play.hysterix.event;

import com.github.mati1979.play.hysterix.HysterixCommand;

/**
 * Created by mati on 06/06/2014.
 */
public class HysterixCommandEvent {

    private HysterixCommand command;
    private long currentTime = System.currentTimeMillis();

    public HysterixCommandEvent(final HysterixCommand command) {
        this.command = command;
    }

    public HysterixCommand getHysterixCommand() {
        return command;
    }

    public long getCurrentTime() {
        return currentTime;
    }

}
