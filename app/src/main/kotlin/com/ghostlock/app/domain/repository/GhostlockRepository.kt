package com.ghostlock.app.domain.repository

import com.ghostlock.app.domain.model.CpuPair
import com.ghostlock.app.domain.model.KernelSnapshot
import com.ghostlock.app.domain.model.OffsetCandidate
import com.ghostlock.app.domain.model.OffsetImportResult
import com.ghostlock.app.domain.model.ParseResult

interface GhostlockRepository {
    suspend fun snapshot(): KernelSnapshot

    fun selectCpuPair(index: Int)

    fun setSafeModeEnabled(enabled: Boolean)

    fun setTcpRouteEnabled(enabled: Boolean)

    fun setManagerPackage(packageName: String)

    /** Package names of root managers currently installed on the device. */
    fun installedManagers(): List<String>

    /** Whether the device already has a working root (module loaded / su reachable). */
    fun isDeviceRooted(): Boolean

    suspend fun exportCandidates(): List<OffsetCandidate>

    suspend fun importOffsets(json: String): OffsetImportResult

    suspend fun confirmImport(json: String): OffsetImportResult

    suspend fun parseSource(
        input: String,
        xblPath: String? = null,
        overwrite: Boolean = false,
        onLog: (String) -> Unit = {},
    ): ParseResult

    suspend fun readDocument(uri: String): String

    suspend fun cacheDocument(uri: String, fileName: String): String

    suspend fun publishOffsets(candidate: OffsetCandidate): String

    suspend fun runExploit(pair: CpuPair, targetManager: String? = null, onLog: (String) -> Unit): Int

    /**
     * Resolve the package of the manager to open after a successful activation.
     * Returns null when no manager with a launcher activity is found.
     */
    fun resolveActiveManagerPackage(): String?

    fun close()
}
