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
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import co.proteus.events.filtering.MessageFilter;
import co.proteus.events.marshalling.EventUnmarshaller;
import co.proteus.events.marshalling.UnmarshalException;
import co.proteus.events.marshalling.json.JsonUnmarshaller;

import static co.proteus.events.publication.SubscribeStatus.SUBSCRIBED;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Service to receive IoT messages and decode them as {@link Event events}.
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
public final class SubscriptionService
{
    // Note: this is used in a HashSet, but intentionally does not implement #hashCode
    // since #_subscriber is unlikely to have a useful implementation of it.
    public static final class Subscription<T>
    {
        final String _topic;
        final String _eventType;
        final MessageFilter _messageFilter;
        final Subscriber<T> _subscriber;

        private Subscription(
            final String topic, final String eventType, final MessageFilter messageFilter, final Subscriber<T> subscriber)
        {
            _topic = topic;
            _eventType = eventType;
            _messageFilter = messageFilter;
            _subscriber = subscriber;
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName() + '{' +
                   "topic='" + _topic + '\'' +
                   ", eventType='" + _eventType + '\'' +
                   ", subscriber=" + _subscriber +
                   '}';
        }

        private void receive(final Event<?> event)
        {
            //noinspection unchecked
            _subscriber.receive((Event<T>) event);
        }
    }

    private final class EventTopic extends AWSIotTopic
    {
        final CompletableFuture<SubscribeResult> _result;

        @SuppressWarnings("ParameterHidesMemberVariable")
        public EventTopic(final String topic, final CompletableFuture<SubscribeResult> result)
        {
            super(topic);
            _result = result;
        }

        @Override
        public void onMessage(final AWSIotMessage message)
        {
            try
            {
                // This is not locked since it doesn't really matter if a subscriber gets one last message after it unsubscribes,
                // nor if a new subscriber misses the first message.
                final Event<?> event = _unmarshaller.unmarshall(message);
                _subscriptions.get(message.getTopic(), event.getEventType()).stream()
                    .filter(sub -> _isAccepted(sub, message))
                    .forEach(sub -> sub.receive(event));
            }
            catch (final UnmarshalException|ClassCastException e)
            {
                _logger.error("Error unmarshalling " + message, e);
            }
        }

        @Override
        public void onSuccess()
        {
            _result.complete(new SubscribeResult(SUBSCRIBED));
        }

        @Override
        public void onFailure()
        {
            if (!_result.isDone())
            {
                // If subscribing to the topic fails just retry it periodically until it succeeds
                _logger.warn(
                    "Subscription to " + getTopic() + " failed: "
                    + getErrorCode() + ' ' + getErrorMessage()
                    + ". Retrying in " + RETRY_DELAY + "s.");
                _executor.schedule(this::_retrySubscribe, RETRY_DELAY, SECONDS);
            }
        }

        @Override
        public void onTimeout()
        {
            if (!_result.isDone())
            {
                // If subscribing to the topic times out just retry it periodically until it succeeds
                _logger.warn("Subscription to " + getTopic() + " timed out. Retrying in " + RETRY_DELAY + "s.");
                _executor.schedule(this::_retrySubscribe, RETRY_DELAY, SECONDS);
            }
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName() + '{' +
                   "_result=" + _result +
                   ", topic='" + topic + '\'' +
                   '}';
        }

        private boolean _isAccepted(final Subscription<?> subscription, final AWSIotMessage message)
        {
            return subscription._messageFilter.accept(new MessageFilter.Parameters(message));
        }

        private void _retrySubscribe()
        {
            _readLock.lock();
            try
            {
                // If there aren't any subscribers there's no need to continue trying to subscribe to the topic
                if (_topics.containsKey(getTopic())) _client.subscribe(this);
            }
            catch (AWSIotException e)
            {
                _logger.error("Error subscribing to " + getTopic(), e);
                _result.completeExceptionally(e);
            }
            finally
            {
                _readLock.unlock();
            }
        }
    }

    /** Time in seconds to retry failed subscriptions */
    private static final long RETRY_DELAY = 30;

    /** An unmarshaller that creates {@link Event events} using Jackson to decode the payload. */
    private static final EventUnmarshaller DEFAULT_UNMARSHALLER = new JsonUnmarshaller();

    private static final Logger _logger = LogManager.getLogger(SubscriptionService.class);

    /** The map of topics that have at least one subscriber */
    private final Map<String, EventTopic> _topics = new HashMap<>();

    /** The table of subscriptions with a row per topic name and column per event type */
    private final Table<String, String, HashSet<Subscription<?>>> _subscriptions = HashBasedTable.create();

    private final AWSIotMqttClient _client;
    private final ScheduledExecutorService _executor;
    private final EventUnmarshaller _unmarshaller;

    // Locks for synchronized updates to the _topics and _subscriptions collections
    private final Lock _readLock;
    private final Lock _writeLock;

    {
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        _readLock = lock.readLock();
        _writeLock = lock.writeLock();
    }

