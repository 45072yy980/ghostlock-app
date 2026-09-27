package com.ghostlock.app.domain.model

/**
 * Supported root managers. [packageName] is the value passed to native via the
 * GHOSTLOCK_MANAGER environment variable; empty means auto-detect.
 */
enum class RootManager(
    val packageName: String,
    val displayName: String,
) {
    Auto("", "Auto"),
    KernelSU("me.weishu.kernelsu", "KernelSU"),
    KernelSUNext("me.weishu.kernelsu.pr", "KernelSU Next"),
    DikSU("me.diksu.kernelsu", "DikSU"),
    ReSukiSU("com.resukisu.resukisu", "ReSukiSU"),
    KowSU("com.kowx712.supermanager", "KowSU"),
    ;

    companion object {
        /** All selectable entries shown in the UI (Auto first). */
        val selectable: List<RootManager> = entries.toList()

        fun fromPackage(packageName: String?): RootManager =
            entries.firstOrNull { it.packageName == packageName } ?: Auto
    }
}