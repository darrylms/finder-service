package finder.service.service;

import com.snowball.location.transport_api.GoogleMapsService;
import io.micronaut.context.annotation.Property;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class FinderGoogleMapsService extends GoogleMapsService {

    public FinderGoogleMapsService(
            @Property(name="google-maps-service.api-key") String apiKey,
            @Property(name = "google-maps-service.optional.place-endpoint") Optional<String> placeEndpoint) {
        super(apiKey);
        if(placeEndpoint.isPresent()) {
            setPlaceEndpoint(placeEndpoint.get());
        }
    }

}
