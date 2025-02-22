package com.company.error

class AuthorizationException(override val message: String? = null) : Throwable()
class AuthenticationException(override val message: String? = null) : Throwable()
