package net.persgroep.maisonyvonne.presentation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.assertj.MockMvcTester

// security rules are covered end-to-end in PilotFlowTest; this one only checks the route
@WebMvcTest(HealthController::class)
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest(@Autowired val mockMvc: MockMvcTester) {

    @Test
    fun `health returns ok`() {
        assertThat(mockMvc.get().uri("/api/health"))
            .hasStatusOk()
            .bodyJson().extractingPath("$.status").isEqualTo("ok")
    }
}
