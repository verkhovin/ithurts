package dev.ithurts.controller.api.webhook.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class BitbucketAppInstallation(
    val productType: String,
    val principal: BitbucketAppInstallationPrincipal,
    val actor: BitbucketAppInstallationActor?,
    val clientKey: String?,
    val sharedSecret: String?
)

data class BitbucketAppInstallationPrincipal(
    val username: String, // workspace slug
    @JsonProperty("display_name") val displayName: String // workspace name
)

data class BitbucketAppInstallationActor(
    val uuid: String
)