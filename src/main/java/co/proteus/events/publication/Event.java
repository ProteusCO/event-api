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

import com.amazonaws.services.iot.client.AWSIotQos;

import java.util.Objects;

import static com.amazonaws.services.iot.client.AWSIotQos.QOS0;

/**
 * A message to send through the {@link PublisherService}.
 *
 * @param <T> the payload type
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
public final class Event<T>
{
    private final String _topic;
    private final String _eventType;
    private final AWSIotQos _qos;
    private final T _payload;

    /**
     * Create an instance of {@code Event} that will be sent using {@link AWSIotQos#QOS0 at-most-once delivery}.
     *
     * @param topic the topic to send the event to
     * @param eventType the event type
     * @param payload the data to send
     */
    public Event(final String topic, final String eventType, T payload)
    {
        this(topic, eventType, QOS0, payload);
    }

    /**
     * Create an instance of {@code Event}
     *
     * @param topic the topic to send the event to
     * @param eventType the event type
     * @param qos the MQTT QoS level. Unless your application has a strategy for handling duplicate messages use
     * {@link AWSIotQos#QOS0 QOS0}, which may drop messages but will not deliver the same message more than once.
     * @param payload the data to send
     */
    public Event(final String topic, final String eventType, final AWSIotQos qos, T payload)
    {
        _topic = topic;
        _eventType = eventType;
        _qos = qos;
        _payload = payload;
    }

    /**
     * Get the topic the event will be sent to. This may be the same topic that other types of events are sent to.
     *
     * @return the topic
     */
    public String getTopic()
    {
        return _topic;
    }

    /**
     * Get the event type. This is expected to be unique for each type of event.
     *
     * @return the event type
     */
    public String getEventType()
    {
        return _eventType;
    }

    /**
     * Get the MQTT QoS level.
     *
     * @return the QoS level
     */
    public AWSIotQos getQos()
    {
        return _qos;
    }

    /**
     * Get the data to send to the topic.
     *
     * @return the payload
     */
    public T getPayload()
    {
        return _payload;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + '{' +
               "_topic='" + _topic + '\'' +
               ", _eventType='" + _eventType + '\'' +
               ", _qos=" + _qos +
               ", _payload=" + _payload +
               '}';
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Event<?> event = (Event<?>) o;
        return Objects.equals(_topic, event._topic) &&
               Objects.equals(_eventType, event._eventType) &&
               _qos == event._qos &&
               Objects.equals(_payload, event._payload);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(_topic, _eventType, _qos, _payload);
    }
}
