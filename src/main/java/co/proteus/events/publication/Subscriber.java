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

package co.proteus.events.publication;

/**
 * A consumer of events that have been received.
 *
 * @param <T> the {@link Event#getPayload payload} type.
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
@FunctionalInterface
public interface Subscriber<T>
{
    /**
     * Receive an event.
     *
     * @param event the event
     */
    void receive(final Event<T> event);
}
