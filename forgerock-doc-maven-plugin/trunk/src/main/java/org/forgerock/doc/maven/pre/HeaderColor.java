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
 * Copyright 2013-2015 ForgeRock AS.
 */

package org.forgerock.doc.maven.pre;

import org.apache.maven.plugin.MojoExecutionException;
import org.forgerock.doc.maven.AbstractDocbkxMojo;
import org.forgerock.doc.maven.utils.TheadRowTransformer;

import java.io.IOException;

/**
 * Add color to &lt;thead&gt;&lt;row&gt; in DocBook XML sources.
 *
 * <p>
 *
 * This adds a processing instruction that affects only FO output, such as PDF.
 */
public class HeaderColor {

    /**
     * The Mojo that holds configuration and related methods.
     */
    private AbstractDocbkxMojo m;

    /**
     * Constructor setting the Mojo that holds the configuration.
     *
     * @param mojo The Mojo that holds the configuration.
     */
    public HeaderColor(final AbstractDocbkxMojo mojo) {
        m = mojo;
    }

    /**
     * Add color to &lt;thead&gt;&lt;row&gt; in the copy of DocBook XML sources.
     *
     * @throws MojoExecutionException Failed to update an XML file.
     */
    public void execute() throws MojoExecutionException {
        TheadRowTransformer trt = new TheadRowTransformer();
        try {
            trt.update(m.getDocbkxModifiableSourcesDirectory());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
