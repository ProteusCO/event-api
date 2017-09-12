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
import co.proteus.events.publication.SubscriptionService;

/**
 * Used by {@link SubscriptionService} to extract the payload from an {@link AWSIotMessage IoT message}.
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
@FunctionalInterface
public interface EventUnmarshaller
{
    /**
     * Create an {@code Event} from an {@code AWSIotMessage}
     *
     * @param message the message
     * @param <T> the type of the event payload
     *
     * @return the event
     *
     * @throws UnmarshalException thrown if there is a problem creating the event
     */
    <T> Event<T> unmarshall(AWSIotMessage message) throws UnmarshalException;
}
