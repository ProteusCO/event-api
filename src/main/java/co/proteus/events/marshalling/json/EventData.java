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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.proteus.events.publication.Event;

/**
 * Jackson data-binding class.
 *
 * @param <T> the event payload type
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
@JsonAutoDetect
public class EventData<T> extends EventMetadata
{
    private final T _payload;

    /**
     * Create an instance for the specified event.
     *
     * @param event the event
     */
    public EventData(final Event<T> event)
    {
        super(event.getEventType());
        _payload = event.getPayload();
    }

    /**
     * Create an instance for the specified type and payload
     *
     * @param eventType the event type
     * @param payload the payload
     */
    @JsonCreator
    public EventData(@JsonProperty("eventType") final String eventType, @JsonProperty("eventType") final T payload)
    {
        super(eventType);
        _payload = payload;
    }

    /**
     * Get the event payload.
     *
     * @return the event payload
     */
    public T getPayload()
    {
        return _payload;
    }
}
