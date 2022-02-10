package dev.ithurts.application.dto.debt

import dev.ithurts.domain.debt.Debt
import java.time.LocalDateTime
import java.time.ZoneOffset

data class DebtDetailsDto(
    val debt: DebtDto,
    val events: List<DebtEventDto>
) {
    companion object {
        fun from(
            debt: Debt,
            debtCost: Int,
            bindings: List<BindingDto>,
            repository: DebtRepositoryDto,
            reporter: DebtAccountDto,
            currentUserVoted: Boolean,
            events: List<DebtEventDto>
        ): DebtDetailsDto {
            return DebtDetailsDto(
                DebtDto.from(debt, bindings, repository, reporter, currentUserVoted, debtCost),
                events
            )
        }
    }
}
