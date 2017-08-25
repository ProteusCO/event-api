/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

package co.proteus.events.throttling;

import co.proteus.events.publication.Event;
import co.proteus.events.publication.PublisherService;

/**
 * A filter used by {@link PublisherService} to decide whether it will actually send an event.
 *
 * @param <T> the event payload type
 *
 * @author Justin Piper (jpiper@proteus.co)
 *
 * @see PublisherService#registerThrottler
 */
@FunctionalInterface
public interface EventThrottler<T>
{
    /** A throttler that does not drop any events */
    EventThrottler<?> INCLUDE_ALL = it -> true;

    /**
     * Information about an event being sent.
     *
     * @param <T>
     */
    final class Parameters<T>
    {
        private final Event<T> _event;

        /**
         * Create an instance of {@code Parameters}
         *
         * @param event the event
         */
        public Parameters(final Event<T> event)
        {
            _event = event;
        }

        /**
         * Get the event.
         *
         * @return the event
         */
        public Event<T> getEvent()
        {
            return _event;
        }
    }

    /**
     * Check if an event should be sent.
     *
     * @param params information about the event
     *
     * @return true to send the event, false to drop it
     */
    boolean isIncluded(final Parameters<T> params);
}
