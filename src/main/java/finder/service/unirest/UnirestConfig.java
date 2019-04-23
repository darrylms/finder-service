package finder.service.unirest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import io.micronaut.context.annotation.Property;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class UnirestConfig {

    public static void configure(int retry) {
        System.out.println("Configuring unirest with retries " + retry);
        Unirest.setObjectMapper(new ObjectMapper() {
            //Ensure Jackson modules to handle Java 8 datatypes are loaded.
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper()
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                    //.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)
                    .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                    .registerModule(new JavaTimeModule())
                    .findAndRegisterModules();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                            = new com.fasterxml.jackson.databind.ObjectMapper();
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Unirest.setHttpClient(HttpClientBuilder.create()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(retry, true))
                .build());
    }
}
