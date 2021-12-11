package dev.ithurts.model.organisation

import dev.ithurts.model.Account
import dev.ithurts.model.SourceProvider
import dev.ithurts.model.organisation.OrganisationMemberRole.*
import dev.ithurts.model.organisation.OrganisationMemebershipStatus.*
import javax.persistence.*

@Entity
@Table(indexes = [
    Index(name = "unique_externalId_sourceProvider", columnList = "externalId, sourceProvider", unique = true),
    Index(name = "clientKey", columnList = "clientKey")
])
class Organisation(
    val name: String,
    @Enumerated(EnumType.STRING)
    val sourceProvider: SourceProvider,
    val externalId: String,
    var clientKey: String,
    var secret: String,
    var active: Boolean = true,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
    @OneToMany(mappedBy = "organisation", cascade = [CascadeType.ALL], orphanRemoval = true)
    val members: MutableList<OrganisationMembership> = mutableListOf()

    fun addMember(
        account: Account,
        role: OrganisationMemberRole = MEMBER,
        status: OrganisationMemebershipStatus = ACTIVE
    ) {
        members.add(OrganisationMembership(account, this, role, status))
    }
}