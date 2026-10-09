package net.persgroep.maisonyvonne.application.command

import java.util.UUID

data class CreateSeniorCommand(val preferredName: String?, val fullName: String)

data class UpdateSeniorCommand(val seniorId: UUID, val preferredName: String?, val fullName: String)
