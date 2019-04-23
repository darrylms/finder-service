package finder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.snowball.location.transport_api.GoogleMapsService;
import com.snowball.location.transport_api.exception.PlaceNotFoundException;
import com.snowball.location.transport_api.response.GooglePlace;
import com.snowball.location.transport_api.response.PublicJourneyContainer;
import finder.service.service.FinderTransportService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/journey")
public class JourneyController {

    static final Logger logger = LoggerFactory.getLogger(JourneyController.class);

    @Inject
    FinderTransportService finderTransportService;

    @Inject
    GoogleMapsService googleMapsService;

    /**
     * Get a public transport journey from one location to another.
     * @param from Address/postcode from which the journey starts
     * @param to Destination address or postcode
     * @return Journey taken from start to destination
     */
    /* TODO: Return json-encoded error messages */
    @Get("/public/{from}/{to}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Request a journey from one location in the UK to another",
            description = "Return a JSON-encoded public transit journey from one address in the UK to another"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Return a JSON-encoded public transit journey from one address in the UK to another",
        content = @Content(mediaType = "application/json"
        )
    )
    @ApiResponse(responseCode = "204", description = "None found")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "504", description = "Internal server error")
    public HttpResponse getPublicJourney(@PathVariable String from, @PathVariable String to) {
        try {
            GooglePlace fromPlace = googleMapsService.findPlace(from);
            GooglePlace toPlace = googleMapsService.findPlace(to);
            PublicJourneyContainer journeyCont = finderTransportService.findJourney(fromPlace, toPlace);
            return HttpResponse.status(HttpStatus.OK).body(new ObjectMapper().writeValueAsString(journeyCont));
        } catch (PlaceNotFoundException e) {
            logger.error(e.getMessage(), e);
            /* Return an empty response instead of empty PublicJourneyContainer */
            return HttpResponse.status(HttpStatus.NO_CONTENT).body("");
        } catch (UnirestException e) {
            logger.error(e.getMessage(), e);
            return HttpResponse.status(HttpStatus.BAD_REQUEST).body("Bad request");
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not interpret response from API");
        }
    }
}
