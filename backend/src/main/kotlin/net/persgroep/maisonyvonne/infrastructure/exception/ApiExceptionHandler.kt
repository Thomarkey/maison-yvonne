package net.persgroep.maisonyvonne.infrastructure.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    /** Domain and service `require`s are input validation: answer 400, not 500. */
    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidRequest(e: IllegalArgumentException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message ?: "invalid request")
}
