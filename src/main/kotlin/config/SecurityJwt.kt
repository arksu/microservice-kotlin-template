package com.company.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*

fun Application.configureJwtSecurity() {
    // Please read the jwt property from the config file if you are using EngineMain
    val jwtAudience = "jwt-audience"
    val jwtDomain = "https://jwt-provider-domain/"
    val jwtRealm = "ktor sample app"
    val jwtSecret = "secret"
    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
//                    .withAudience(jwtAudience)
//                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("permissions").isNull) {
                    null
                } else {
                    JWTPrincipal(credential.payload)
                }

//                if (credential.payload.audience.contains(jwtAudience))
//                    JWTPrincipal(credential.payload)
//                else
//                    null
            }
        }
    }
}

@KtorDsl
suspend fun RoutingContext.withJwtPermission(requiredPermission: String, build: RoutingHandler) {
    val principal = call.principal<JWTPrincipal>()
    val claim = principal?.payload?.getClaim("permissions")
    val permissions = claim?.asList(String::class.java) ?: emptyList<String>()
    if (requiredPermission !in permissions) {
        call.respond(HttpStatusCode.Forbidden, "Forbidden: Missing permission '$requiredPermission'")
    } else {
        build()
    }
}
