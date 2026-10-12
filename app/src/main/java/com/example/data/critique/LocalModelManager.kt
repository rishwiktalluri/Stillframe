package com.example.data.critique

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

enum class ModelLifecycleStatus(val label: String, val description: String) {
    NOT_INSTALLED("Not Installed", "No on-device language model weights have been installed."),
    DOWNLOAD_AVAILABLE("Download Available", "Gemma 4 E4B-it package ready for user-initiated download."),
    DOWNLOADING("Downloading", "Transferring quantized model package to private storage..."),
    VERIFYING("Verifying", "Validating file signature and SHA256 integrity check..."),
    READY("Ready", "Model verified and stored on device. Ready for initialization."),
    LOADING("Loading", "Allocating model weights into GPU / NPU memory buffers..."),
    RUNNING("Running", "Performing local on-device inference..."),
    UNAVAILABLE_OR_UNSUPPORTED("Unavailable / Blocked", "Hardware or native runtime requirements not met."),
    ERROR("Error", "Initialization failed or corrupted model weights detected.")
}

data class ModelSpec(
    val name: String = "Gemma 4 E4B-it (LiteRT-LM)",
    val architecture: String = "4-bit Quantized INT4 Transformer",
    val estimatedDownloadBytes: Long = 2_791_728_742L, // ~2.6 GB
    val requiredFreeSpaceBytes: Long = 3_500_000_000L, // ~3.5 GB
    val targetRuntime: String = "LiteRT-LM Android Native (Arm64-v8a)",
    val contextWindowTokens: Int = 4096
)

data class ModelManagerState(
    val status: ModelLifecycleStatus = ModelLifecycleStatus.NOT_INSTALLED,
    val progressPercent: Int = 0,
    val statusMessage: String = "Target model: Gemma 4 E4B-it via LiteRT-LM",
    val localFilePath: String? = null,
    val freeStorageBytes: Long = 0L,
    val isHardwareCompatible: Boolean = true,
    val compatibilityDetails: String = "",
    val activeCritiqueFinding: CritiqueFinding? = null
)

class LocalModelManager(private val context: Context) {

    private val _state = MutableStateFlow(
        ModelManagerState(
            status = ModelLifecycleStatus.NOT_INSTALLED,
            statusMessage = "Target model: Gemma 4 E4B-it. LiteRT-LM native runtime.",
            freeStorageBytes = getAvailableInternalStorage(),
            isHardwareCompatible = checkHardwareCompatibility(),
            compatibilityDetails = getHardwareDetails()
        )
    )
    val state: StateFlow<ModelManagerState> = _state.asStateFlow()

    val modelSpec = ModelSpec()

    companion object {
        fun getAvailableInternalStorage(): Long {
            return try {
                val stat = StatFs(Environment.getDataDirectory().path)
                stat.availableBytes
            } catch (e: Exception) {
                0L
            }
        }

        private fun checkHardwareCompatibility(): Boolean {
            // Check if 64-bit architecture
            val is64Bit = Build.SUPPORTED_ABIS.any { it.contains("64") }
            return is64Bit
        }

        private fun getHardwareDetails(): String {
            val abis = Build.SUPPORTED_ABIS.joinToString(", ")
            val is64Bit = Build.SUPPORTED_ABIS.any { it.contains("64") }
            return "Architecture: $abis | 64-Bit Support: ${if (is64Bit) "Supported" else "Unsupported"} | Android API: ${Build.VERSION.SDK_INT}"
        }
    }

    fun inspectCustomModelPath(path: String) {
        val file = File(path)
        if (!file.exists()) {
            _state.value = _state.value.copy(
                status = ModelLifecycleStatus.NOT_INSTALLED,
                statusMessage = "Specified model file does not exist at path: $path",
                localFilePath = null
            )
            return
        }
        if (file.length() < 100_000_000L) { // Less than 100MB is not a valid LLM weight
            _state.value = _state.value.copy(
                status = ModelLifecycleStatus.ERROR,
                statusMessage = "File found but size (${file.length() / (1024 * 1024)} MB) is too small for Gemma 4 E4B-it weights.",
                localFilePath = path
            )
            return
        }
        _state.value = _state.value.copy(
            status = ModelLifecycleStatus.READY,
            statusMessage = "Local model binary located (${file.length() / (1024 * 1024)} MB).",
            localFilePath = path
        )
    }

    fun promptDownloadNotice() {
        val freeBytes = getAvailableInternalStorage()
        if (freeBytes < modelSpec.requiredFreeSpaceBytes) {
            _state.value = _state.value.copy(
                status = ModelLifecycleStatus.UNAVAILABLE_OR_UNSUPPORTED,
                statusMessage = "Insufficient storage: Free ${freeBytes / (1024 * 1024)} MB, required ${modelSpec.requiredFreeSpaceBytes / (1024 * 1024)} MB."
            )
        } else {
            _state.value = _state.value.copy(
                status = ModelLifecycleStatus.DOWNLOAD_AVAILABLE,
                statusMessage = "Gemma 4 E4B-it package (~2.6 GB) ready. Awaiting explicit user confirmation."
            )
        }
    }

    fun reportContainerLimitation() {
        _state.value = _state.value.copy(
            status = ModelLifecycleStatus.UNAVAILABLE_OR_UNSUPPORTED,
            statusMessage = "LiteRT-LM native binaries (arm64-v8a C++ runtime) cannot run in this cloud build container. Real on-device execution requires physical Android device with LiteRT native runtime installed. Rule-based editorial critic remains 100% active."
        )
    }

    fun cancelOperation() {
        _state.value = _state.value.copy(
            status = ModelLifecycleStatus.NOT_INSTALLED,
            progressPercent = 0,
            statusMessage = "Operation cancelled by author."
        )
    }
}
