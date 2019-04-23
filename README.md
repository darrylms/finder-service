# finder-service

Micronaut-based REST API implementing [transport-api](https://github.com/darrylms/transport-api).

It can be run as a standalone Java [application](https://github.com/darrylms/finder-service/blob/master/src/main/java/finder/service/Application.java) or as an [AWS Lambda function](https://github.com/darrylms/finder-service/blob/master/src/main/java/finder/service/StreamLambdaHandler.java).

The project uses Maven and relies on a prebuilt JAR of the [transport-api](https://github.com/darrylms/transport-api) to be added to the local maven repository (see [pom.xml](pom.xml)).

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for the full license text.
