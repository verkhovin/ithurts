package dev.ithurts.application.security

import dev.ithurts.application.security.oauth2.AuthenticatedOAuth2User
import dev.ithurts.application.service.RepositoryInfo
import dev.ithurts.domain.account.Account
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.domain.workspace.WorkspaceMember
import dev.ithurts.domain.workspace.WorkspaceMemberRole
import dev.ithurts.domain.workspace.WorkspaceMemberRole.*
import dev.ithurts.domain.workspace.WorkspaceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class OrganisationPermissionEvaluator(
    private val workspaceRepository: WorkspaceRepository,
    private val permissionQueryRepository: PermissionQueryRepository
) : PermissionEvaluator {
    override fun hasPermission(authentication: Authentication?, targetDomainObject: Any?, permission: Any?): Boolean {
        throw NotImplementedError()
    }

    override fun hasPermission(
        authentication: Authentication?,
        target: Serializable,
        targetType: String,
        permission: Any
    ): Boolean {
        authentication ?: return false
        val requiredRole = valueOf(permission as String)
        val accountId = when (val principal = authentication.principal) {
            is AuthenticatedOAuth2User -> principal.accountId
            is Account -> principal.identity
            is Workspace -> return target == principal.identity
            else -> return false
        }
         return when (targetType) {
            "Workspace" -> evaluatePermissionToWorkspace(target, accountId, requiredRole)
            "Repository" -> evaluatePermissionToRepository(target, accountId, requiredRole)
                else -> false
        }
    }

    private fun evaluatePermissionToRepository(
        target: Serializable,
        accountId: Long,
        requiredRole: WorkspaceMemberRole
    ): Boolean {
        when (target) {
            is Long -> {
                val member = permissionQueryRepository.getWorkspaceMemberByRepositoryId(target, accountId) ?: return false
                return checkMemberPermission(member, requiredRole)
            }
            is RepositoryInfo -> {
                val memberRole = permissionQueryRepository.getWorkspaceMemberByRepositoryInfo(target, accountId) ?: return false
                return checkMemberPermission(memberRole, requiredRole)
            }
            else -> return false
        }
    }

    private fun checkMemberPermission(
        member: WorkspaceMember,
        requiredRole: WorkspaceMemberRole
    ) = member.role == requiredRole || (member.role == ADMIN && requiredRole == MEMBER)

    private fun evaluatePermissionToWorkspace(
        targetId: Serializable,
        accountId: Long,
        requiredRole: WorkspaceMemberRole
    ): Boolean {
        val workspace = workspaceRepository.findByIdOrNull(targetId as Long) ?: return false
        return workspace.checkAccountHasPermission(accountId, requiredRole)
    }
}