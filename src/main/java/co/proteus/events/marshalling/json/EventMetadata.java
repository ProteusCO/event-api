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

/**
 * Jackson data-binding class.
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
public class EventMetadata
{
    private final String _eventType;

    /**
     * Create an instance for the specified type and payload
     *
     * @param eventType the event type
     */
    @JsonCreator
    public EventMetadata(@JsonProperty("eventType") final String eventType)
    {
        _eventType = eventType;
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
}
