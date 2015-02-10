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
 * information: "Portions Copyright [year] [name of copyright owner]".
 *
 * Copyright 2014 ForgeRock AS.
 */

package org.forgerock.http;

import static org.forgerock.http.util.Duration.duration;

import java.io.Closeable;
import java.io.IOException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.forgerock.http.io.Buffer;
import org.forgerock.http.io.IO;
import org.forgerock.http.spi.ClientImpl;
import org.forgerock.http.spi.ClientImplProvider;
import org.forgerock.http.spi.Loader;
import org.forgerock.http.util.Duration;
import org.forgerock.http.util.Option;
import org.forgerock.http.util.Options;
import org.forgerock.util.Factory;
import org.forgerock.util.Reject;
import org.forgerock.util.promise.Promise;

/**
 * An HTTP client for sending requests to remote servers.
 */
public final class Client implements Closeable {

    /**
     * The TCP connect timeout for new HTTP connections. The default timeout is
     * 10 seconds.
     */
    public static final Option<Duration> OPTION_CONNECT_TIMEOUT = Option.of(Duration.class,
            duration("10 seconds"));

    /**
     * The TCP socket timeout when waiting for HTTP responses. The default
     * timeout is 10 seconds.
     */
    public static final Option<Duration> OPTION_SO_TIMEOUT = Option.of(Duration.class,
            duration("10 seconds"));

    /**
     * Specifies whether HTTP connections should be kept alive an reused for
     * additional requests. By default, connections will be reused if possible.
     */
    public static final Option<Boolean> OPTION_REUSE_CONNECTIONS = Option.withDefault(true);

    /**
     * Specifies whether requests should be retried if a failure is detected. By
     * default requests will be retried.
     */
    public static final Option<Boolean> OPTION_RETRY_REQUESTS = Option.withDefault(true);

    /**
     * Specifies the list of key managers that should be used when configuring
     * SSL/TLS connections. By default the system key manager(s) will be used.
     */
    public static final Option<KeyManager[]> OPTION_KEY_MANAGERS = Option.of(KeyManager[].class,
            null);

    /**
     * The strategy which should be used for loading the
     * {@link ClientImplProvider}. By default, the provider will be loaded using
     * a {@code ServiceLoader}.
     *
     * @see Loader#SERVICE_LOADER
     */
    public static final Option<Loader> OPTION_LOADER = Option.of(Loader.class,
            Loader.SERVICE_LOADER);

    /**
     * Specifies the maximum number of connections that should be pooled by the
     * HTTP client. At most 64 connections will be cached by default.
     */
    public static final Option<Integer> OPTION_MAX_CONNECTIONS = Option.of(Integer.class, 64);

    /**
     * Specifies the temporary storage that should be used for storing HTTP
     * responses. By default {@link IO#newTemporaryStorage()} is used.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final Option<Factory<Buffer>> OPTION_TEMPORARY_STORAGE = (Option) Option.of(
            Factory.class, IO.newTemporaryStorage());

    /**
     * Specifies the list of trust managers that should be used when configuring
     * SSL/TLS connections. By default the system trust manager(s) will be used.
     */
    public static final Option<TrustManager[]> OPTION_TRUST_MANAGERS = Option.of(
            TrustManager[].class, null);

    /** The client implementation. */
    private final ClientImpl impl;

    /**
     * SSL host name verification policies.
     */
    public static enum HostnameVerifier {
        /**
         * Accepts any host name (disables host name verification).
         */
        ALLOW_ALL,

        /**
         * Requires that the host name matches the host name presented in the
         * certificate. Similar to {@code STRICT} except that wild-cards match
         * any number of sub-domains.
         */
        BROWSER_COMPATIBLE,

        /**
         * Requires that the host name matches the host name presented in the
         * certificate. Wild-cards only match a single domain.
         */
        STRICT;
    }

    /**
     * Specifies the SSL host name verification policy. The default is to allow
     * all host names.
     */
    public static final Option<HostnameVerifier> OPTION_HOSTNAME_VERIFIER = Option.of(
            HostnameVerifier.class, HostnameVerifier.ALLOW_ALL);

    /**
     * Creates a new HTTP client using default client options. The returned
     * client must be closed when it is no longer needed by the application.
     *
     * @throws HttpApplicationException
     *             If no client provider could be found.
     */
    public Client() throws HttpApplicationException {
        this(Options.unmodifiableDefaultOptions());
    }

    /**
     * Creates a new HTTP client using the provided client options. The returned
     * client must be closed when it is no longer needed by the application.
     *
     * @param options
     *            The options which will be used to configure the client.
     * @throws HttpApplicationException
     *             If no client provider could be found, or if the client could
     *             not be configured using the provided set of options.
     * @throws NullPointerException
     *             If {@code options} was {@code null}.
     */
    public Client(final Options options) throws HttpApplicationException {
        Reject.ifNull(options);
        final Loader loader = options.get(OPTION_LOADER);
        final ClientImplProvider factory = loader.load(ClientImplProvider.class, options);
        if (factory == null) {
            throw new HttpApplicationException("No HTTP client factory found");
        }
        this.impl = factory.newClientImpl(options);
    }

    /**
     * Completes all pending requests and release resources associated with
     * underlying implementation.
     */
    @Override
    public void close() throws IOException {
        impl.close();
    }

    /**
     * Sends an HTTP request to a remote server and returns the response.
     *
     * @param request
     *            The HTTP request to send.
     * @return The HTTP response if the response has a 2xx status code.
     * @throws ResponseException
     *             The HTTP error response if the response did not have a 2xx
     *             status code.
     */
    public Response send(final Request request) throws ResponseException {
        try {
            return sendAsync(request).getOrThrow();
        } catch (final InterruptedException e) {
            // FIXME: is a 408 time out the best status code?
            throw new ResponseException(408);
        }
    }

    /**
     * Sends an HTTP request to a remote server and returns a {@code Promise}
     * representing the asynchronous response.
     *
     * @param request
     *            The HTTP request to send.
     * @return A promise representing the pending HTTP response. The promise
     *         will yield a {@code ResponseException} when a non-2xx HTTP status
     *         code is returned.
     */
    public Promise<Response, ResponseException> sendAsync(final Request request) {
        return impl.sendAsync(request);
    }
}
