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

import io.burt.jmespath.Expression;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import co.proteus.events.filtering.MessageFilter.Parameters;

import static co.proteus.events.TestGroups.UNIT;
import static co.proteus.events.filtering.JmesPathFilter.jmesPathCompile;
import static com.amazonaws.services.iot.client.AWSIotQos.QOS0;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link JmesPathFilter} truthiness.
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
public final class JmesPathTest
{
    private static final Expression<JsonNode> EXPRESSION = jmesPathCompile("payload");

    private static final AWSIotQos QOS = QOS0;
    private static final String TOPIC = "sample/topic";

    private static final String JSON_BOOL_TRUE = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "true}");
    private static final String JSON_BOOL_FALSE = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "false}");

    private static final String JSON_NUM_TRUE = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "-1234.56789}");
    private static final String JSON_NUM_FALSE = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "0.0}");

    private static final String JSON_STRING_TRUE = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "\"false\"}");
    private static final String JSON_STRING_FALSE = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "\"\"}");

    private static final String JSON_ARRAY_TRUE = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "\"hello\"}");
    private static final String JSON_ARRAY_FALSE = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "\"\"}");

    private static final String JSON_OBJECT_TRUE = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "{\"greeting\":\"hello\"}}");
    private static final String JSON_OBJECT_FALSE = (
        "{\"eventType\":\"sample-type\",\"payload\":"
        + "null}");

    private MessageFilter _filter;

    @BeforeTest(groups = UNIT)
    public void setup()
    {
        _filter = new JmesPathFilter(EXPRESSION);
    }

    @DataProvider
    Object[][] trueValues()
    {
        return new Object[][]
            {
                {JSON_BOOL_TRUE},
                {JSON_NUM_TRUE},
                {JSON_STRING_TRUE},
                {JSON_ARRAY_TRUE},
                {JSON_OBJECT_TRUE},
            };
    }

    @DataProvider
    Object[][] falseValues()
    {
        return new Object[][]
            {
                {JSON_BOOL_FALSE},
                {JSON_NUM_FALSE},
                {JSON_STRING_FALSE},
                {JSON_ARRAY_FALSE},
                {JSON_OBJECT_FALSE},
            };
    }

    @Test(groups = UNIT, dataProvider = "trueValues")
    public void checkTrueValues(final String payload)
    {
        assertTrue(_filter.accept(new Parameters(new AWSIotMessage(TOPIC, QOS, payload.getBytes(UTF_8)))));
    }

    @Test(groups = UNIT, dataProvider = "falseValues")
    public void checkFalseValues(final String payload)
    {
        assertFalse(_filter.accept(new Parameters(new AWSIotMessage(TOPIC, QOS, payload.getBytes(UTF_8)))));
    }
}
