package net.persgroep.maisonyvonne.presentation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.assertj.MockMvcTester

@WebMvcTest(HealthController::class)
class HealthControllerTest(@Autowired val mockMvc: MockMvcTester) {

    @Test
    fun `health returns ok`() {
        assertThat(mockMvc.get().uri("/api/health"))
            .hasStatusOk()
            .bodyJson().extractingPath("$.status").isEqualTo("ok")
    }
}
