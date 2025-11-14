package com.goquestly.presentation.profile

data class ProfileState(
    val name: String = "",
    val editableName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val hasChanges: Boolean
        get() = name != editableName.trim() && editableName.isNotBlank()
}