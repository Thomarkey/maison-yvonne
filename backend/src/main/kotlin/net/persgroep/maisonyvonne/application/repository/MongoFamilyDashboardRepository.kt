package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.AccountDocument
import net.persgroep.maisonyvonne.application.domain.SeniorDocument
import net.persgroep.maisonyvonne.application.domain.VisitDocument
import net.persgroep.maisonyvonne.application.domain.SeniorUpdate
import net.persgroep.maisonyvonne.application.domain.Visit
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class MongoFamilyDashboardRepository(private val mongo: MongoTemplate) : FamilyDashboardRepository {

    override fun dashboardFor(userId: UUID): List<SeniorUpdate> {
        val links = mongo.findById(userId.toString(), AccountDocument::class.java)?.links.orEmpty()
        if (links.isEmpty()) return emptyList()

        val seniorIds = links.map { it.seniorId }.toSet()
        val seniors = mongo.find(query(where("_id").`in`(seniorIds)), SeniorDocument::class.java)
            .associateBy { it.id }
        val latestVisits = latestPublishedVisits(seniorIds)

        return links.mapNotNull { link ->
            seniors[link.seniorId]?.let { senior ->
                SeniorUpdate(
                    senior = senior.toDomain(),
                    relationLabel = link.relationLabel,
                    latestVisit = latestVisits[link.seniorId],
                )
            }
        }.sortedBy { it.senior.preferredName }
    }

    /**
     * Only the seniors from the caller's own links, and only published visits: a family
     * member cannot reach an unlinked senior or a draft, whatever the request asks for.
     */
    private fun latestPublishedVisits(seniorIds: Set<String>): Map<String, Visit> =
        mongo.find(
            Query(where("seniorId").`in`(seniorIds).and("publishedAt").ne(null))
                .with(Sort.by(Sort.Direction.DESC, "scheduledAt")),
            VisitDocument::class.java,
        )
            .groupBy { it.seniorId }
            .mapValues { (_, visits) -> visits.first().toFamilyVisit() }
}
