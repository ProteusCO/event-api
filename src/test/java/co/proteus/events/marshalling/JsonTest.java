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

package co.proteus.events.marshalling;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.collect.Sets;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import co.proteus.events.marshalling.json.JsonMarshaller;
import co.proteus.events.marshalling.json.JsonUnmarshaller;
import co.proteus.events.publication.Event;

import static co.proteus.events.TestGroups.UNIT;
import static com.amazonaws.services.iot.client.AWSIotQos.QOS0;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link JsonMarshaller} and {@link JsonUnmarshaller}, with examples of various payload types that might be useful.
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
public final class JsonTest
{
    // Simple payload class
    @SuppressWarnings("unused")
    private static final class Person
    {
        final String _firstName;
        final String _lastName;
        final Date _birthday;

        public Person(
            @JsonProperty("firstName") final String firstName,
            @JsonProperty("lastName") final String lastName,
            @JsonProperty("birthday") final Date birthday)
        {
            _firstName = firstName;
            _lastName = lastName;
            _birthday = birthday;
        }

        public String getFirstName()
        {
            return _firstName;
        }

        public String getLastName()
        {
            return _lastName;
        }

        public Date getBirthday()
        {
            return _birthday;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Person person = (Person) o;
            return Objects.equals(_firstName, person._firstName) &&
                   Objects.equals(_lastName, person._lastName) &&
                   Objects.equals(_birthday, person._birthday);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(_firstName, _lastName, _birthday);
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName() + '{' +
                   "_firstName='" + _firstName + '\'' +
                   ", _lastName='" + _lastName + '\'' +
                   '}';
        }
    }

    // Payload class hierarchy
    @JsonTypeInfo(use = Id.CLASS, include = As.WRAPPER_OBJECT)
    @JsonSubTypes({
        @Type(value = Dog.class, name = "dog"),
        @Type(value = Cat.class, name = "cat")
    })
    private static class Animal
    {
        private final String _name;

        public Animal(final String name)
        {
            _name = name;
        }

        public String getName()
        {
            return _name;
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName() + '{' +
                   "_name='" + _name + '\'' +
                   '}';
        }
    }

    @SuppressWarnings("unused")
    private static final class Dog extends Animal
    {
        enum Trick
        {
            SIT, STAY, SHAKE
        }

        private final Set<Trick> _tricks;

        @JsonCreator
        private Dog(@JsonProperty("name") final String name, @JsonProperty("tricks") final Collection<Trick> tricks)
        {
            super(name);
            _tricks = Sets.immutableEnumSet(tricks);
        }

        public Set<Trick> getTricks()
        {
            return _tricks;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Dog dog = (Dog) o;
            return Objects.equals(getName(), dog.getName()) &&
                   Objects.equals(_tricks, dog._tricks);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(getName(), _tricks);
        }
    }

    @SuppressWarnings("unused")
    private static final class Cat extends Animal
    {
        enum Trick
        {
            NAP, SNOOZE, SLEEP
        }

        private final Set<Trick> _tricks;

        @JsonCreator
        private Cat(
            @JsonProperty("name") final String name,
            @JsonProperty("tricks") final Collection<Trick> tricks)
        {
            super(name);
            _tricks = Sets.immutableEnumSet(tricks);
        }

        public Set<Trick> getTricks()
        {
            return _tricks;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Cat cat = (Cat) o;
            return Objects.equals(getName(), cat.getName()) &&
                   Objects.equals(_tricks, cat._tricks);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(getName(), _tricks);
        }
    }

    // Due to type erasure Jackson needs the payload to be a concrete class if it is a collection
    private static final class Zoo extends LinkedHashSet<Animal>
    {
        private static final long serialVersionUID = -1931882980469973129L;

        @JsonCreator
        private Zoo(final Collection<Animal> animals)
        {
            super(animals);
        }
    }

    private static final AWSIotQos QOS = QOS0;

    private static final String TOPIC = "sample/topic";
    private static final String TYPE = "sample-type";

