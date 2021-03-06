package dev.ithurts.controller.api

import dev.ithurts.application.model.TechDebtReport
import dev.ithurts.application.model.debt.DebtDto
import dev.ithurts.application.query.DebtQueryRepository
import dev.ithurts.application.service.DebtService
import dev.ithurts.application.model.RepositoryInfo
import dev.ithurts.application.service.internal.RepositoryInfoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("api/debts")
class DebtController(
    private val debtService: DebtService,
    private val debtQueryRepository: DebtQueryRepository,
    private val repositoryInfoService: RepositoryInfoService,
) {
    @PostMapping
    fun reportDebt(@RequestBody techDebtReport: TechDebtReport): ResponseEntity<Any> {
        val repositoryInfo: RepositoryInfo = repositoryInfoService.parseRemoteUrl(techDebtReport.remoteUrl)
        val debtId = debtService.createDebt(techDebtReport, repositoryInfo)
        return ResponseEntity.created(URI.create("https://ithurts.dev/api/debt/$debtId"))
            .build()
    }
    @PutMapping("/{id}")
    fun updateDebt(@PathVariable id: String, @RequestBody debtDto: TechDebtReport): ResponseEntity<Any> {
        debtService.update(id, debtDto)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getDebts(@RequestParam remoteUrl: String): List<DebtDto> {
        val repositoryInfo = repositoryInfoService.parseRemoteUrl(remoteUrl)
        return debtQueryRepository.queryRepositoryActiveDebts(repositoryInfo)
    }

    @PostMapping("/{debtId}/vote")
    fun vote(@PathVariable debtId: String): ResponseEntity<Any> {
        debtService.vote(debtId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{debtId}/downVote")
    fun downVote(@PathVariable debtId: String): ResponseEntity<Any> {
        debtService.downVote(debtId)
        return ResponseEntity.ok().build()
    }

//    @ExceptionHandler(NoResultException::class)
//    fun handleNoResult(): ResponseEntity<ItHurtsError> {
//        return ResponseEntity(
//            ItHurtsError(
//                "not_accessible_entity", "We failed to find what you are looking for. " +
//                        "The entity either does not exist or you have lack of access"
//            ), HttpStatus.NOT_FOUND
//        )
//    }
}