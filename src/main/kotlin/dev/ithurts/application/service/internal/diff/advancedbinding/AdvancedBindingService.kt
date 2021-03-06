package dev.ithurts.application.service.internal.diff.advancedbinding

import dev.ithurts.application.service.internal.sourceprovider.SourceProviderCommunicationService
import dev.ithurts.application.model.LineRange
import dev.ithurts.application.security.IntegrationAuthenticationFacade
import dev.ithurts.domain.Language
import dev.ithurts.domain.debt.AdvancedBinding
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.repository.RepositoryRepository
import dev.ithurts.application.exception.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AdvancedBindingService(
    private val languageSpecificBindingServices: Map<Language, LanguageSpecificBindingService>,
    private val sourceProviderCommunicationService: SourceProviderCommunicationService,
    private val authenticationFacade: IntegrationAuthenticationFacade,
    private val repositoryRepository: RepositoryRepository
) {
    fun lookupBindingLocation(debt: Debt, advancedBinding: AdvancedBinding, filePath: String, commitHash: String): LineRange? {
        val languageSpecificBindingService = languageSpecificBindingServices[advancedBinding.language]
        if (languageSpecificBindingService == null) {
            log.error("Didn't find appropriate handler for language ${advancedBinding.language}")
            throw Exception("Didn't find appropriate handler for language ${advancedBinding.language}")
        }
        val repo = repositoryRepository.findById(debt.repositoryId).orElseThrow{ EntityNotFoundException("Repository", "id", debt.repositoryId) }
        val file = sourceProviderCommunicationService.getFile(authenticationFacade.workspace.externalId,
            repo.name, filePath, commitHash)
        return languageSpecificBindingService.lookupBindingLocation(advancedBinding, file)
    }

    companion object {
        private val log = LoggerFactory.getLogger(AdvancedBindingService::class.java)
    }
}