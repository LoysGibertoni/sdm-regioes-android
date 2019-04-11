package br.edu.ifsp.scl.tradutorsdmkt.model

data class Language(var id: String? = "", var language: String? = "") {
    override fun toString(): String {
        return language ?: ""
    }
}