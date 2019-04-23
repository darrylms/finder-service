package finder.service;

import finder.service.unirest.UnirestConfig;
import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
        info = @Info(
                title = "finder-service",
                version = "0.1",
                description = "REST API beckend for the Finder Alexa Skill",
                license = @License(name = "Apache 2.0", url = ""),
                contact = @Contact(url = "", name = "Darryl", email = "darryl@example.com")
        )
)
public class Application {

    public static void main(String[] args) {
        ApplicationContext ctx = Micronaut.run(Application.class);
        Integer retry = ctx.getEnvironment().getProperty("connections.retry", Integer.class).orElse(3);
        UnirestConfig.configure(retry);
    }
}
