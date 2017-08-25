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
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import co.proteus.events.filtering.MessageFilter;
import co.proteus.events.marshalling.EventUnmarshaller;
import co.proteus.events.marshalling.UnmarshalException;
import co.proteus.events.marshalling.json.EventMetadata;
import co.proteus.events.marshalling.json.JsonUnmarshaller;

import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static java.util.Objects.requireNonNull;

/**
 * @author Justin Piper (jpiper@proteus.co)
 */
public final class SubscriptionService
{
    public static class Subscription<T>
    {
        final String topic;
        final String eventType;
        final MessageFilter messageFilter;
        final Subscriber<T> subscriber;
        final Class<T> payloadType;

        @SuppressWarnings("ParameterHidesMemberVariable")
        private Subscription(final String topic, final String eventType, final MessageFilter messageFilter,
            final Subscriber<T> subscriber,
            final Class<T> payloadType)
        {
            this.topic = topic;
            this.eventType = eventType;
            this.messageFilter = messageFilter;
            this.subscriber = subscriber;
            this.payloadType = payloadType;
        }
    }

    private class EventTopic extends AWSIotTopic
    {
        @SuppressWarnings("ParameterHidesMemberVariable")
        public EventTopic(final String topic)
        {
            super(topic);
        }

        @Override
        public void onMessage(final AWSIotMessage message)
        {
            try
            {
                final EventMetadata metadata = MAPPER.readValue(message.getPayload(), EventMetadata.class);
                final Channel channel = new Channel(message.getTopic(), metadata.getEventType());
                _subscriptions.get(channel).stream()
                    .filter(sub -> _isIncluded(sub, message))
                    .forEach(sub -> _dispatch(sub, channel, message));
            }
            catch (final IOException e)
            {
                _logger.error("Error unmarshalling " + message, e);
            }
        }

        private boolean _isIncluded(final Subscription<?> subscription, final AWSIotMessage message)
        {
            return subscription.messageFilter.isIncluded(new MessageFilter.Parameters(message));
        }

        private void _dispatch(final Subscription<?> sub, final Channel channel, final AWSIotMessage message)
        {
            final EventUnmarshaller unmarshaller = _unmarshallers.getOrDefault(channel, _defaultUnmarshaller);
            try
            {
                //noinspection rawtypes
                final Event event = unmarshaller.unmarshall(message, sub.payloadType);
                //noinspection unchecked
                sub.subscriber.receive(event);
            }
            catch (final UnmarshalException | ClassCastException e)
            {
                _logger.error("Error unmarshalling " + message, e);
            }
        }
    }

    /** An unmarshaller that creates {@link Event events} using Jackson to decode the payload. */
    private static final EventUnmarshaller DEFAULT_UNMARSHALLER = new JsonUnmarshaller();

    /** Used to extract event metadata */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Logger */
    private static final Logger _logger = LogManager.getLogger(SubscriptionService.class);

    private final Map<String, EventTopic> _topics = new ConcurrentHashMap<>();
    private final Map<Channel, EventUnmarshaller> _unmarshallers = new ConcurrentHashMap<>();
    private final Multimap<Channel, Subscription<?>> _subscriptions = synchronizedListMultimap(ArrayListMultimap.create());

    private final AWSIotMqttClient _client;
    private final EventUnmarshaller _defaultUnmarshaller;

    /**
     * Create an instance of {@code SubscriptionService} that uses the {@link #DEFAULT_UNMARSHALLER default unmarshaller} to
     * decode payloads if there is not one registered.
     *
     * @param client the IoT client
     */
    public SubscriptionService(final AWSIotMqttClient client)
    {
        this(client, DEFAULT_UNMARSHALLER);
    }

    /**
     * Create an instance of {@code SubscriptionService}.
     *
     * @param client the IoT client
     * @param defaultUnmarshaller the unmarshaller to use to decode event payloads if there is not one registered
     */
    public SubscriptionService(final AWSIotMqttClient client, final EventUnmarshaller defaultUnmarshaller)
    {
        _client = client;
        _defaultUnmarshaller = defaultUnmarshaller;
    }

    /**
     * Subscribe to all events of the specified type sent to a topic.
     *
     * @param topic the topic
     * @param eventType the event type
     * @param payloadClass the type of the event payload
     * @param subscriber the subscriber to notify
     * @param <T> the type of the {@link Event#getPayload event payload}
     */
    public <T> void subscribe(
        final String topic, final String eventType, final Class<T> payloadClass, final Subscriber<T> subscriber)
    {
        subscribe(topic, eventType, it -> true, payloadClass, subscriber);
    }

    /**
     * Subscribe to events of the specified type sent to a topic and filter them by payload.
     *
     * @param topic the topic
     * @param eventType the event type
     * @param messageFilter the filter to use to ignore messages
     * @param payloadClass the type of the event payload
     * @param subscriber the subscriber to notify
     * @param <T> the type of the {@link Event#getPayload event payload}
     *
     * @return a future that contains the subscription
     */
    public <T> CompletableFuture<Subscription<T>> subscribe(
        final String topic, final String eventType, final MessageFilter messageFilter,
        final Class<T> payloadClass, final Subscriber<T> subscriber)
    {
        final CompletableFuture<Subscription<T>> result = new CompletableFuture<>();

        final Channel channel = new Channel(topic, eventType);
        final Subscription<T> subscription = new Subscription<>(topic, eventType, messageFilter, subscriber, payloadClass);
        try
        {
            if (!_topics.containsKey(topic)) _createTopic(topic);

            _subscriptions.put(channel, subscription);
            result.complete(subscription);
        }
        catch (final AWSIotException e)
        {
            _logger.error("Error subscribing to " + topic, e);
            result.completeExceptionally(e);
        }

        return result;
    }

    /**
     * Remove a subscription.
     *
     * @param subscription the subscription to remove
     */
    public void unsubscribe(final Subscription<?> subscription)
    {
        final Channel channel = new Channel(subscription.topic, subscription.eventType);
        _subscriptions.remove(channel, subscription);
        if (!_subscriptions.containsKey(channel))
        {
            try
            {
                _removeTopic(subscription.topic);
            }
            catch (AWSIotException e)
            {
                _logger.error("Error unsubscribing from " + subscription.topic, e);
            }
        }
    }

    /**
     * Register an unmarshaller to create {@link Event events} from {@link AWSIotMessage IoT messages}. If there is an existing
     * unmarshaller it will be replaced. The default unmarshaller assumes the payload is JSON data.
     *
     * @param topic the topic
     * @param eventType the event type
     * @param unmarshaller the unmarshaller
     */
    public void registerUnmarshaller(final String topic, final String eventType, final EventUnmarshaller unmarshaller)
    {

    }

    private void _createTopic(final String topic) throws AWSIotException
    {
        // FIXME: this needs to be on a separate thread in the completed version
        final EventTopic eventTopic = new EventTopic(topic);
        _client.subscribe(eventTopic);
        _topics.put(topic, eventTopic);
    }

    private void _removeTopic(final String topic) throws AWSIotException
    {
        // FIXME: this needs to be on a separate thread in the completed version
        final EventTopic eventTopic = requireNonNull(_topics.remove(topic));
        _client.unsubscribe(eventTopic);
    }
}
