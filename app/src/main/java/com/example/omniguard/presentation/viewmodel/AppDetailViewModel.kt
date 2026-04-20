package com.example.omniguard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omniguard.data.repository.AppUsageRepository
import com.example.omniguard.domain.model.AppInfo
import com.example.omniguard.domain.usecase.GetAppDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val getAppDetailsUseCase: GetAppDetailsUseCase,
    private val appUsageRepository: AppUsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppDetailUiState())
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    fun loadAppDetails(packageName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val app = getAppDetailsUseCase(packageName)
            val usageTime = appUsageRepository.getAppUsageTime(packageName)

            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    appInfo = app,
                    usageTimeMillis = usageTime
                )
            }
        }
    }
}

data class AppDetailUiState(
    val isLoading: Boolean = false,
    val appInfo: AppInfo? = null,
    val usageTimeMillis: Long = 0
)
