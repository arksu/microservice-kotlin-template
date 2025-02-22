package com.company.config

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*

typealias Permission = String

class AuthConfig {
    var permissions: Set<String> = emptySet()
    var type: AuthType = AuthType.ALL
}

val RoleAuthorization = createRouteScopedPlugin(
    name = "AuthorizationPlugin",
    createConfiguration = ::AuthConfig
) {
    val requiredPermissions = pluginConfig.permissions
    val type = pluginConfig.type

    on(AuthenticationChecked) { call ->
        val principal = call.principal<JWTPrincipal>() ?: throw AuthenticationException()

        val claim = principal.payload.getClaim("permissions")
        val permissions = (claim?.asList(String::class.java) ?: emptyList<String>()).toSet()
        val denyReasons = mutableListOf<String>()

        when (type) {
            AuthType.ALL -> {
                val missing = requiredPermissions - permissions
                if (missing.isNotEmpty()) {
                    denyReasons += "Principal lacks required permission(s) ${missing.joinToString(" and ")}"
                }
            }

            AuthType.ANY -> {
                if (permissions.none { it in requiredPermissions }) {
                    denyReasons += "Principal has none of the sufficient permission(s) ${
                        requiredPermissions.joinToString(
                            " or "
                        )
                    }"

                }
            }

            AuthType.NONE -> {
                if (permissions.any { it in requiredPermissions }) {
                    denyReasons += "Principal has forbidden permission(s) ${
                        (requiredPermissions.intersect(permissions)).joinToString(
                            " and "
                        )
                    }"

                }
            }
        }

        if (denyReasons.isNotEmpty()) {
            val message = denyReasons.joinToString(". ")
            throw AuthorizationException(message)
        }
    }

}

// Applies logical AND between roles
fun Route.withAllRoles(vararg permissions: Permission, build: Route.() -> Unit) =
    authorizedRoute(
        requiredPermissions = permissions.toSet(),
        authType = AuthType.ALL,
        build = build,
    )

// Applies logical OR between roles
fun Route.withAnyRole(vararg permissions: Permission, build: Route.() -> Unit) =
    authorizedRoute(
        requiredPermissions = permissions.toSet(),
        authType = AuthType.ANY,
        build = build,
    )

// Applies logical NOT for provided roles
fun Route.withoutRoles(vararg permissions: Permission, build: Route.() -> Unit) =
    authorizedRoute(
        requiredPermissions = permissions.toSet(),
        authType = AuthType.NONE,
        build = build,
    )

private fun Route.authorizedRoute(requiredPermissions: Set<String>, authType: AuthType, build: Route.() -> Unit): Route {
    val authorizedRoute = createChild(AuthorizedRouteSelector(requiredPermissions.joinToString(",")))
    authorizedRoute.install(RoleAuthorization) {
        permissions = requiredPermissions
        type = authType
    }
    authorizedRoute.build()
    return authorizedRoute
}


class AuthorizedRouteSelector(private val description: String) : RouteSelector() {
    override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant

    override fun toString(): String = "(authorize ${description})"
}

class AuthorizationException(override val message: String? = null) : Throwable()

class AuthenticationException(override val message: String? = null) : Throwable()

enum class AuthType {
    ALL,
    ANY,
    NONE,
}