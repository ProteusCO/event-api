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

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import co.proteus.events.marshalling.EventMarshaller;
import co.proteus.events.marshalling.MarshalException;
import co.proteus.events.marshalling.json.JsonMarshaller;
import co.proteus.events.throttling.EventThrottler;

import static co.proteus.events.throttling.EventThrottler.INCLUDE_ALL;
import static com.amazonaws.services.iot.client.AWSIotQos.QOS0;
import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Service to encode events and send them to IoT.
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
public final class PublisherService
{

    /**
     * A marshaller that creates {@link AWSIotMessage IoT messages} that use at least once delivery and uses Jackson to encode the
     * payload as JSON data.
     */
    private static final EventMarshaller DEFAULT_MARSHALLER = new JsonMarshaller(QOS0);

    private final AWSIotMqttClient _client;
    private final EventMarshaller _marshaller;
    private final Map<Channel,EventThrottler<?>> _throttlers = new ConcurrentHashMap<>();

    /**
     * Create an instance of {@code PublisherService} that uses the {@link #DEFAULT_MARSHALLER default marshaller} to encode event
     * payloads.
     *
     * @param client the IoT client
     */
    public PublisherService(final AWSIotMqttClient client)
    {
        this(client, DEFAULT_MARSHALLER);
    }

    /**
     * Create an instance of {@code PublisherService}
     *
     * @param client the IoT client
     * @param marshaller the marshaller to use to encode event payloads
     */
    public PublisherService(final AWSIotMqttClient client, final EventMarshaller marshaller)
    {
        _client = client;
        _marshaller = marshaller;
    }

    /**
     * Register a throttler to limit the number of events sent to the specified topic for the specified event type. If there is an
     * existing throttler it will be replaced.
     *
     * @param topic the topic
     * @param eventType the event type
     * @param throttler the throttler
     */
    public void registerThrottler(final String topic, final String eventType, final EventThrottler<?> throttler)
    {
        _throttlers.put(new Channel(topic, eventType), throttler);
    }

    /**
     * Publish an event and return a future for the result.
     *
     * @param event the event
     * @param <T> the type of the event payload
     *
     * @return the future for the result.
     */
    public <T> CompletableFuture<?> publish(final Event<T> event)
    {
        @SuppressWarnings("unchecked")
        final EventThrottler<T> throttler = (EventThrottler<T>) _throttlers.getOrDefault(new Channel(event), INCLUDE_ALL);
        final EventThrottler.Parameters<T> parameters = new EventThrottler.Parameters<>(event);

        final boolean included = throttler.shouldSend(parameters);
        return included? _publish(event) : completedFuture(new PublishResult(PublishStatus.THROTTLED));
    }

    private <T> CompletableFuture<PublishResult> _publish(final Event<T> event)
    {
        // FIXME: the final implementation needs to do this on a separate thread
        final CompletableFuture<PublishResult> result = new CompletableFuture<>();
        try
        {
            // Note: this will block until the timeouts configured on the client.
            _client.connect();
            _client.publish(_marshal(event));
            result.complete(new PublishResult(PublishStatus.PUBLISHED));
            return result;
        }
        catch (final AWSIotException|MarshalException e)
        {
            result.completeExceptionally(e);
            return result;
        }
    }

    private <T> AWSIotMessage _marshal(final Event<T> event) throws MarshalException
    {
        return _marshaller.marshall(event);
    }
}
