package finder.service;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/hello")
public class HelloController {

    static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    /**
     * Test controller
     */
    @Get("/")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Test summary",
            description = "Test description"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Return hello world",
            content = @Content(mediaType = "text/plain")
    )
    public String index() {
        logger.debug("Hello world invoked.");
        return "Hello World";
    }

}
