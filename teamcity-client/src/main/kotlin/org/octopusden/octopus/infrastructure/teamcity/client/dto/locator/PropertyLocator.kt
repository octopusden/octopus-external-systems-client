package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

data class PropertyLocator(
    val name: String,
    val value: String,
    val matchType: MatchType? = null,
    val ignoreCase: Boolean? = null,
) : BaseLocator() {
    enum class MatchType(private val value: String) {
        EXISTS("exists"),
        NOT_EXISTS("not-exists"),
        EQUALS("equals"),
        DOES_NOT_EQUAL("does-not-equal"),
        STARTS_WITH("starts-with"),
        CONTAINS("contains"),
        DOES_NOT_CONTAIN("does-not-contain"),
        ENDS_WITH("ends-with"),
        ANY("any"),
        MATCHES("matches"),
        DOES_NOT_MATCH("does-not-match"),
        MORE_THAN("more-than"),
        NO_MORE_THAN("no-more-than"),
        LESS_THAN("less-than"),
        NO_LESS_THAN("no-less-than"),
        VER_MORE_THAN("ver-more-than"),
        VER_NO_MORE_THAN("ver-no-more-than"),
        VER_LESS_THAN("ver-less-than"),
        VER_NO_LESS_THAN("ver-no-less-than");

        override fun toString() = value
    }

    override fun toString() = super.toString()
}
