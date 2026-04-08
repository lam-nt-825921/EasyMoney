package com.example.easymoney.ui.loan.information.ekyc

import com.example.easymoney.domain.model.FaceDetectionResult

sealed class EkycUiEvent {
    data object OnScreenEnter : EkycUiEvent()
    data object OnScreenExit : EkycUiEvent()
    
    data object OnRequestPermission : EkycUiEvent()
    data class OnPermissionResult(val granted: Boolean) : EkycUiEvent()
    data object OnOpenSettings : EkycUiEvent()
    
    data object OnStartCamera : EkycUiEvent()
    data object OnStopCamera : EkycUiEvent()
    data object OnCaptureClick : EkycUiEvent()
    data object OnRetakeClick : EkycUiEvent()
    data class OnFrameAnalyzed(val result: FaceDetectionResult) : EkycUiEvent()
    
    data object OnConfirmCapture : EkycUiEvent()
    data object OnRetryUpload : EkycUiEvent()
    
    data object OnBackClick : EkycUiEvent()
    data object OnExitFlow : EkycUiEvent()
}

