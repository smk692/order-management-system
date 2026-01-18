package com.oms.core.domain

import jakarta.persistence.Embeddable

/**
 * LocalizedString Value Object
 * Represents multilingual text content
 */
@Embeddable
data class LocalizedString(
    val ko: String,
    val en: String? = null
) {
    init {
        require(ko.isNotBlank()) { "Korean text cannot be blank" }
    }

    fun get(language: Language): String {
        return when (language) {
            Language.KO -> ko
            Language.EN -> en ?: ko  // Fallback to Korean if English not available
        }
    }

    fun toMap(): Map<String, String> {
        val map = mutableMapOf("ko" to ko)
        en?.let { map["en"] = it }
        return map
    }

    companion object {
        fun korean(text: String): LocalizedString = LocalizedString(ko = text)

        fun bilingual(ko: String, en: String): LocalizedString = LocalizedString(ko = ko, en = en)

        fun fromMap(map: Map<String, String>): LocalizedString {
            val ko = map["ko"] ?: throw IllegalArgumentException("Korean text is required")
            val en = map["en"]
            return LocalizedString(ko = ko, en = en)
        }
    }
}

enum class Language {
    KO, EN
}
