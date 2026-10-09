package net.persgroep.maisonyvonne.infrastructure.exception

import net.persgroep.maisonyvonne.application.domain.NotYourVisitException
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

    @ExceptionHandler(NotYourVisitException::class)
    fun forbidden(e: NotYourVisitException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.message ?: "forbidden")

    /** An edit aimed at a visit or senior that does not exist. */
    @ExceptionHandler(NoSuchElementException::class)
    fun notFound(e: NoSuchElementException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message ?: "not found")
}
