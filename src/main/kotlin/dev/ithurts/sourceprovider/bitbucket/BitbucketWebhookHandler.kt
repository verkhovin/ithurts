package dev.ithurts.sourceprovider.bitbucket

import dev.ithurts.model.SourceProvider
import dev.ithurts.model.api.bitbucket.BitbucketAppInstallation
import dev.ithurts.repository.AccountRepository
import dev.ithurts.security.IntegrationAuthenticationFacade
import dev.ithurts.service.DiffHandlingService
import dev.ithurts.service.OrganisationService
import dev.ithurts.service.RepositoryService
import dev.ithurts.sourceprovider.SourceProviderCommunicationService
import dev.ithurts.sourceprovider.bitbucket.dto.webhook.BitbucketWebhookEvent
import dev.ithurts.sourceprovider.bitbucket.dto.webhook.ChangesPushed
import dev.ithurts.sourceprovider.bitbucket.dto.webhook.RepoUpdated
import dev.ithurts.sourceprovider.model.SourceProviderOrganisation
import org.springframework.stereotype.Service

@Service
class BitbucketWebhookHandler(
    private val accountRepository: AccountRepository,
    private val organisationService: OrganisationService,
    private val repositoryService: RepositoryService,
    private val authenticationFacade: IntegrationAuthenticationFacade,
    private val sourceProviderCommunicationService: SourceProviderCommunicationService,
    private val diffHandlingService: DiffHandlingService
) {
    fun appInstalled(bitbucketAppInstallation: BitbucketAppInstallation) {
        val actorAccount = accountRepository.findByExternalIdAndSourceProvider(
            "{${bitbucketAppInstallation.actor!!.uuid}}",
            SourceProvider.BITBUCKET
        ) ?: throw IllegalArgumentException("Actor is not It Hurts account")

        val organisation = bitbucketAppInstallation.principal
        organisationService.createOrganisationFromExternalOne(
            SourceProviderOrganisation(
                organisation.username,
                organisation.displayName,
                SourceProvider.BITBUCKET
            ),
            actorAccount,
            bitbucketAppInstallation.clientKey!!,
            bitbucketAppInstallation.sharedSecret!!
        )
    }

    fun appUninstalled(bitbucketAppInstallation: BitbucketAppInstallation) {
        organisationService.deactivateOrganisation(
            SourceProvider.BITBUCKET,
            bitbucketAppInstallation.principal.username
        )
    }

    fun repoUpdated(data: RepoUpdated) {
        val organisation = authenticationFacade.organisation
        repositoryService.changeName(organisation, data.changes.slug.old, data.changes.slug.new)
    }

    fun changesPushed(changesPushedEvent: ChangesPushed) {
        changesPushedEvent.push.changes.forEach { change ->
            val diffSpec = "${change.old.target.hash}..${change.new.target.hash}"
            val diff = sourceProviderCommunicationService.getDiff(
                changesPushedEvent.repository.workspace.slug,
                changesPushedEvent.repository.name,
                diffSpec
            )
            diffHandlingService.handleDiff(diff)
        }
    }
}