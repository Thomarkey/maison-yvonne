package net.persgroep.maisonyvonne.presentation

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/** The app lives under /app in the same bundle; deep links must reach index.html. */
@Controller
class SpaController {

    @GetMapping("/app", "/app/**")
    fun app() = "forward:/index.html"
}
