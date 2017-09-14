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
 * The result of {@link SubscriptionService#subscribe subscribing to a topic}
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
public final class SubscribeResult
{
    private final SubscribeStatus _status;

    SubscribeResult(final SubscribeStatus status)
    {
        _status = status;
    }

    /**
     * Get the subscribe status
     *
     * @return the status
     */
    public SubscribeStatus getStatus()
    {
        return _status;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + '{' +
               "_status=" + _status +
               '}';
    }
}
