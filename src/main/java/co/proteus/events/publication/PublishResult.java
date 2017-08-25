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
 * @author Justin Piper (jpiper@proteus.co)
 */
public final class PublishResult
{
    private final PublishStatus _status;

    PublishResult(final PublishStatus status)
    {
        _status = status;
    }

    /**
     * Get the publish status
     *
     * @return the status
     */
    public PublishStatus getStatus()
    {
        return _status;
    }
}
