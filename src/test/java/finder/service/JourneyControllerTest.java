package finder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.UrlEscapers;
import com.snowball.location.transport_api.exception.PlaceNotFoundException;
import com.snowball.location.transport_api.response.*;
import finder.service.unirest.UnirestConfig;
import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.uri.UriMatchTemplate;
import io.micronaut.runtime.server.EmbeddedServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JourneyControllerTest {

    private EmbeddedServer server;
    private HttpClient client;

    @BeforeAll
    public void setupServer() {
        server = ApplicationContext.run(EmbeddedServer.class);
        client = server
                .getApplicationContext()
                .createBean(HttpClient.class, server.getURL());
        UnirestConfig.configure(3);
    }

    @AfterAll
    public void stopServer() {
        if (server != null) server.stop();
        if (client != null) client.stop();
    }

    @Test
    public void testRoutes() {
        UriMatchTemplate template = UriMatchTemplate.of("/journey/public/{from}/{to}");
        assertTrue(template.match("/journey/public/WWW%20A23/street%20number%20street%20town").isPresent());
        assertTrue(template.match("/journey/public/W1W 5DW/15a high street orpington").isPresent());
        assertFalse(template.match("/journey/public/only-one-param").isPresent());
    }

    @Test
    public void testGetPublicJourney() throws IOException {
        String fromLocation = "Barbican Centre";
        String toLocation = "London Bridge";

        GoogleLocation loc = new GoogleLocation();
        loc.setLat("51.50544");
        loc.setLng("-0.09106059999999999");
        GoogleGeometry geo = GoogleGeometry.builder().location(loc).build();
        GooglePlace place = new GooglePlace();
        place.setAddress("address line 1, city postcode, UK");
        place.setName("Place 1");
        place.setGeometry(geo);

        GoogleLocation loc2 = new GoogleLocation();
        loc2.setLat("51.5031653");
        loc2.setLng("-0.1123051");
        GoogleGeometry geo2 = GoogleGeometry.builder().location(loc).build();
        GooglePlace place2 = new GooglePlace();
        place2.setAddress("some other address line 1, city postcode, UK");
        place2.setName("Place 2");
        place2.setGeometry(geo2);

        GooglePlaceContainer googleCont = new GooglePlaceContainer();
        googleCont.setStatus(GooglePlaceContainer.STATUS_OK);
        googleCont.setPlaces(Arrays.asList(place));

        GooglePlaceContainer googleCont2 = new GooglePlaceContainer();
        googleCont2.setStatus(GooglePlaceContainer.STATUS_OK);
        googleCont2.setPlaces(Arrays.asList(place2));

        RoutePart part = new RoutePart();
        part.setMode(RoutePart.MODE_TRAM);
        part.setFrom("Tramp stop 1");
        part.setTo("Tram stop 2");
        part.setDestination("End of the line");
        part.setDeparture("07:33");
        part.setArrival("07:41");
        part.setLineName("");
        part.setDuration("00:08:00");

        RoutePart part2 = new RoutePart();
        part2.setMode(RoutePart.MODE_FOOT);
        part2.setFrom("Tram stop 2");
        part2.setTo("Bus stop 1");
        part2.setDestination("");
        part2.setDeparture("07:41");
        part2.setArrival("07:45");
        part2.setLineName("");
        part2.setDuration("00:04:00");

        RoutePart part3 = new RoutePart();
        part3.setMode(RoutePart.MODE_BUS);
        part3.setFrom("Bus stop 1");
        part3.setTo("Bus stop 2");
        part3.setDestination("Some destination");
        part3.setDeparture("07:45");
        part3.setArrival("08:20");
        part3.setLineName("J45");
        part3.setDuration("00:35:00");

        PublicJourneyRoute route = new PublicJourneyRoute();
        route.setDeparture("07:33");
        route.setArrival("08:20");
        route.setArrivalDate("");
        route.setRouteParts(Arrays.asList(part, part2, part3));

        PublicJourneyContainer cont = new PublicJourneyContainer();
        cont.setRequestTime("07:32");
        cont.setSource("test source");
        cont.setAcknowledgements("test ack");
        cont.setRoutes(Collections.singletonList(route));

        String mockJourneyEndpoint = "/uk/public/journey/from/(.*)/to/(.*).json.*";
        String mockMapsEndpoint = "/maps/api/place/findplacefromtext/json.*";

        WireMockServer wireMockServer = setupWireMock(convertToJson(cont), mockJourneyEndpoint);
        addMockResponse(wireMockServer, convertToJson(googleCont), mockMapsEndpoint);
        Map<String, StringValuePattern> queryParams = ImmutableMap.of(
                "input", equalTo("London Bridge"));
        addMockResponse(wireMockServer, convertToJson(googleCont2), mockMapsEndpoint, queryParams);
        wireMockServer.start();

        String endpoint = String.format("/journey/public/%s/%s", UrlEscapers.urlFragmentEscaper().escape(fromLocation), UrlEscapers.urlFragmentEscaper().escape(toLocation));
        HttpRequest<String> request = HttpRequest.GET(endpoint);
        String result = client.toBlocking().retrieve(request);

        PublicJourneyContainer resultJourney = getObjectMapper().readValue(result, PublicJourneyContainer.class);
        assertEquals(resultJourney.getRoutes().size(), 1);
        assertEquals(resultJourney.getRoutes().get(0).getRouteParts().size(), 3);
        assertEquals(resultJourney.getRoutes().get(0).getRouteParts().get(2).getMode(), RoutePart.MODE_BUS);
        wireMockServer.stop();
    }

    private String convertToJson(Object cont) throws JsonProcessingException {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = getObjectMapper();
        return objectMapper.writeValueAsString(cont);
    }

    private static com.fasterxml.jackson.databind.ObjectMapper getObjectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .registerModule(new JavaTimeModule())
                .findAndRegisterModules();
    }

    private WireMockServer setupWireMock(String serverResponse, String endpoint) {
        WireMockServer wireMockServer = new WireMockServer(options().port(8745));
        configureFor("localhost", 8745);
        addMockResponse(wireMockServer, serverResponse, endpoint);
        return wireMockServer;
    }

    private void addMockResponse(WireMockServer wireMockServer, String serverResponse, String endpoint) {
        wireMockServer.stubFor(get(urlPathMatching(endpoint))
                .atPriority(5)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(serverResponse)));
    }

    private void addMockResponse(WireMockServer wireMockServer, String serverResponse, String endpoint, Map<String, StringValuePattern> queryParams) {
        wireMockServer.stubFor(get(urlPathMatching(endpoint))
                .atPriority(1)
                .withQueryParams(queryParams)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(serverResponse)));
    }

}
