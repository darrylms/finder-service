package finder.service.service;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.snowball.location.transport_api.TransportService;
import io.micronaut.context.annotation.Property;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class FinderTransportService extends TransportService {

    public FinderTransportService(
            @Property(name = "finder-transport-service.app-id") String appId,
            @Property(name = "finder-transport-service.app-key") String appKey,
            @Property(name = "finder-transport-service.optional.public-journey-endpoint")Optional<String> publicJourneyEndpoint) throws UnirestException {
        super(appId, appKey);
        if (publicJourneyEndpoint.isPresent()) {
            setPublicJourneyEndpoint(publicJourneyEndpoint.get());
        }
    }
}