    /**
     * Create an instance of {@code SubscriptionService} that uses the {@link #DEFAULT_UNMARSHALLER default unmarshaller} to
     * decode payloads.
     *
     * @param client the IoT client. Make sure to set appropriate settings, especially the
     * {@link AWSIotMqttClient#setConnectionTimeout connection timeout} and
     * {@link AWSIotMqttClient#setExecutionService execution service}.
     * @param executor the {@code ScheduledExecutorService} to use to connect to IoT and send messages
     */
    public SubscriptionService(final AWSIotMqttClient client, final ScheduledExecutorService executor)
    {
        this(client, executor, DEFAULT_UNMARSHALLER);
    }

    /**
     * Create an instance of {@code SubscriptionService}.
     *
     * @param client the IoT client. Make sure to set appropriate settings, especially the
     * {@link AWSIotMqttClient#setConnectionTimeout connection timeout} and
     * {@link AWSIotMqttClient#setExecutionService execution service}.
     * @param executor the {@code ScheduledExecutorService} to use to connect to IoT and send messages
     * @param unmarshaller the unmarshaller to use to decode event payloads
     */
    public SubscriptionService(
        final AWSIotMqttClient client, final ScheduledExecutorService executor, final EventUnmarshaller unmarshaller)
    {
        _client = client;
        _executor = executor;
        _unmarshaller = unmarshaller;
    }

    /**
     * Subscribe to all events of the specified type sent to a topic.
     *
     * @param topic the topic
     * @param eventType the event type
     * @param subscriber the subscriber to notify
     * @param <T> the type of the {@link Event#getPayload event payload}
     */
    public <T> void subscribe(final String topic, final String eventType, final Subscriber<T> subscriber)
    {
        subscribe(topic, eventType, it -> true, subscriber);
    }

    /**
     * Subscribe to events of the specified type sent to a topic and filter them by payload.
     *
     * @param topic the topic
     * @param eventType the event type
     * @param messageFilter the filter to use to ignore messages
     * @param subscriber the subscriber to notify
     * @param <T> the type of the {@link Event#getPayload event payload}
     *
     * @return a future that contains the subscription
     */
    public <T> CompletableFuture<Subscription<T>> subscribe(
        final String topic, final String eventType, final MessageFilter messageFilter, final Subscriber<T> subscriber)
    {
        final CompletableFuture<Subscription<T>> result = new CompletableFuture<>();
        _executor.submit(() -> _subscribe(topic, eventType, messageFilter, subscriber, result));

        return result;
    }

    /**
     * Remove a subscription.
     *
     * @param subscription the subscription to remove
     */
    public void unsubscribe(final Subscription<?> subscription)
    {
        _writeLock.lock();
        try
        {
            if (_subscriptions.contains(subscription._topic, subscription._eventType))
            {
                final Collection<Subscription<?>> existing = _subscriptions.get(subscription._topic, subscription._eventType);
                existing.remove(subscription);
                if (existing.isEmpty()) _subscriptions.remove(subscription._topic, subscription._eventType);
                if (!_subscriptions.containsRow(subscription._topic))
                {
                    try
                    {
                        final EventTopic eventTopic = _topics.remove(subscription._topic);
                        _client.unsubscribe(eventTopic);
                        eventTopic._result.cancel(true);
                    }
                    catch (AWSIotException e)
                    {
                        _logger.error("Error unsubscribing from " + subscription._topic, e);
                    }
                }
            }
        }
        finally
        {
            _writeLock.unlock();
        }
    }

    private <T> void _subscribe(final String topic, final String eventType, final MessageFilter messageFilter,
        final Subscriber<T> subscriber, final CompletableFuture<Subscription<T>> result)
    {
        try
        {
            result.complete(_createSubscription(topic, eventType, messageFilter, subscriber));
        }
        catch (final AWSIotException e)
        {
            _logger.error("Error subscribing to " + topic, e);
            result.completeExceptionally(e);
        }
    }

    private <T> Subscription<T> _createSubscription(
        final String topic, final String eventType, final MessageFilter messageFilter, final Subscriber<T> subscriber)
        throws AWSIotException
    {
        _writeLock.lock();
        try
        {
            _ensureTopic(topic);

            if (!_subscriptions.contains(topic, eventType)) _subscriptions.put(topic, eventType, new HashSet<>());

            final Collection<Subscription<?>> existing = _subscriptions.get(topic, eventType);
            final Subscription<T> subscription = new Subscription<>(topic, eventType, messageFilter, subscriber);
            existing.add(subscription);

            return subscription;
        }
        finally
        {
            _writeLock.unlock();
        }
    }

    private void _ensureTopic(final String topic) throws AWSIotException
    {
        if (!_topics.containsKey(topic))
        {
            final CompletableFuture<SubscribeResult> result = new CompletableFuture<>();
            final EventTopic eventTopic = new EventTopic(topic, result);
            _client.connect();
            _client.subscribe(eventTopic);
            _topics.put(topic, eventTopic);
        }
    }
}
