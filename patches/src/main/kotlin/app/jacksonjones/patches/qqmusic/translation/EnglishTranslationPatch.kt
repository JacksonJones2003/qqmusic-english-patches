package app.jacksonjones.patches.qqmusic.translation

import app.jacksonjones.patches.shared.Constants.COMPATIBILITY_QQ_MUSIC
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import java.io.File
import java.util.logging.Logger

private const val DICTIONARY_RESOURCE = "/qqmusic/translations/zh-en.tsv"

private val CJK_REGEX = Regex("[\\u4e00-\\u9fff]")

// <string name="abc">text</string> and <item>text</item> elements that have no child elements.
// Groups: 1 = opening tag, 2 = tag name, 3 = text, 4 = closing tag.
private val VALUE_ELEMENT_REGEX = Regex("(<(string|item)\\b[^>]*>)([^<]*)(</\\2>)")

// Text hardcoded in layouts, menus and preference screens. Custom view attributes have
// obfuscated names, so every attribute of the app namespace is included.
// Groups: 1 = attribute name and opening quote, 2 = namespace, 3 = text, 4 = closing quote.
private val TEXT_ATTRIBUTE_REGEX =
    Regex("(\\b(?:(android):(?:text|hint|title|summary|label|contentDescription)|(?:app):[\\w.]+)=\")([^\"]*)(\")")

private val VALUES_FILE_NAMES = setOf("strings.xml", "arrays.xml", "plurals.xml")
private val ATTRIBUTE_DIRECTORY_PREFIXES = listOf("layout", "menu", "xml", "navigation")

// Text starting with these characters is escaped with a backslash by the resource decoder,
// otherwise it is compiled as a reference instead of text.
private const val ESCAPED_START_CHARACTERS = "@?#"

/**
 * How the text of a decoded resource file is escaped.
 */
private enum class TextType {
    /** Text of a string element in strings.xml, which the patcher escapes while patches execute. */
    ESCAPED_STRING,

    /** Text of other elements, which is not escaped. */
    ELEMENT,

    /** Value of an XML attribute. */
    ATTRIBUTE
}

@Suppress("unused")
val englishTranslationPatch = resourcePatch(
    name = "English translation",
    description = "Translates the Chinese interface text of the app to English.",
    default = true
) {
    compatibleWith(COMPATIBILITY_QQ_MUSIC)

    execute {
        val logger = Logger.getLogger("EnglishTranslationPatch")
        val dictionary = loadDictionary()

        val resDirectory = get("res")
        val resourceDirectories = resDirectory.listFiles { file -> file.isDirectory }
            ?: throw PatchException("Could not list resources in: $resDirectory")

        var translated = 0
        var untranslated = 0

        fun translate(rawText: String, type: TextType): String? {
            if (!CJK_REGEX.containsMatchIn(rawText)) return null

            var text = rawText.unescapeXml()
            if (type == TextType.ESCAPED_STRING) text = text.unescapeString()
            if (text.length >= 2 && text[0] == '\\' && text[1] in ESCAPED_START_CHARACTERS) {
                text = text.substring(1)
            }

            val english = dictionary[text.trim()]
            if (english == null) {
                untranslated++
                return null
            }
            translated++

            // Keep the original padding, since the app may rely on it for layout.
            var result = text.takeWhile { it.isWhitespace() } + english + text.takeLastWhile { it.isWhitespace() }

            if (result[0] in ESCAPED_START_CHARACTERS || result[0] == '\\') result = "\\" + result
            if (type == TextType.ESCAPED_STRING) result = result.escapeString()

            return result.escapeXml(isAttribute = type == TextType.ATTRIBUTE)
        }

        fun File.translateAll(regex: Regex, type: (MatchResult) -> TextType) {
            val original = readText()
            if (!CJK_REGEX.containsMatchIn(original)) return

            val patched = regex.replace(original) { match ->
                val english = translate(match.groupValues[3], type(match))
                    ?: return@replace match.value

                match.groupValues[1] + english + match.groupValues[4]
            }

            if (patched != original) writeText(patched)
        }

        resourceDirectories.forEach { directory ->
            val name = directory.name
            when {
                name == "values" || name.startsWith("values-") ->
                    directory.listFiles { file -> file.name in VALUES_FILE_NAMES }?.forEach { file ->
                        val isStringsFile = file.name == "strings.xml"

                        file.translateAll(VALUE_ELEMENT_REGEX) { match ->
                            if (isStringsFile && match.groupValues[2] == "string") {
                                TextType.ESCAPED_STRING
                            } else {
                                TextType.ELEMENT
                            }
                        }
                    }

                ATTRIBUTE_DIRECTORY_PREFIXES.any { name == it || name.startsWith("$it-") } ->
                    directory.listFiles { file -> file.extension == "xml" }?.forEach { file ->
                        file.translateAll(TEXT_ATTRIBUTE_REGEX) { TextType.ATTRIBUTE }
                    }
            }
        }

        if (translated == 0) {
            throw PatchException("No text was translated. This app version is not supported.")
        }

        logger.info("Translated $translated texts to English ($untranslated are not translated yet)")
    }
}

/**
 * Loads the translations, keyed by the Chinese source text.
 *
 * Text is matched by its content and not by resource name, because the resource names
 * of the app are obfuscated and change with every app release.
 */
private fun loadDictionary(): Map<String, String> {
    val stream = object {}.javaClass.getResourceAsStream(DICTIONARY_RESOURCE)
        ?: throw PatchException("Translation dictionary not found: $DICTIONARY_RESOURCE")

    fun String.decode() = replace("\\n", "\n").replace("\\t", "\t")

    return stream.bufferedReader(Charsets.UTF_8).useLines { lines ->
        lines.filter { it.isNotEmpty() && !it.startsWith("# ") }
            .mapNotNull { line ->
                val separator = line.indexOf('\t')
                if (separator <= 0) return@mapNotNull null

                line.substring(0, separator).decode() to line.substring(separator + 1).decode()
            }.toMap()
    }
}

private fun String.unescapeXml() = replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&apos;", "'")
    .replace("&#10;", "\n")
    .replace("&amp;", "&")

private fun String.escapeXml(isAttribute: Boolean): String {
    val escaped = replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    return if (isAttribute) {
        escaped.replace("\"", "&quot;").replace("\n", "&#10;")
    } else {
        escaped
    }
}

private fun String.unescapeString(): String {
    if (indexOf('\\') < 0) return this

    val builder = StringBuilder(length)
    var index = 0
    while (index < length) {
        val char = this[index]
        if (char == '\\' && index + 1 < length) {
            when (val next = this[index + 1]) {
                'n' -> builder.append('\n')
                't' -> builder.append('\t')
                'r' -> builder.append('\r')
                'u' -> {
                    val code = if (index + 6 <= length) substring(index + 2, index + 6).toIntOrNull(16) else null
                    if (code != null) {
                        builder.append(code.toChar())
                        index += 4
                    } else {
                        builder.append("\\u")
                    }
                }
                else -> builder.append(next)
            }
            index += 2
        } else {
            builder.append(char)
            index++
        }
    }

    return builder.toString()
}

private fun String.escapeString() = replace("\\", "\\\\")
    .replace("'", "\\'")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\t", "\\t")
    .replace("\r", "\\r")
