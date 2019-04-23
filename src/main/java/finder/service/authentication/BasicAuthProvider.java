package finder.service.authentication;

import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UserDetails;
import io.reactivex.Flowable;
import io.micronaut.context.annotation.Property;
import org.reactivestreams.Publisher;

import javax.inject.Singleton;
import java.util.ArrayList;

@Singleton
public class BasicAuthProvider implements AuthenticationProvider {

    private String username;
    private String password;

    public BasicAuthProvider(@Property(name="auth.username") String username, @Property(name="auth.password") String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
        if ( authenticationRequest.getIdentity().equals(username) &&
                authenticationRequest.getSecret().equals(password) ) {
            return Flowable.just(new UserDetails((String) authenticationRequest.getIdentity(), new ArrayList<>()));
        }
        return Flowable.just(new AuthenticationFailed());
    }
}
