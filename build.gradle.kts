plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kmp).apply(false)
    alias(libs.plugins.android.kmp.library).apply(false)
}
