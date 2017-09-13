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
import com.amazonaws.services.iot.client.AWSIotQos;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import co.proteus.events.marshalling.EventMarshaller;
import co.proteus.events.marshalling.MarshalException;
import co.proteus.events.publication.Event;

/**
 * Marshaller that uses Jackson to convert to encode the {@link Event#getPayload payload} as JSON data.
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
public class JsonMarshaller implements EventMarshaller
{
    private static final ObjectWriter WRITER = new ObjectMapper()
        .writer();

    private final AWSIotQos _qos;

    /**
     * Create a {@code JsonMarshaller} that creates messages with the specified QoS level
     *
     * @param qos the QoS level
     */
    public JsonMarshaller(final AWSIotQos qos)
    {
        _qos = qos;
    }

    @Override
    public <T> AWSIotMessage marshall(final Event<T> event) throws MarshalException
    {
        try
        {
            final EventData<T> fields = new EventData<>(event);
            return new AWSIotMessage(event.getTopic(), _qos, WRITER.writeValueAsBytes(fields));
        }
        catch (JsonProcessingException e)
        {
            throw new MarshalException("Error marshalling " + event, e);
        }
    }
}
