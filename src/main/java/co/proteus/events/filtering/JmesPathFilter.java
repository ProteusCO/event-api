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
import io.burt.jmespath.JmesPath;
import io.burt.jmespath.jackson.JacksonRuntime;
import io.burt.jmespath.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;

/**
 * {@link MessageFilter} that accepts messages that match a JMESpath expression.
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
public final class JmesPathFilter implements MessageFilter
{
    private static final ObjectReader READER = new ObjectMapper()
        .reader();
    private static final Logger _logger = LogManager.getLogger(JmesPathFilter.class);

    private final Expression<JsonNode> _expression;

    /**
     * Create an instance of {@code JmesPathFilter} that accepts any messages that match the expression provided.
     *
     * @param expression the compiled expression
     */
    public JmesPathFilter(final Expression<JsonNode> expression)
    {
        _expression = expression;
    }

    /**
     * Compile a JMESpath expression. Compiled expressions are reusable and thread-safe.
     *
     * @param expression the expression
     *
     * @return the compiled expression
     *
     * @throws ParseException thrown when {@code expression} is invalid
     */
    public static Expression<JsonNode> jmesPathCompile(final String expression) throws ParseException
    {
        final JmesPath<JsonNode> jmespath = new JacksonRuntime();
        return jmespath.compile(expression);
    }

    private static boolean _deriveTruthiness(final JsonNode node)
    {
        return (node.isBoolean() && node.booleanValue())
               || (node.isNumber() && ZERO.compareTo(new BigDecimal(node.numberValue().toString())) != 0)
               || (node.isTextual() && !node.textValue().isEmpty())
               || (node.isArray() && node.size() > 0)
               || (node.isObject());
    }

    /**
     * {@inheritDoc}
     *
     * This filter accepts messages if the result of applying the JMESpath expression is:
     * <ul>
     * <li>Boolean {@code true}</li>
     * <li>A non-zero number</li>
     * <li>A non-empty string</li>
     * <li>A non-empty array</li>
     * <li>Any object</li>
     * </ul>
     */
    @Override
    public boolean accept(final Parameters params)
    {
        final String payload = params.getMessage().getStringPayload();
        try
        {
            final JsonNode input = READER.readTree(payload);
            return _deriveTruthiness(_expression.search(input));
        }
        catch (IOException e)
        {
            _logger.error("Error parsing " + payload + " as JSON", e);
            return false;
        }
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + '{' +
               "_expression=" + _expression +
               '}';
    }
}
