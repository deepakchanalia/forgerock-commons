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

package org.forgerock.json.jose.utils;

/**
 * A DuplicateMapEntryException is thrown when an entry is attempted to be added to a {@link NoDuplicatesMap} which
 * already contains an entry with the same key.
 *
 * @author Phill Cunnington
 * @since 2.0.0
 */
public class DuplicateMapEntryException extends RuntimeException {

    /** Serializable class version number. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new DuplicateMapEntryException with the specified detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message The detail message. The detail message is saved for later retrieval by the
     *                {@link #getMessage()} method.
     */
    public DuplicateMapEntryException(String message) {
        super(message);
    }
}