    // An event with no payload
    private static final Event<Void> EVENT_VOID = new Event<>(TOPIC, TYPE, null);
    private static final byte[] JSON_VOID = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "null}")
        .getBytes(UTF_8);
    private static final AWSIotMessage MESSAGE_VOID = new AWSIotMessage(TOPIC, QOS, JSON_VOID);

    // An event with a String payload
    private static final Event<String> EVENT_STRING = new Event<>(TOPIC, TYPE, "hello");
    private static final byte[] JSON_STRING = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "\"hello\"}")
        .getBytes(UTF_8);
    private static final AWSIotMessage MESSAGE_STRING = new AWSIotMessage(TOPIC, QOS, JSON_STRING);

    // An event with a Date payload
    private static final Event<Date> EVENT_DATE = new Event<>(TOPIC, TYPE, new Date(0));
    private static final byte[] JSON_DATE = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "[\"java.util.Date\",0]}")
        .getBytes(UTF_8);
    private static final AWSIotMessage MESSAGE_DATE = new AWSIotMessage(TOPIC, QOS, JSON_DATE);

    // An event with a numeric payload
    private static final Event<Long> EVENT_NUM = new Event<>(TOPIC, TYPE, 32767L);
    private static final byte[] JSON_NUM = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "[\"java.lang.Long\",32767]}")
        .getBytes(UTF_8);
    private static final AWSIotMessage MESSAGE_NUM = new AWSIotMessage(TOPIC, QOS, JSON_NUM);

    // An event with a simple payload class
    private static final Event<Person> EVENT_PERSON = new Event<>(TOPIC, TYPE, new Person("John", "Doe", new Date(0)));
    private static final byte[] JSON_PERSON = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "{\"@class\":\"co.proteus.events.marshalling.JsonTest$Person\","
        + "\"firstName\":\"John\","
        + "\"lastName\":\"Doe\","
        + "\"birthday\":0}}")
        .getBytes(UTF_8);
    private static final AWSIotMessage MESSAGE_PERSON = new AWSIotMessage(TOPIC, QOS, JSON_PERSON);

    // An event with a simple collection payload
    private static final Event<Set<String>> EVENT_COL = new Event<>(TOPIC, TYPE, new LinkedHashSet<>(asList("abc", "def", "ghi")));
    private static final byte[] JSON_COL = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "[\"java.util.LinkedHashSet\",[\"abc\",\"def\",\"ghi\"]]}")
        .getBytes(UTF_8);
    private static final AWSIotMessage MESSAGE_COL = new AWSIotMessage(TOPIC, QOS, JSON_COL);

    // An event with a type hierarchy payload
    private static final Animal DOG = new Dog("Rover", singletonList(Dog.Trick.SIT));
    private static final Animal CAT = new Cat("Fluffy", singletonList(Cat.Trick.NAP));
    private static final Event<Zoo> EVENT_PETS = new Event<>(TOPIC, TYPE, new Zoo(asList(DOG, CAT)));
    private static final byte[] JSON_PET = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "[\"co.proteus.events.marshalling.JsonTest$Zoo\",["
        + "{\"co.proteus.events.marshalling.JsonTest$Dog\":{\"name\":\"Rover\",\"tricks\":[\"SIT\"]}},"
        + "{\"co.proteus.events.marshalling.JsonTest$Cat\":{\"name\":\"Fluffy\",\"tricks\":[\"NAP\"]}}"
        + "]]}")
        .getBytes(UTF_8);
    private static final AWSIotMessage MESSAGE_PET = new AWSIotMessage(TOPIC, QOS, JSON_PET);

    private EventMarshaller _marshaller;
    private EventUnmarshaller _unmarshaller;

    @BeforeTest(groups = UNIT)
    public void setup()
    {
        _marshaller = new JsonMarshaller(QOS);
        _unmarshaller = new JsonUnmarshaller();
    }

    @DataProvider
    Object[][] createData()
    {
        return new Object[][]
            {
                {EVENT_VOID, MESSAGE_VOID},
                {EVENT_STRING, MESSAGE_STRING},
                {EVENT_DATE, MESSAGE_DATE},
                {EVENT_NUM, MESSAGE_NUM},
                {EVENT_PERSON, MESSAGE_PERSON},
                {EVENT_COL, MESSAGE_COL},
                {EVENT_PETS, MESSAGE_PET},
            };
    }

    @Test(groups = UNIT, dataProvider = "createData")
    public void eventsShouldMarshalToJson(final Event<?> event, final AWSIotMessage expected) throws MarshalException
    {
        final AWSIotMessage actual = _marshaller.marshall(event);
        assertEquals(actual.getTopic(), expected.getTopic());
        assertEquals(actual.getStringPayload(), expected.getStringPayload());
    }

    @Test(groups = UNIT, dataProvider = "createData")
    public void eventsShouldUnmarshalFromJson(final Event<?> expected, final AWSIotMessage message) throws UnmarshalException
    {
        final Event<?> actual = _unmarshaller.unmarshall(message);
        assertEquals(actual.getTopic(), expected.getTopic());
        assertEquals(actual.getPayload(), expected.getPayload());
    }
}
