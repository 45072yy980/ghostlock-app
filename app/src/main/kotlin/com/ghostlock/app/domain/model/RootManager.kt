package com.ghostlock.app.domain.model

/**
 * Supported root managers. [packageName] is the value passed to native via the
 * GHOSTLOCK_MANAGER environment variable; empty means auto-detect.
 *
 * [Custom] is a placeholder that lets the user type an arbitrary package name.
 * The actual chosen package (when Custom is selected) is stored separately and
 * supplied as the "current package" string.
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
    Custom("", "Custom…"),
    ;

    companion object {
        /** All selectable entries shown in the UI (Auto first, Custom last). */
        val selectable: List<RootManager> = entries.toList()

        /** Entries that map to a concrete built-in package. */
        val builtIn: List<RootManager> = entries.filter { it.packageName.isNotEmpty() }

        /** Find the enum entry that matches a concrete package name. */
        fun fromPackage(packageName: String?): RootManager =
            builtIn.firstOrNull { it.packageName == packageName } ?: Auto

        /** True when [packageName] is non-empty and not one of the built-ins. */
        fun isCustomPackage(packageName: String?): Boolean =
            !packageName.isNullOrBlank() && builtIn.none { it.packageName == packageName }

        private val PACKAGE_RE = Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+$")

        /** Basic Android package-name validation (at least two dot-separated segments). */
        fun isValidPackageName(packageName: String): Boolean = PACKAGE_RE.matches(packageName)
    }
}