package dev.ithurts.service.core

import dev.ithurts.exception.EntityNotFoundException
import dev.ithurts.model.Account
import dev.ithurts.model.SourceProvider
import dev.ithurts.model.organisation.Organisation
import dev.ithurts.model.organisation.OrganisationMemberRole
import dev.ithurts.model.organisation.OrganisationMembership
import dev.ithurts.repository.AccountRepository
import dev.ithurts.repository.OrganisationMembershipRepository
import dev.ithurts.repository.OrganisationRepository
import dev.ithurts.sourceprovider.model.SourceProviderOrganisation
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class OrganisationService(
    private val organisationRepository: OrganisationRepository,
    private val organisationMembershipRepository: OrganisationMembershipRepository,
    private val accountRepository: AccountRepository,
) {

    @PreAuthorize("hasPermission(#organisationId, 'Organisation', 'MEMBER')")
    fun getById(organisationId: Long): Organisation {
        return organisationRepository.findByIdOrNull(organisationId)
            ?: throw EntityNotFoundException("organisation", "id", organisationId.toString())
    }

    @Transactional
    fun createOrganisationFromExternalOne(
        sourceProviderOrganisation: SourceProviderOrganisation,
        owner: Account,
        clientKey: String,
        secret: String
    ): Long {
        val existingOrganisation = organisationRepository.getBySourceProviderAndExternalId(
            sourceProviderOrganisation.sourceProvider,
            sourceProviderOrganisation.id
        )
        return if (existingOrganisation != null) {
            existingOrganisation.active = true
            existingOrganisation.clientKey = clientKey
            existingOrganisation.secret = secret
            organisationRepository.save(existingOrganisation).id!!
        } else {
            val organisation = organisationRepository.save(
                Organisation(
                    sourceProviderOrganisation.name,
                    sourceProviderOrganisation.sourceProvider,
                    sourceProviderOrganisation.id,
                    clientKey,
                    secret
                )
            )
            organisation.addMember(owner, OrganisationMemberRole.ADMIN)
            organisationRepository.save(organisation)
            return organisation.id!!
        }
    }

    fun deactivateOrganisation(sourceProvider: SourceProvider, externalId: String) {
        organisationRepository.getBySourceProviderAndExternalId(sourceProvider, externalId)?.let {
            it.active = false
            organisationRepository.save(it)
        }
    }

    fun getMembership(organisationId: Long, accountId: Long): OrganisationMembership {
        return organisationMembershipRepository.findByOrganisationIdAndAccountId(organisationId, accountId)
            ?: throw EntityNotFoundException("membership", "organisationId/accountId", "$organisationId/$accountId")
    }

    @PreAuthorize("hasPermission(#currentOrganisationId, 'Organisation', 'ADMIN')")
    fun addMemberByEmail(currentOrganisationId: Long, email: String) {
        val account = accountRepository.findByEmail(email) ?: throw EntityNotFoundException("acccount", "email", email)
        val organisation = organisationRepository.findByIdOrNull(currentOrganisationId)!!
        organisation.addMember(account)
        organisationRepository.save(organisation)
    }
}