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
 * Copyright 2014 ForgeRock AS.
 */

package org.forgerock.caf.authn.test.runtime;

import org.forgerock.guice.core.InjectorHolder;
import org.forgerock.jaspi.runtime.config.inject.RuntimeInjector;

/**
 * Test implementation of the JASPI runtime's {@code RuntimeInjector}, which uses Guice to construct and configure
 * the JASPI runtime.
 *
 * @since 1.5.0
 */
public class TestRuntimeInjector implements RuntimeInjector {

    /**
     * Gets the RuntimeInjector.
     *
     * @return The RuntimeInjector.
     */
    public static RuntimeInjector getRuntimeInjector() {
        return new TestRuntimeInjector();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getInstance(Class<T> type) {
        return InjectorHolder.getInstance(type);
    }
}
