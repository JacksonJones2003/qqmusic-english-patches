# QQ Music English Patches

Unofficial [Morphe](https://morphe.software) patches that translate the Chinese interface of
QQ Music (QQ音乐, `com.tencent.qqmusic`) for Android to English.

## ❓ About

QQ Music for Android ships with a Chinese interface only. These patches replace the Chinese text
that is built into the app with English text when you patch the app with Morphe.

- Text is matched by its Chinese content and not by resource name, so the patch is not tied to a
  single app version. Developed and tested against QQ Music **20.9.0.8**.
- This repository contains patch code and translations only. It does not contain the app.
  You must provide your own copy of the app to patch.

### What is and is not translated

| Text | Translated |
| --- | --- |
| Menus, buttons, tabs, settings and dialogs built into the app | Yes, about 73% of all built-in text in 20.9.0.8. Short interface text is covered first. |
| Long explanations, legal text and rarely seen messages | Partly |
| Content sent by Tencent servers (home feed, charts, banners, playlist and song names) | No |
| Pages rendered as web pages inside the app | No |
| Text hardcoded in the app code instead of its resources | No |

### Known limitations

- A patched app is signed with a different key than the original. Sign in with QQ or WeChat
  may be refused by the patched app, and the original app must be uninstalled before the patched
  app can be installed.
- Using a modified app may be against the terms of service of the app. Use at your own risk.

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=JacksonJones2003/qqmusic-english-patches

Or download the `.mpp` file from the [latest release](https://github.com/JacksonJones2003/qqmusic-english-patches/releases/latest)
and use it with [Morphe Manager](https://github.com/MorpheApp/morphe-manager) or
[Morphe Desktop](https://github.com/MorpheApp/morphe-desktop):

```
java -jar morphe-desktop-*-all.jar patch -p patches-*.mpp QQMusic.apk
```

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.1](https://github.com/JacksonJones2003/qqmusic-english-patches/releases/tag/v1.0.0-dev.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
<details open>
<summary>📦 QQ音乐&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 20.9.0.8 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [English translation](#english-translation) | Translates the Chinese interface text of the app to English. |  |

</details>

<!-- PATCHES_END -->

## 🌐 Improving the translations

All translations are in one file:
[zh-en.tsv](patches/src/main/resources/qqmusic/translations/zh-en.tsv).
Each line is the Chinese source text, a tab, and the English text. Line breaks are written as `\n`.
Format placeholders such as `%s` and `%1$d` must be kept in the English text.
Text that is not in the file is left in Chinese.

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## ⚖️ Disclaimer

This project is not affiliated with, endorsed by or sponsored by Tencent or the Morphe open source
project. QQ Music is a trademark of its owner. These patches are built with the
[Morphe patches template](https://github.com/MorpheApp/morphe-patches-template) for use with Morphe.

## 📜 License

QQ Music English Patches are licensed under the [GNU General Public License v3.0](LICENSE)
