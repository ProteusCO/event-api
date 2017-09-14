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

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

import java.util.concurrent.CompletableFuture;

// Used by PublisherService#publish to complete the future it returns
final class PublisherServiceMessage extends AWSIotMessage
{
    private final CompletableFuture<PublishResult> _result;

    @SuppressWarnings("ParameterHidesMemberVariable")
    PublisherServiceMessage(
        final String topic, final AWSIotQos qos, final byte[] payload, final CompletableFuture<PublishResult> result)
    {
        super(topic, qos, payload);
        _result = result;
    }

    @Override
    public void onSuccess()
    {
        _result.complete(new PublishResult(PublishStatus.PUBLISHED));
    }

    @Override
    public void onFailure()
    {
        _result.complete(new PublishResult(PublishStatus.FAILED));
    }

    @Override
    public void onTimeout()
    {
        _result.complete(new PublishResult(PublishStatus.TIMED_OUT));
    }
}
