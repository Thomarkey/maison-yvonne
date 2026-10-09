package net.persgroep.maisonyvonne

import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import net.persgroep.maisonyvonne.application.domain.AccountDocument
import net.persgroep.maisonyvonne.application.domain.FamilyLinkDocument
import net.persgroep.maisonyvonne.application.domain.SeniorDocument
import net.persgroep.maisonyvonne.application.domain.Account
import net.persgroep.maisonyvonne.application.domain.Senior
import net.persgroep.maisonyvonne.application.domain.UserRole
import org.springframework.http.MediaType
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * The pilot's core flow: Yasmina registers and publishes a visit, the linked daughter
 * logs in and sees it — and nobody else does.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PilotFlowTest(
    @Autowired val mockMvc: MockMvcTester,
    @Autowired val mongo: MongoTemplate,
    @Autowired val passwords: PasswordEncoder,
) {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val mongodb = MongoDBContainer("mongo:8")
    }

    private val password = "test-${UUID.randomUUID()}"

    @Test
    fun `admin publishes a visit and only the linked family member sees it`() {
        val admin = account(role = UserRole.ADMIN, displayName = "Yasmina")
        val louisa = senior(preferredName = "Louisa-${UUID.randomUUID()}")
        val otherSenior = senior(preferredName = "Marc-${UUID.randomUUID()}")
        val daughter = account(
            role = UserRole.FAMILY,
            displayName = "Sophie",
            links = listOf(FamilyLinkDocument(louisa.id.toString(), "dochter")),
        )
        val stranger = account(
            role = UserRole.FAMILY,
            displayName = "Iemand anders",
            links = listOf(FamilyLinkDocument(otherSenior.id.toString(), "zoon")),
        )

        val visitedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val nextVisitAt = visitedAt.plus(7, ChronoUnit.DAYS)
        val adminSession = login(admin.email)

        assertThat(
            mockMvc.post().uri("/api/admin/visits")
                .session(adminSession)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "seniorId": "${louisa.id}",
                      "scheduledAt": "$visitedAt",
                      "completed": true,
                      "check": "ATTENTION",
                      "familyMessage": "Louisa was goedgemutst.",
                      "momentOfWeek": "Ze zong mee met de radio.",
                      "internalNote": "Loopt wat moeizamer dan vorige week.",
                      "meals": [
                        {"slot": "FIRST", "dishName": "Kabeljauw met prei en puree", "storageInfo": "Vandaag of morgen"},
                        {"slot": "SECOND", "dishName": "Kip met appelmoes", "storageInfo": "Binnen 3 dagen"},
                        {"slot": "LAST", "dishName": "Stoofvlees met worteltjes", "storageInfo": "Houdt 5 dagen"}
                      ],
                      "nextVisitAt": "$nextVisitAt"
                    }
                    """.trimIndent(),
                ),
        ).hasStatusOk()

        val dashboard = mockMvc.get().uri("/api/family/dashboard").session(login(daughter.email))
        assertThat(dashboard).hasStatusOk().bodyJson().apply {
            extractingPath("$.length()").isEqualTo(1)
            extractingPath("$[0].senior.id").isEqualTo(louisa.id.toString())
            extractingPath("$[0].relationLabel").isEqualTo("dochter")
            extractingPath("$[0].lastVisit.familyMessage").isEqualTo("Louisa was goedgemutst.")
            extractingPath("$[0].lastVisit.check").isEqualTo("ATTENTION")
            extractingPath("$[0].lastVisit.meals[0].dishName").isEqualTo("Kabeljauw met prei en puree")
            extractingPath("$[0].lastVisit.meals[2].label").isEqualTo("Bewaar mij voor het laatst")
        }
        // the internal note must not reach the family payload in any shape
        assertThat(dashboard.exchange().response.contentAsString)
            .doesNotContain("moeizamer")
            .doesNotContain("internalNote")

        // a family member linked to another senior sees nothing of Louisa
        assertThat(mockMvc.get().uri("/api/family/dashboard").session(login(stranger.email)))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$[0].senior.id")
            .isEqualTo(otherSenior.id.toString())

        // the admin overview lists the visit, internal note included
        assertThat(mockMvc.get().uri("/api/admin/visits").session(adminSession))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$[?(@.seniorId == '${louisa.id}')].internalNote")
            .asArray()
            .containsExactly("Loopt wat moeizamer dan vorige week.")
        assertThat(mockMvc.get().uri("/api/admin/visits").session(login(daughter.email)))
            .hasStatus(HttpStatus.FORBIDDEN)

        // "mijn bezoeken" only holds what the logged-in admin registered
        assertThat(mockMvc.get().uri("/api/admin/visits/mine").session(adminSession))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$[*].seniorId")
            .asArray()
            .containsExactly(louisa.id.toString())
        val otherAdmin = account(role = UserRole.ADMIN, displayName = "Louis")
        assertThat(mockMvc.get().uri("/api/admin/visits/mine").session(login(otherAdmin.email)))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.length()")
            .isEqualTo(0)
    }

    @Test
    fun `family may not register visits and anonymous callers get nothing`() {
        val daughter = account(role = UserRole.FAMILY, displayName = "Sophie")

        assertThat(
            mockMvc.post().uri("/api/admin/visits")
                .session(login(daughter.email))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"),
        ).hasStatus(HttpStatus.FORBIDDEN)

        assertThat(mockMvc.get().uri("/api/family/dashboard")).hasStatus(HttpStatus.UNAUTHORIZED)
        assertThat(mockMvc.get().uri("/api/health")).hasStatusOk()
    }

    @Test
    fun `an admin edits only their own visits and manages seniors`() {
        val yasmina = account(role = UserRole.ADMIN, displayName = "Yasmina")
        val louis = account(role = UserRole.ADMIN, displayName = "Louis")
        val yasminaSession = login(yasmina.email)
        val louisSession = login(louis.email)

        val created = mockMvc.post().uri("/api/admin/seniors")
            .session(yasminaSession)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"preferredName": "Jos", "fullName": "Jos Peeters"}""")
            .exchange()
        assertThat(created).hasStatusOk()
        val seniorId = UUID.fromString(JsonPath.read(created.response.contentAsString, "$.id"))

        assertThat(
            mockMvc.put().uri("/api/admin/seniors/$seniorId")
                .session(louisSession)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"preferredName": "Jozef", "fullName": "Jozef Peeters"}"""),
        ).hasStatusOk().bodyJson().extractingPath("$.preferredName").isEqualTo("Jozef")

        // no preferred name: the first name from the full name stands in
        assertThat(
            mockMvc.post().uri("/api/admin/seniors")
                .session(yasminaSession)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"preferredName": "", "fullName": "Maria Janssens"}"""),
        ).hasStatusOk().bodyJson().extractingPath("$.preferredName").isEqualTo("Maria")

        val daughter = account(
            role = UserRole.FAMILY,
            displayName = "An",
            links = listOf(FamilyLinkDocument(seniorId.toString(), "dochter")),
        )
        val visitedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        fun visitBody(familyMessage: String) = """
            {
              "seniorId": "$seniorId",
              "scheduledAt": "$visitedAt",
              "completed": false,
              "check": "ALL_AS_USUAL",
              "familyMessage": "$familyMessage",
              "meals": [],
              "nextVisitAt": null
            }
        """.trimIndent()

        val registered = mockMvc.post().uri("/api/admin/visits")
            .session(yasminaSession)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(visitBody("Jos was niet thuis."))
            .exchange()
        assertThat(registered).hasStatusOk()
        val visitId = JsonPath.read<String>(registered.response.contentAsString, "$.id")

        // another admin may not touch it
        assertThat(
            mockMvc.put().uri("/api/admin/visits/$visitId")
                .session(louisSession)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(visitBody("Overschreven door Louis.")),
        ).hasStatus(HttpStatus.FORBIDDEN)

        assertThat(
            mockMvc.put().uri("/api/admin/visits/$visitId")
                .session(yasminaSession)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(visitBody("Jos was bij de buren.")),
        ).hasStatusOk()

        assertThat(mockMvc.get().uri("/api/family/dashboard").session(login(daughter.email)))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$[0].lastVisit.familyMessage")
            .isEqualTo("Jos was bij de buren.")
        assertThat(mockMvc.get().uri("/api/admin/visits/mine").session(yasminaSession))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$[*].id")
            .asArray()
            .containsExactly(visitId)

        // the seniors overview shows who did the visit, also to other admins
        assertThat(mockMvc.get().uri("/api/admin/visits").session(louisSession))
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$[?(@.id == '$visitId')].registeredBy")
            .asArray()
            .containsExactly("Yasmina")
    }

    private fun login(email: String): MockHttpSession {
        val result = mockMvc.post().uri("/api/auth/login")
            .param("username", email)
            .param("password", password)
            .with(csrf())
            .exchange()
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT)
        return result.request.session as MockHttpSession
    }

    private fun account(role: UserRole, displayName: String, links: List<FamilyLinkDocument> = emptyList()): Account {
        val account = Account(
            id = UUID.randomUUID(),
            email = "${UUID.randomUUID()}@example.com",
            passwordHash = checkNotNull(passwords.encode(password)),
            role = role,
            displayName = displayName,
        )
        mongo.insert(AccountDocument.from(account, links))
        return account
    }

    private fun senior(preferredName: String): Senior {
        val senior = Senior(
            id = UUID.randomUUID(),
            preferredName = preferredName,
            fullName = "$preferredName Vermeulen",
        )
        mongo.insert(SeniorDocument.from(senior))
        return senior
    }
}
