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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import co.proteus.events.marshalling.EventMarshaller;
import co.proteus.events.marshalling.MarshalException;
import co.proteus.events.marshalling.json.JsonMarshaller;
import co.proteus.events.throttling.EventThrottler;

import static co.proteus.events.throttling.EventThrottler.INCLUDE_ALL;
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
    private static final EventMarshaller DEFAULT_MARSHALLER = new JsonMarshaller();

    private static final Logger _logger = LogManager.getLogger(PublisherService.class);

    private final AWSIotMqttClient _client;
    private final ExecutorService _executor;
    private final EventMarshaller _marshaller;
    private final Map<Channel,EventThrottler<?>> _throttlers = new ConcurrentHashMap<>();

    /**
     * Create an instance of {@code PublisherService} that uses the {@link #DEFAULT_MARSHALLER default marshaller} to encode event
     * payloads.
     *
     * @param client the IoT client. Make sure to set appropriate settings, especially the
     * {@link AWSIotMqttClient#setConnectionTimeout connection timeout} and
     * {@link AWSIotMqttClient#setExecutionService execution service}.
     * @param executor the {@code ExecutorService} to use to connect to IoT and send messages
     */
    public PublisherService(final AWSIotMqttClient client, final ExecutorService executor)
    {
        this(client, executor, DEFAULT_MARSHALLER);
    }

    /**
     * Create an instance of {@code PublisherService}
     *
     * @param client the IoT client
     * @param executor the {@code ExecutorService} to use to connect to IoT and send messages
     * @param marshaller the marshaller to use to encode event payloads
     */
    public PublisherService(final AWSIotMqttClient client, final ExecutorService executor, final EventMarshaller marshaller)
    {
        _client = client;
        _executor = executor;
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
        final Channel channel = new Channel(topic, eventType);
        final EventThrottler<?> previous = _throttlers.put(channel, throttler);
        if (previous != null)
        {
            _logger.info("Throttler for " + channel + " changed from " + previous + " to " + throttler);
        }
    }

    /**
     * Publish an event and return a future for the result.
     *
     * @param event the event
     * @param publishTimeout the publish timeout in milliseconds
     * @param <T> the type of the event payload
     *
     * @return the future for the result.
     */
    public <T> CompletableFuture<?> publish(final Event<T> event, long publishTimeout)
    {
        @SuppressWarnings("unchecked")
        final EventThrottler<T> throttler = (EventThrottler<T>) _throttlers.getOrDefault(new Channel(event), INCLUDE_ALL);
        final EventThrottler.Parameters<T> parameters = new EventThrottler.Parameters<>(event);

        final boolean included = throttler.shouldSend(parameters);
        return included? _publish(event, publishTimeout) : completedFuture(new PublishResult(PublishStatus.THROTTLED));
    }

    private <T> CompletableFuture<PublishResult> _publish(final Event<T> event, long publishTimeout)
    {
        final CompletableFuture<PublishResult> result = new CompletableFuture<>();
        try
        {
            final byte[] payload = _marshaller.marshall(event);
            final AWSIotMessage message = new PublisherServiceMessage(event.getTopic(), event.getQos(), payload, result);

            _executor.submit(() -> _publish(message, publishTimeout, result));
        }
        catch (MarshalException e)
        {
            result.completeExceptionally(e);
        }
        return result;
    }

    private void _publish(final AWSIotMessage message, long publishTimeout, final CompletableFuture<PublishResult> result)
    {
        try
        {
            // Note: this will block until the timeout configured on the client.
            _client.connect();

            // This will _not_ block, because why not have two different ways of handling timeouts?
            _client.publish(message, publishTimeout);
        }
        catch (final AWSIotException e)
        {
            result.completeExceptionally(e);
        }
    }

}
