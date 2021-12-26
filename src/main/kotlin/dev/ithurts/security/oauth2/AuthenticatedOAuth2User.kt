package dev.ithurts.security.oauth2

import dev.ithurts.model.Account
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.core.user.OAuth2User


class AuthenticatedOAuth2User(val account: Account, private val delegate: OAuth2User): OAuth2User {
    val accountId = account.id!!

    override fun getName() = delegate.name

    override fun getAttributes() = delegate.attributes

    override fun getAuthorities() = delegate.authorities
}