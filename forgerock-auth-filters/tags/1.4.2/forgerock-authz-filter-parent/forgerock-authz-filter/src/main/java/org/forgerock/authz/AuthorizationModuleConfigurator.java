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

package org.forgerock.authz;

import org.forgerock.json.fluent.JsonValue;

/**
 * Interface to provide an instance of the Authorization Module and any configuration that it requires.
 *
 * @since 1.4.0
 */
public interface AuthorizationModuleConfigurator {

    /**
     * Returns an instance of the desired AuthorizationModule.
     * <br/>
     * The instance should be a freshly created AuthorizationModule without the initialise method being called, as
     * this will get called by the Authz framework.
     *
     * @return An instance of the AuthorizationModule.
     */
    AuthorizationModule getModule();

    /**
     * Returns the configuration that will be passed to the Authorization Module.
     *
     * @return The Authorization Modules configuration.
     */
    JsonValue getConfiguration();
}
