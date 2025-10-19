package com.goquesty.presentation.verify_email

import androidx.lifecycle.ViewModel
import com.goquesty.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel()