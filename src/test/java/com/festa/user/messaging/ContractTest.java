package com.festa.user.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.festa.user.config.AppProperties;
import com.festa.user.config.RabbitConfig;
import com.festa.user.domain.event.DomainEvent;
import com.festa.user.domain.event.inbound.AccountDeletedEvent;
import com.festa.user.domain.event.inbound.AccountRegisteredEvent;
import com.festa.user.domain.event.outbound.LocationConsentGrantedEvent;
import com.festa.user.domain.event.outbound.LocationConsentRevokedEvent;
import com.festa.user.domain.event.outbound.MerchantRejectedEvent;
import com.festa.user.domain.event.outbound.MerchantSubmittedEvent;
import com.festa.user.domain.event.outbound.MerchantUpdatedEvent;
import com.festa.user.domain.event.outbound.MerchantVerifiedEvent;
import com.festa.user.domain.event.outbound.ProfileUpdatedEvent;
import com.festa.user.domain.event.outbound.EventHiddenEvent;
import com.festa.user.domain.event.outbound.EventRestoredEvent;
import com.festa.user.domain.event.outbound.ReportCreatedEvent;
import com.festa.user.domain.event.outbound.ReportResolvedEvent;
import com.festa.user.domain.event.outbound.UserReinstatedEvent;
import com.festa.user.domain.event.outbound.UserSuspendedEvent;
import com.festa.user.domain.model.MerchantProfile;
import com.festa.user.domain.vo.AccountId;
import com.festa.user.domain.vo.BusinessCategory;
import com.festa.user.domain.vo.Interest;
import com.festa.user.infrastructure.messaging.InboundEnvelopeParser;
import com.festa.user.infrastructure.persistence.adapter.OutboxRepositoryAdapter;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Verifica user-service contra festa-infra/contracts (única fuente de verdad): lo que publica,
 * lo que consume y la topología que declara. Los contratos se ubican con -Dfesta.contracts.dir
 * (el pom lo pone solo si existe ../festa-infra); sin ellos el test se omite.
 */
class ContractTest {

    private static Path contracts;
    private static final ObjectMapper MAPPER = Jackson2ObjectMapperBuilder.json()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    private static final JsonSchemaFactory FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    private static final Instant NOW = Instant.parse("2026-11-06T15:00:00.123456Z");

    @BeforeAll
    static void locate() {
        String dir = System.getProperty("festa.contracts.dir");
        contracts = dir == null || dir.isBlank() ? null : Path.of(dir);
        assumeTrue(contracts != null && Files.isDirectory(contracts.resolve("events")),
                "Contratos no disponibles (festa.contracts.dir); se omite el test de contrato");
    }

    static Stream<Arguments> publishedEvents() {
        UUID userId = UUID.randomUUID();
        UUID admin = UUID.randomUUID();
        MerchantProfile verified = merchant(userId, BusinessCategory.GASTRONOMICO).approve(NOW);
        MerchantProfile otro = merchant(userId, BusinessCategory.OTRO);
        return Stream.of(
                Arguments.of(ProfileUpdatedEvent.of(userId, "Ana Pérez", Set.of(Interest.MUSICAL, Interest.CULTURAL), NOW)),
                Arguments.of(ProfileUpdatedEvent.of(userId, "Ana Pérez", Set.of(), NOW)),
                Arguments.of(LocationConsentGrantedEvent.of(userId, "2026-10", NOW, NOW)),
                Arguments.of(LocationConsentRevokedEvent.of(userId, NOW, NOW)),
                Arguments.of(MerchantSubmittedEvent.of(otro, NOW)),
                Arguments.of(MerchantVerifiedEvent.of(verified, admin, NOW)),
                Arguments.of(MerchantRejectedEvent.of(otro.reject("No se pudo validar la dirección", NOW), "No se pudo validar la dirección", admin, NOW)),
                Arguments.of(MerchantUpdatedEvent.of(verified, NOW)),
                Arguments.of(MerchantUpdatedEvent.of(otro, NOW)),
                Arguments.of(ReportCreatedEvent.of(UUID.randomUUID(), userId, "event", UUID.randomUUID(), "spam", "Publica lo mismo cada hora", NOW)),
                Arguments.of(ReportCreatedEvent.of(UUID.randomUUID(), userId, "user", UUID.randomUUID(), "acoso", null, NOW)),
                Arguments.of(ReportResolvedEvent.of(UUID.randomUUID(), userId, "event", UUID.randomUUID(), "action_taken", admin, NOW)),
                Arguments.of(UserSuspendedEvent.of(userId, UUID.randomUUID(), "Fraude en la venta de boletas", NOW.plus(Duration.ofDays(7)), admin, NOW)),
                Arguments.of(UserSuspendedEvent.of(userId, null, "Fraude", null, admin, NOW)),
                Arguments.of(UserReinstatedEvent.of(userId, admin, NOW)),
                Arguments.of(EventHiddenEvent.of(UUID.randomUUID(), UUID.randomUUID(), "Información falsa", admin, NOW)),
                Arguments.of(EventHiddenEvent.of(UUID.randomUUID(), null, "Spam", admin, NOW)),
                Arguments.of(EventRestoredEvent.of(UUID.randomUUID(), admin, NOW)));
    }

    @ParameterizedTest(name = "data de {0}")
    @MethodSource("publishedEvents")
    void dataCumpleElEsquema(DomainEvent event) throws Exception {
        JsonNode schemaNode = MAPPER.readTree(schemaFile(event.eventType()).toFile());
        assertThat(schemaNode.path("x-exchange").asText()).isEqualTo(RabbitConfig.USER_EXCHANGE);
        assertThat(schemaNode.path("x-routing-key").asText()).isEqualTo(event.eventType());

        JsonNode data = MAPPER.valueToTree(event);
        assertThat(schema(schemaFile(event.eventType())).validate(data)).as(event.eventType() + " " + data).isEmpty();
    }

    @ParameterizedTest(name = "sobre de {0}")
    @MethodSource("publishedEvents")
    void sobreCumpleElEsquema(DomainEvent event) throws Exception {
        ObjectNode envelope = new OutboxRepositoryAdapter(null, MAPPER).envelope(event, null, null);
        assertThat(envelope.has("correlationId")).isTrue();
        assertThat(envelope.path("producer").asText())
                .isEqualTo(MAPPER.readTree(schemaFile(event.eventType()).toFile()).path("x-producer").asText());
        assertThat(schema(contracts.resolve("envelope.schema.json")).validate(envelope)).isEmpty();
    }

    @Test
    void consumeElEjemploDeAccountRegistered() throws Exception {
        JsonNode data = example("auth/account.registered");
        DomainEvent parsed = parser().parse(envelopeBytes("account.registered", "auth-service", data));

        assertThat(parsed).isInstanceOfSatisfying(AccountRegisteredEvent.class, e -> {
            assertThat(e.accountId()).hasToString(data.path("userId").asText());
            assertThat(e.displayName()).isEqualTo(data.path("displayName").asText());
            assertThat(e.role()).isEqualTo(data.path("role").asText());
        });
    }

    @Test
    void consumeElEjemploDeAccountDeleted() throws Exception {
        JsonNode data = example("auth/account.deleted");
        DomainEvent parsed = parser().parse(envelopeBytes("account.deleted", "auth-service", data));

        assertThat(parsed).isInstanceOfSatisfying(AccountDeletedEvent.class,
                e -> assertThat(e.accountId()).hasToString(data.path("userId").asText()));
    }

    @Test
    void rechazaProductorFalsoYEventosQueNoConsume() throws Exception {
        JsonNode data = example("auth/account.deleted");
        assertThatThrownBy(() -> parser().parse(envelopeBytes("account.deleted", "event-service", data)))
                .isInstanceOf(InboundEnvelopeParser.InvalidMessageException.class);
        assertThatThrownBy(() -> parser().parse(envelopeBytes("account.role_changed", "auth-service", data)))
                .isInstanceOf(InboundEnvelopeParser.InvalidMessageException.class);
        assertThatThrownBy(() -> parser().parse("no es json".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(InboundEnvelopeParser.InvalidMessageException.class);
    }

    @Test
    void topologiaIgualADefinitionsJson() throws Exception {
        JsonNode definitions = MAPPER.readTree(contracts.resolve("rabbitmq/definitions.json").toFile());
        AppProperties props = new AppProperties(null, null, new AppProperties.Outbox(Duration.ofSeconds(2), 50),
                new AppProperties.Rabbit("festa.user", "user-service.inbox", "user-service.inbox.retry", "user-service.inbox.dlq"));
        var declared = new RabbitConfig().messagingTopology(props);

        Map<String, Map<String, Object>> expectedQueues = new HashMap<>();
        for (JsonNode q : definitions.path("queues")) {
            if (q.path("name").asText().startsWith("user-service.")) {
                expectedQueues.put(q.path("name").asText(), MAPPER.convertValue(q.path("arguments"), Map.class));
            }
        }
        Map<String, Map<String, Object>> declaredQueues = new HashMap<>();
        declared.getDeclarablesByType(Queue.class).forEach(q -> declaredQueues.put(q.getName(), q.getArguments()));
        assertThat(declaredQueues).isEqualTo(expectedQueues);

        Set<String> expectedBindings = new java.util.HashSet<>();
        for (JsonNode b : definitions.path("bindings")) {
            if (b.path("destination").asText().startsWith("user-service.")) {
                expectedBindings.add(b.path("source").asText() + " " + b.path("routing_key").asText() + " " + b.path("destination").asText());
            }
        }
        Set<String> declaredBindings = new java.util.HashSet<>();
        declared.getDeclarablesByType(Binding.class)
                .forEach(b -> declaredBindings.add(b.getExchange() + " " + b.getRoutingKey() + " " + b.getDestination()));
        assertThat(declaredBindings).isEqualTo(expectedBindings);
    }

    private static MerchantProfile merchant(UUID userId, BusinessCategory category) {
        return MerchantProfile.submit(AccountId.of(userId), "Café Bohemio", category,
                "Cl. 11 #3-45, Bogotá", 4.5981, -74.0758, "Café y música en vivo", NOW);
    }

    private static InboundEnvelopeParser parser() {
        return new InboundEnvelopeParser(MAPPER);
    }

    private static JsonNode example(String event) throws Exception {
        return MAPPER.readTree(contracts.resolve("events/" + event + ".schema.json").toFile()).path("examples").get(0);
    }

    private static byte[] envelopeBytes(String eventType, String producer, JsonNode data) throws Exception {
        ObjectNode env = MAPPER.createObjectNode();
        env.put("eventId", UUID.randomUUID().toString());
        env.put("eventType", eventType);
        env.put("version", 1);
        env.put("occurredAt", NOW.toString());
        env.put("producer", producer);
        env.putNull("correlationId");
        env.putNull("causationId");
        env.set("data", data);
        assertThat(schema(contracts.resolve("envelope.schema.json")).validate(env)).isEmpty();
        return MAPPER.writeValueAsBytes(env);
    }

    private static Path schemaFile(String eventType) {
        return contracts.resolve("events/user/" + eventType + ".schema.json");
    }

    private static JsonSchema schema(Path path) throws Exception {
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().formatAssertionsEnabled(true).build();
        return FACTORY.getSchema(Files.readString(path), config);
    }
}
