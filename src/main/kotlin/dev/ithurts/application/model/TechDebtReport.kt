package dev.ithurts.application.model

import dev.ithurts.domain.Language
import dev.ithurts.domain.debt.BindingStatus
import org.bson.types.ObjectId
import dev.ithurts.domain.debt.AdvancedBinding as DomainAdvancedBinding
import dev.ithurts.domain.debt.Binding as DomainBinding


class TechDebtReport(
    val title: String,
    val description: String,
    val remoteUrl: String,
    val bindings: List<Binding>
)


class Binding(
    val id: String?,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val advancedBinding: AdvancedBinding?,
    val status: BindingStatus
) {
    fun toDomain(): DomainBinding {
        return DomainBinding(
            filePath,
            startLine,
            endLine,
            advancedBinding?.toDomain(),
            status,
            id ?: ObjectId().toString()
        )
    }
}


class AdvancedBinding(
    val language: Language,
    val type: String,
    val name: String,
    val params: List<String>,
    val parent: String?
) {
    fun toDomain(): DomainAdvancedBinding {
        return DomainAdvancedBinding(
            language,
            type,
            name,
            params,
            parent
        )
    }
}