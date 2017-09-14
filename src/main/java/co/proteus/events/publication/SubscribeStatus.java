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
public enum SubscribeStatus
{
    /** The IoT client successfully subscribed to the topic */
    SUBSCRIBED,
    /** All subscribers unsubscribed before the IoT client ever successfully subscribed to the topic */
    CANCELED
}
