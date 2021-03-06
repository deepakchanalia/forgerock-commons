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
 * Copyright 2012-2014 ForgeRock AS.
 */
package org.forgerock.json.resource;

import org.forgerock.json.fluent.JsonValue;

import java.util.UUID;

/**
 * A {@link Context} which has an an ID but no parent. All request context
 * chains are terminated by a root context as the top-most context.
 * <p>
 * Here is an example of the JSON representation of a root context:
 *
 * <pre>
 * {
 *   "id"     : "56f0fb7e-3837-464d-b9ec-9d3b6af665c3",
 *   "class"  : "org.forgerock.json.resource.RootContext",
 *   "parent" : null
 * }
 * </pre>
 */
public final class RootContext extends AbstractContext {

    // a client-friendly name for this context */
    private static final String CONTEXT_NAME = "root";

    /**
     * Creates a new root context having an ID automatically generated using
     * {@code UUID.randomUUID()}.
     */
    public RootContext() {
        this(UUID.randomUUID().toString());
    }

    /**
     * Creates a new root context having the provided ID.
     *
     * @param id
     *            The context ID.
     */
    public RootContext(final String id) {
        super(id, null); // No parent.
    }

    /**
     * Restore from JSON representation.
     *
     * @param savedContext
     *            The JSON representation from which this context's attributes
     *            should be parsed.
     * @param config
     *            The persistence configuration.
     * @throws ResourceException
     *             If the JSON representation could not be parsed.
     */
    RootContext(final JsonValue savedContext, final PersistenceConfig config)
            throws ResourceException {
        super(savedContext, config);
    }

    /**
     * Get this Context's name.
     *
     * @return this object's name
     */
    public String getContextName() {
        return CONTEXT_NAME;
    }
}
