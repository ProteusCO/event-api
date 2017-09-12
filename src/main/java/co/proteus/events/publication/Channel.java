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

import java.util.Objects;

// Used to associate a topic name and event type with a value in a map.
final class Channel
{
    final String _topic;
    final String _eventType;

    Channel(final String topic, final String eventType)
    {
        _topic = topic;
        _eventType = eventType;
    }

    Channel(final Event<?> event)
    {
        this(event.getTopic(), event.getEventType());
    }

    @Override
    public boolean equals(final Object that)
    {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        final Channel channel = (Channel) that;
        return Objects.equals(_topic, channel._topic) &&
               Objects.equals(_eventType, channel._eventType);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(_topic, _eventType);
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + '{' +
               "_topic='" + _topic + '\'' +
               ", _eventType='" + _eventType + '\'' +
               '}';
    }
}
