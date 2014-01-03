/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2013 ForgeRock AS.
 */

package org.forgerock.json.jose.exceptions;

/**
 * Represents an exception that occurs when a JWT is determined as invalid.
 * <p>
 * Possible causes, but not definitive, are incorrect number of "." characters, unparsable JSON,
 * duplicate entries in header or claims set.
 *
 * @author Phill Cunnington
 * @since 2.0.0
 */
public class InvalidJwtException extends JwtReconstructionException {

    /** Serializable class version number. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new JwtReconstructionException with the provided exception message.
     *
     * @param message The exception message.
     */
    public InvalidJwtException(String message) {
        super(message);
    }

    /**
     * Constructs a new JwtReconstructionException with the provided exception message and underlying throwable.
     *
     * @param message The exception message.
     * @param throwable The underlying throwable.
     */
    public InvalidJwtException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Constructs a new JwtReconstructionException with the provided underlying throwable.
     *
     * @param throwable The underlying throwable.
     */
    public InvalidJwtException(Throwable throwable) {
        super(throwable);
    }
}