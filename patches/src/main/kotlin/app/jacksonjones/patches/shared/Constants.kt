package app.jacksonjones.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_QQ_MUSIC = Compatibility(
        name = "QQ音乐",
        packageName = "com.tencent.qqmusic",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x31C27C,
        targets = listOf(
            // Translations are matched by the Chinese source text and not by resource name,
            // so other app versions are expected to work but will have less coverage.
            AppTarget(
                version = null,
                isExperimental = true
            ),
            AppTarget(
                version = "20.9.0.8"
            )
        )
    )
}
