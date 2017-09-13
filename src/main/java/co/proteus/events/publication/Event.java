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
 * A message to send through the {@link PublisherService}.
 *
 * @param <T> the payload type
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
public final class Event<T>
{
    private final String _topic;
    private final String _eventType;
    private final T _payload;

    /**
     * @param topic the topic to send the event to
     * @param eventType the event type
     * @param payload the data to send
     */
    public Event(final String topic, final String eventType, T payload)
    {
        _topic = topic;
        _eventType = eventType;
        _payload = payload;
    }

    /**
     * Get the topic the event will be sent to. This may be the same topic that other types of events are sent to.
     *
     * @return the topic
     */
    public String getTopic()
    {
        return _topic;
    }

    /**
     * Get the event type. This is expected to be unique for each type of event.
     *
     * @return the event type
     */
    public String getEventType()
    {
        return _eventType;
    }

    /**
     * Get the data to send to the topic.
     *
     * @return the payload
     */
    public T getPayload()
    {
        return _payload;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + '{' +
               "_topic='" + _topic + '\'' +
               ", _eventType='" + _eventType + '\'' +
               ", _payload=" + _payload +
               '}';
    }
}
