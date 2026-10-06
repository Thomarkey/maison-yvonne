package net.persgroep.maisonyvonne.presentation

import net.persgroep.maisonyvonne.application.exception.AlreadyExistsException
import net.persgroep.maisonyvonne.application.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionControllerAdvice {

    @ExceptionHandler(NotFoundException::class)
    fun handle(notFoundException: NotFoundException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundException.message ?: "Not found")
    }

    @ExceptionHandler(AlreadyExistsException::class)
    fun handle(alreadyExistsException: AlreadyExistsException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(alreadyExistsException.message ?: "Already exists")
    }

}