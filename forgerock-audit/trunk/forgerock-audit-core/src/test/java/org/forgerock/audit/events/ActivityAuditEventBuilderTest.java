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
 * Copyright 2015 ForgeRock AS.
 */
package org.forgerock.audit.events;

import static java.util.Arrays.*;
import static org.fest.assertions.api.Assertions.*;
import static org.forgerock.audit.events.ActivityAuditEventBuilderTest.OpenProductActivityAuditEventBuilder.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.resource.Request;
import org.forgerock.json.resource.Requests;
import org.forgerock.json.resource.servlet.HttpContext;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class ActivityAuditEventBuilderTest {

    private static final ObjectMapper MAPPER = new ObjectMapper(
            new JsonFactory().configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true));

    /**
     * Example builder of audit activity events for some imaginary product "OpenProduct".
     */
    static class OpenProductActivityAuditEventBuilder<T extends OpenProductActivityAuditEventBuilder<T>>
            extends ActivityAuditEventBuilder<T> {

        @SuppressWarnings("rawtypes")
        public static OpenProductActivityAuditEventBuilder<?> productActivityEvent() {
            return new OpenProductActivityAuditEventBuilder();
        }

        public T openField(String v) {
            jsonValue.put("open", v);
            return self();
        }

    }

    @Test
    public void ensureEventIsCorrectlyBuilt() {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Content-Length", asList("200"));
        headers.put("Content-Type", asList("application/json"));

        AuditEvent event = productActivityEvent()
                .transactionId("transactionId")
                .timestamp(1427293286239L)
                .eventName("AM-REALM-CREATE")
                .authentication("someone@forgerock.com")
                .runAs("admin")
                .resourceOperation("some/resource", "CREST", "ACTION", "customAction")
                .before("{ \"name\": \"Old\", \"revision\": 1 }")
                .after("{ \"name\": \"New\", \"revision\": 2 }")
                .changedFields("name", "revision")
                .revision(2)
                .openField("value")
                .toEvent();

        assertEvent(event);
    }

    @Test
    public void canPopulateResourceOperationFromContextAndRequest() throws Exception {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Content-Length", asList("200"));
        headers.put("Content-Type", asList("application/json"));

        HttpContext context = new HttpContext(jsonFromFile("/httpContext.json"), null);
        Request request = Requests.newActionRequest("some/resource", "customAction");

        AuditEvent event = productActivityEvent()
                .transactionId("transactionId")
                .timestamp(1427293286239L)
                .eventName("AM-REALM-CREATE")
                .authentication("someone@forgerock.com")
                .resourceOperationFromRequest(request)
                .toEvent();

        final JsonValue resourceOperation = event.getValue().get(RESOURCE_OPERATION);

        assertThat(resourceOperation.get(URI).asString().equals("some/resource"));
        assertThat(resourceOperation.get(PROTOCOL).asString().equals("http"));
        assertThat(resourceOperation.get(OPERATION).get(METHOD).asString().equals("ACTION"));
        assertThat(resourceOperation.get(OPERATION).get(DETAIL).asString().equals("customAction"));
    }

    @Test
    public void ensureBuilderMethodsCanBeCalledInAnyOrder() {
        AuditEvent event = productActivityEvent()
                .eventName("AM-REALM-CREATE")
                .authentication("someone@forgerock.com")
                .runAs("admin")
                .resourceOperation("some/resource", "CREST", "ACTION", "customAction")
                .before("{ \"name\": \"Old\", \"revision\": 1 }")
                .after("{ \"name\": \"New\", \"revision\": 2 }")
                .changedFields("name", "revision")
                .revision(2)
                .openField("value")
                .transactionId("transactionId")
                .timestamp(1427293286239L)
                .toEvent();
        assertEvent(event);
    }

    private void assertEvent(AuditEvent event) {
        JsonValue value = event.getValue();
        assertThat(value.get(TRANSACTION_ID).asString()).isEqualTo("transactionId");
        assertThat(value.get(TIMESTAMP).asString()).isEqualTo("2015-03-25T14:21:26.239Z");
        assertThat(value.get(EVENT_NAME).asString()).isEqualTo("AM-REALM-CREATE");
        assertThat(value.get(AUTHENTICATION).get(ID).asString()).isEqualTo("someone@forgerock.com");
        assertThat(value.get(RUN_AS).asString()).isEqualTo("admin");
        assertThat(value.get(RESOURCE_OPERATION).get(URI).asString()).isEqualTo("some/resource");
        assertThat(value.get(RESOURCE_OPERATION).get(PROTOCOL).asString()).isEqualTo("CREST");
        assertThat(value.get(RESOURCE_OPERATION).get(OPERATION).get(METHOD).asString()).isEqualTo("ACTION");
        assertThat(value.get(RESOURCE_OPERATION).get(OPERATION).get(DETAIL).asString()).isEqualTo("customAction");
        assertThat(value.get(BEFORE).asString()).isEqualTo("{ \"name\": \"Old\", \"revision\": 1 }");
        assertThat(value.get(AFTER).asString()).isEqualTo("{ \"name\": \"New\", \"revision\": 2 }");
        assertThat(value.get(CHANGED_FIELDS).asList(String.class)).containsExactly("name", "revision");
        assertThat(value.get(REVISION).asLong()).isEqualTo(2);
        assertThat(value.get("open").getObject()).isEqualTo("value");
    }

    private JsonValue jsonFromFile(String resourceFilePath) throws IOException {
        final InputStream configStream = ActivityAuditEventBuilderTest.class.getResourceAsStream(resourceFilePath);
        return new JsonValue(MAPPER.readValue(configStream, Map.class));
    }

}
