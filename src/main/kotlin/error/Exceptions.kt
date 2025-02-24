package com.company.error

class AuthenticationException(override val message: String? = null) : Throwable()
class AuthorizationException(override val message: String? = null) : Throwable()
