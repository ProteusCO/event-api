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

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;

import co.proteus.events.marshalling.EventUnmarshaller;
import co.proteus.events.marshalling.UnmarshalException;
import co.proteus.events.publication.Event;

/**
 * @author Justin Piper (jpiper@proteus.co)
 */
public class JsonUnmarshaller implements EventUnmarshaller
{
    private static final ObjectReader READER = new ObjectMapper()
        .reader();

    @Override
    public <T> Event<T> unmarshall(final AWSIotMessage message) throws UnmarshalException
    {
        try
        {
            final EventData<T> fields = READER.forType(EventData.class)
                .readValue(message.getPayload());
            return new Event<>(message.getTopic(), fields.getEventType(), fields.getPayload());
        }
        catch (final IOException | ClassCastException e)
        {
            throw new UnmarshalException("Error marshalling " + message, e);
        }
    }
}
