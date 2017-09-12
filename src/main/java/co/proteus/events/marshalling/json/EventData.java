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

package co.proteus.events.marshalling.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import co.proteus.events.publication.Event;

/**
 * Jackson data-binding class.
 *
 * @param <T> the event payload type
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
public class EventData<T>
{
    private final String _eventType;
    private final T _payload;

    /**
     * Create an instance for the specified event.
     *
     * @param event the event
     */
    public EventData(final Event<T> event)
    {
        this(event.getEventType(), event.getPayload());
    }

    /**
     * Create an instance for the specified type and payload
     *
     * @param eventType the event type
     * @param payload the payload
     */
    @JsonCreator
    public EventData(@JsonProperty("eventType") final String eventType, @JsonProperty("payload") final T payload)
    {
        _eventType = eventType;
        _payload = payload;
    }

    /**
     * Get the event type
     *
     * @return the event type
     */
    public String getEventType()
    {
        return _eventType;
    }

    /**
     * Get the event payload.
     *
     * @return the event payload
     */
    @JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY, property = "@class")
    public T getPayload()
    {
        return _payload;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + '{' +
               "_eventType='" + _eventType + '\'' +
               ", _payload=" + _payload +
               '}';
    }
}
