package com.github.mati1979.play.hysterix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by mati on 26/05/2014.
 */
public class HysterixRequestLog {

    static final int MAX_STORAGE = 1000;

    private static final Logger logger = LoggerFactory.getLogger(HysterixRequestLog.class);

    private LinkedBlockingQueue<HysterixCommand<?>> executedCommands = new LinkedBlockingQueue<>(MAX_STORAGE);

    /* package */void addExecutedCommand(final HysterixCommand<?> command) {
        if (!executedCommands.offer(command)) {
            logger.warn("RequestLog ignoring command after reaching limit of " + MAX_STORAGE);
        }
    }

    public Collection<HysterixCommand<?>> getExecutedCommands() {
        return Collections.unmodifiableCollection(executedCommands);
    }

    /**
     * Formats the log of executed commands into a string usable for logging purposes.
     * <p>
     * Examples:
     * <ul>
     * <li>TestCommand[SUCCESS][1ms]</li>
     * <li>TestCommand[SUCCESS][1ms], TestCommand[SUCCESS, RESPONSE_FROM_CACHE][1ms]x4</li>
     * <li>TestCommand[TIMEOUT][1ms]</li>
     * <li>TestCommand[FAILURE][1ms]</li>
     * <li>TestCommand[THREAD_POOL_REJECTED][1ms]</li>
     * <li>TestCommand[THREAD_POOL_REJECTED, FALLBACK_SUCCESS][1ms]</li>
     * <li>TestCommand[FAILURE, FALLBACK_SUCCESS][1ms], TestCommand[FAILURE, FALLBACK_SUCCESS, RESPONSE_FROM_CACHE][1ms]x4</li>
     * <li>GetData[SUCCESS][1ms], PutData[SUCCESS][1ms], GetValues[SUCCESS][1ms], GetValues[SUCCESS, RESPONSE_FROM_CACHE][1ms], TestCommand[FAILURE, FALLBACK_FAILURE][1ms], TestCommand[FAILURE,
     * FALLBACK_FAILURE, RESPONSE_FROM_CACHE][1ms]</li>
     * </ul>
     * <p>
     * If a command has a multiplier such as <code>x4</code> that means this command was executed 4 times with the same events. The time in milliseconds is the sum of the 4 executions.
     * <p>
     * For example, <code>TestCommand[SUCCESS][15ms]x4</code> represents TestCommand being executed 4 times and the sum of those 4 executions was 15ms. These 4 each executed the run() method since
     * <code>RESPONSE_FROM_CACHE</code> was not present as an event.
     *
     * @return String request log or "Unknown" if unable to instead of throwing an exception.
     */
    public String getExecutedCommandsAsString() {
        try {
            LinkedHashMap<String, Long> aggregatedCommandsExecuted = new LinkedHashMap<>();
            Map<String, Long> aggregatedCommandExecutionTime = new HashMap<>();

            for (HysterixCommand<?> command : executedCommands) {
                StringBuilder displayString = new StringBuilder();
                displayString.append(command.getCommandKey());

                final List<HysterixEventType> events = new ArrayList<>(command.getExecutionEvents());
                if (events.size() > 0) {
                    Collections.sort(events);
                    displayString.append(Arrays.toString(events.toArray()));
                } else {
                    displayString.append("[Executed]");
                }

                String display = displayString.toString();
                if (aggregatedCommandsExecuted.containsKey(display)) {
                    // increment the count
                    aggregatedCommandsExecuted.put(display, aggregatedCommandsExecuted.get(display) + 1);
                } else {
                    // add it
                    aggregatedCommandsExecuted.put(display, 1L);
                }

                long executionTime = command.getExecutionTime(TimeUnit.MILLISECONDS);
                if (executionTime < 0) {
                    // do this so we don't create negative values or subtract values
                    executionTime = 0;
                }
                if (aggregatedCommandExecutionTime.containsKey(display)) {
                    // add to the existing executionTime (sum of executionTimes for duplicate command displayNames)
                    aggregatedCommandExecutionTime.put(display, aggregatedCommandExecutionTime.get(display) + executionTime);
                } else {
                    // add it
                    aggregatedCommandExecutionTime.put(display, executionTime);
                }

            }

            StringBuilder header = new StringBuilder();
            for (String displayString : aggregatedCommandsExecuted.keySet()) {
                if (header.length() > 0) {
                    header.append(", ");
                }
                header.append(displayString);

                long totalExecutionTime = aggregatedCommandExecutionTime.get(displayString);
                header.append("[").append(totalExecutionTime).append("ms]");

                long count = aggregatedCommandsExecuted.get(displayString);
                if (count > 1) {
                    header.append("x").append(count);
                }
            }
            return header.toString();
        } catch (Exception e) {
            logger.error("Failed to create response header string.", e);
            return "Unknown";
        }
    }

}
