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
import com.amazonaws.services.iot.client.AWSIotQos;

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
    private final EventMarshaller _defaultMarshaller;
    private final Map<Channel,EventThrottler<?>> _throttlers = new ConcurrentHashMap<>();
    private final Map<Channel, EventMarshaller> _marshallers = new ConcurrentHashMap<>();

    /**
     * Create an instance of {@code PublisherService} that uses the {@link #DEFAULT_MARSHALLER default marshaller} to encode event
     * payloads if there is not one registered.
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
     * @param defaultMarshaller the marshaller to use to encode event payloads if there is not one registered
     */
    public PublisherService(final AWSIotMqttClient client, final EventMarshaller defaultMarshaller)
    {
        _client = client;
        _defaultMarshaller = defaultMarshaller;
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
     * Register a marshaller to create {@link AWSIotMessage IoT messages} from {@link Event events}. If there is an existing
     * throttler it will be replaced. The default marshaller creates a message that uses {@link AWSIotQos#QOS0 at most once}
     * delivery and encodes the payload as JSON data using Jackson.
     *
     * @param topic the topic
     * @param eventType the event type
     * @param marshaller the marshaller
     */
    public void registerMarshaller(final String topic, final String eventType, final EventMarshaller marshaller)
    {
        _marshallers.put(new Channel(topic, eventType), marshaller);
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

        final boolean included = throttler.isIncluded(parameters);
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
        final EventMarshaller marshaller = _marshallers.getOrDefault(new Channel(event), _defaultMarshaller);
        return marshaller.marshall(event);
    }
}
