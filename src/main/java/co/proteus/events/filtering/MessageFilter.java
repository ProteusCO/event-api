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

package co.proteus.events.filtering;

import com.amazonaws.services.iot.client.AWSIotMessage;

/**
 * Filter incoming {@link AWSIotMessage messages}.
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
@FunctionalInterface
public interface MessageFilter
{
    /**
     * Information about the message received.
     */
    final class Parameters
    {
        private final AWSIotMessage _message;

        /**
         * Create an instance of {@code Parameters}
         *
         * @param message the message
         */
        public Parameters(final AWSIotMessage message)
        {
            _message = message;
        }

        /**
         * Get the message.
         * @return the message.
         */
        public AWSIotMessage getMessage()
        {
            return _message;
        }
    }

    /**
     * Check if a message is relevant.
     *
     * @param params information about the message
     *
     * @return true if the message is relevant, false to ignore the message
     */
    boolean isIncluded(final Parameters params);
}
