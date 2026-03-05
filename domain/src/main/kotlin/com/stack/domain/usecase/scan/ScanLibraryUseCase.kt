package com.stack.domain.usecase.scan

import com.stack.domain.repository.ScanService
import javax.inject.Inject

class ScanLibraryUseCase @Inject constructor(
    private val scanService: ScanService
) {
    suspend operator fun invoke() {
        scanService.fullScan()
    }
}
