package net.persgroep.maisonyvonne.application.domain

import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document
data class User(
    val id: UUID,
    val name: String,
    val email: String,
    val userRole: UserRole
)