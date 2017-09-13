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

/**
 * Exception thrown when there is a problem decoding an IoT message.
 *
 * @author Justin Piper (jpiper@proteus.co)
 */
public class UnmarshalException extends Exception
{
    private static final long serialVersionUID = -2663834978786797911L;

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be
     * initialized by a call to initCause.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the getMessage() method.
     */
    public UnmarshalException(String message)
    {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * Note that the detail message associated with cause is not automatically incorporated in this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method).
     * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
     * indicates that the cause is nonexistent or unknown.)
     */
    public UnmarshalException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which
     * typically contains the class and detail message of cause). This constructor is useful for exceptions that are little more
     * than wrappers for other throwables (for example, java.security.PrivilegedActionException).
     *
     * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
     * indicates that the cause is nonexistent or unknown.)
     */
    public UnmarshalException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message, cause, suppression enabled or disabled, and writable stack
     * trace enabled or disabled.
     *
     * @param message the detail message.
     * @param cause the cause. (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    public UnmarshalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
