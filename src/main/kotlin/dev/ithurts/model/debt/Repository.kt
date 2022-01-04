package dev.ithurts.model.debt

import dev.ithurts.model.organisation.Organisation
import javax.persistence.*

@Entity
class Repository(
    var name: String,
    var mainBranch: String,
    @ManyToOne
    @JoinColumn(name = "organisation_id")
    val organisation: Organisation,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)