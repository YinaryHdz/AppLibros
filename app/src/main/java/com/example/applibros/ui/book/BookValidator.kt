package com.example.applibros.ui.book


object BookValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validate(
        title: String,
        description: String,
        genre: String,
        tags: List<String> = emptyList()
    ): ValidationResult {
        if (title.isBlank()) {
            return ValidationResult(false, "El título no puede estar vacío")
        }
        if (description.isBlank()) {
            return ValidationResult(false, "La descripción no puede estar vacía")
        }
        if (genre.isBlank()) {
            return ValidationResult(false, "Selecciona un género")
        }
        if (tags.isEmpty()) return ValidationResult(false, "Agrega al menos una etiqueta")

        return ValidationResult(true)
    }
}
