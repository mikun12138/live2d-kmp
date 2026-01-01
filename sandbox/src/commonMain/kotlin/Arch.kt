import java.util.Locale

enum class Arch(
    val value: String,
) {
    X86_64("x86_64"),
    ARM64("arm64")
    ;

    companion object {
        fun byName(name: String): Arch {
            return when (name) {
                "x86_64", "amd64" -> X86_64
                "aarch64", "arm64" -> ARM64
                else -> error("Unknown arch: [$name]")
            }
        }
    }
}

@Suppress("ConstantLocale")
val arch = Arch.byName(
    System.getProperty("os.arch").lowercase(Locale.getDefault())
)