package net.persgroep.maisonyvonne.presentation.api

import net.persgroep.maisonyvonne.application.domain.Meal

data class MealDTO(
    val name: String,
    val forDayNumber: Int
) {
    companion object {
        fun from(meal: Meal) = MealDTO(
            name = meal.name,
            forDayNumber = meal.forDayNumber
        )
    }
}