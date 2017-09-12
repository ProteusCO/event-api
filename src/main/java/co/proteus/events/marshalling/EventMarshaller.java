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

package co.proteus.events.marshalling;

import com.amazonaws.services.iot.client.AWSIotMessage;

import co.proteus.events.publication.Event;
import co.proteus.events.publication.PublisherService;

/**
 * Used by {@link PublisherService} to create {@link AWSIotMessage IoT messages} from events.
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
@FunctionalInterface
public interface EventMarshaller
{
    /**
     * Create an {@code AWSIotMessage} for an event
     *
     * @param event the event
     * @param <T> the type of the event payload
     *
     * @return the message
     *
     * @throws MarshalException thrown if there is a problem creating the message
     */
    <T> AWSIotMessage marshall(Event<T> event) throws MarshalException;
}
