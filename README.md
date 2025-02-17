# project_name

This is template for microservice app, use Kotlin, Ktor, Coroutines. R2DBC as data access engine. Mysql (Postgres) as database. Kafka as broker.

## Features

Here's a list of features included in this project:

| Name                                                               | Description                                                                        |
| --------------------------------------------------------------------|------------------------------------------------------------------------------------ |
| [Kafka](https://start.ktor.io/p/ktor-server-kafka)                 | Adds Kafka support to your application                                             |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation) | Provides automatic content conversion according to Content-Type and Accept headers |
| [Routing](https://start.ktor.io/p/routing)                         | Provides a structured routing DSL                                                  |
| [GSON](https://start.ktor.io/p/ktor-gson)                          | Handles JSON serialization using GSON library                                      |
| [Call Logging](https://start.ktor.io/p/call-logging)               | Logs client requests                                                               |
| [Call ID](https://start.ktor.io/p/callid)                          | Allows to identify a request/call.                                                 |
| [Authentication](https://start.ktor.io/p/auth)                     | Provides extension point for handling the Authorization header                     |
| [Authentication JWT](https://start.ktor.io/p/auth-jwt)             | Handles JSON Web Token (JWT) bearer authentication scheme                          |
| [Request Validation](https://start.ktor.io/p/request-validation)   | Adds validation for incoming requests                                              |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
| -------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `buildImage`                  | Build the docker image to use with the fat JAR                       |
| `publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `run`                         | Run the server                                                       |
| `runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

