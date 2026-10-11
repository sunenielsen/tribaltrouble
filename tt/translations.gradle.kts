// Translation sync between the game's .properties bundles and a Google Sheet (via CSV).
//
//   exportTranslations [-PsheetCsv=<downloaded sheet .csv>]
//       Collects every string from src/main/resources and writes build/translations.csv
//       for import into Google Sheets (File > Import > Replace spreadsheet).
//       When -PsheetCsv is given, translations are merged cell by cell: a value present on
//       only one side is kept, and the baseline (translations-baseline.csv) decides which
//       side edited when both have values. Only when both sides changed since the last sync
//       does the sheet win, with a CONFLICT printed. Keys and English always come from code.
//
//   importTranslations -PsheetCsv=<downloaded sheet .csv>
//       Rewrites the locale .properties files from the sheet. Files whose content is
//       unchanged are left untouched. English base files are never modified.

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import org.apache.commons.csv.CSVFormat

buildscript {
    repositories { mavenCentral() }
    dependencies { classpath("org.apache.commons:commons-csv:1.14.0") }
}

val locales = listOf("da", "de", "es", "it", "pt", "pl")
val localeNames = listOf("Danish", "German", "Spanish", "Italian", "Portuguese", "Polish")
val csvHeader = listOf("File", "Key", "English") + localeNames
val resourceRoot = file("src/main/resources")
val bundleRoot = resourceRoot.resolve("com/oddlabs/tt")

// bundle short name (e.g. "form/OptionsMenu") -> base English file
fun findBundles(): Map<String, File> =
    bundleRoot.walkTopDown()
        .filter { it.isFile && it.extension == "properties" }
        .filter { f -> locales.none { f.name.endsWith("_$it.properties") } }
        .associateBy { it.relativeTo(bundleRoot).path.replace('\\', '/').removeSuffix(".properties") }
        .toSortedMap()

fun localeFile(baseFile: File, locale: String): File =
    baseFile.resolveSibling(baseFile.name.removeSuffix(".properties") + "_$locale.properties")

// Properties files are mostly latin-1 with \uXXXX escapes, but a few are UTF-8.
fun readPropertiesText(f: File): String {
    val bytes = f.readBytes()
    return try {
        StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes)).toString()
    } catch (e: CharacterCodingException) {
        String(bytes, Charsets.ISO_8859_1)
    }
}

// java.util.Properties does the spec-compliant parsing; the put override preserves file order.
fun parseProperties(f: File): LinkedHashMap<String, String> {
    val map = LinkedHashMap<String, String>()
    object : java.util.Properties() {
        override fun put(key: Any, value: Any): Any? {
            map[key as String] = value as String
            return super.put(key, value)
        }
    }.load(java.io.StringReader(readPropertiesText(f)))
    return map
}

fun escaped(s: String, isKey: Boolean): String {
    val sb = StringBuilder()
    s.forEachIndexed { i, c ->
        when {
            c == '\\' -> sb.append("\\\\")
            c == '\n' -> sb.append("\\n")
            c == '\t' -> sb.append("\\t")
            c == '\r' -> sb.append("\\r")
            c == '\u000C' -> sb.append("\\f")
            c == ' ' && (isKey || i == 0) -> sb.append("\\ ")
            (c == '=' || c == ':' || c == '#' || c == '!') && isKey -> sb.append('\\').append(c)
            c < ' ' || c > '~' -> sb.append("\\u%04X".format(c.code))
            else -> sb.append(c)
        }
    }
    return sb.toString()
}

fun propertiesText(entries: Map<String, String>): String =
    entries.entries.joinToString("") { (k, v) -> escaped(k, true) + "=" + escaped(v, false) + "\n" }

fun csvField(s: String): String =
    if (s.any { it == '"' || it == ',' || it == '\n' || it == '\r' }) {
        "\"" + s.replace("\"", "\"\"") + "\""
    } else s

fun parseCsv(rawText: String): List<List<String>> =
    CSVFormat.RFC4180.parse(java.io.StringReader(rawText.removePrefix("\uFEFF")))
        .map { record -> record.toList() }

// Sheet contents: `values` holds the known locales in `locales` order; `extras` holds
// columns for languages not (yet) registered in `localeNames`, passed through the sync
// verbatim so translator-added columns survive until the language is wired into the game.
class SheetData(
    val values: LinkedHashMap<Pair<String, String>, List<String>>,
    val extraHeaders: List<String>,
    val extras: Map<Pair<String, String>, List<String>>,
)

// Columns are matched by header name so a newly added language (missing from the sheet)
// reads as untranslated instead of failing.
fun readSheetCsv(path: String): SheetData {
    val csvFile = File(path).takeIf { it.isAbsolute } ?: rootDir.resolve(path)
    val rows = parseCsv(csvFile.readText(Charsets.UTF_8))
    val header = rows.firstOrNull() ?: emptyList()
    require(header.take(3) == listOf("File", "Key", "English")) {
        "Unexpected header in $path.\nExpected to start with: File,Key,English\nFound: $header"
    }
    val extraHeaders = header.drop(3).filter { it !in localeNames }
    extraHeaders.forEach {
        logger.warn("Sheet column \"$it\" is not a registered language; passing it through unsynced")
    }
    val columns = localeNames.map { header.indexOf(it) }
    columns.forEachIndexed { i, c ->
        if (c < 0) logger.warn("Sheet has no ${localeNames[i]} column yet; treating it as untranslated")
    }
    val extraColumns = extraHeaders.map { header.indexOf(it) }
    val values = LinkedHashMap<Pair<String, String>, List<String>>()
    val extras = LinkedHashMap<Pair<String, String>, List<String>>()
    rows.drop(1).filter { it.size >= 2 && it[0].isNotEmpty() }.forEach { r ->
        values[r[0] to r[1]] = columns.map { c -> if (c >= 0) r.getOrElse(c) { "" } else "" }
        extras[r[0] to r[1]] = extraColumns.map { c -> r.getOrElse(c) { "" } }
    }
    return SheetData(values, extraHeaders, extras)
}

// Snapshot of the last synced state, written by importTranslations and committed with it.
// Lets the merge tell which side actually changed a translation instead of guessing.
val baselineFile = file("translations-baseline.csv")

tasks.register("exportTranslations") {
    group = "translations"
    description = "Write build/translations.csv from the game strings, merging -PsheetCsv if given"
    val sheetCsv = providers.gradleProperty("sheetCsv").orNull
    val output = layout.buildDirectory.file("translations.csv")
    doLast {
        val sheetData = sheetCsv?.let { readSheetCsv(it) }
        val sheet = sheetData?.values ?: linkedMapOf()
        val base = baselineFile.takeIf { it.isFile }?.let { readSheetCsv(it.absolutePath).values }
        val extraHeaders = sheetData?.extraHeaders ?: emptyList()
        val seen = mutableSetOf<Pair<String, String>>()
        val conflicts = mutableListOf<String>()
        var newKeys = 0
        val lines = mutableListOf((csvHeader + extraHeaders).joinToString(",") { csvField(it) })
        findBundles().forEach { (bundle, baseFile) ->
            val english = parseProperties(baseFile)
            val localeEntries = locales.map { loc ->
                localeFile(baseFile, loc).takeIf { it.isFile }?.let { parseProperties(it) } ?: emptyMap()
            }
            english.forEach { (key, en) ->
                val sheetRow = sheet[bundle to key]
                if (sheetRow == null && sheet.isNotEmpty()) newKeys++
                seen.add(bundle to key)
                val merged = locales.indices.map { i ->
                    val code = localeEntries[i][key] ?: ""
                    val fromSheet = sheetRow?.get(i) ?: ""
                    val baseVal = base?.get(bundle to key)?.get(i)
                    when {
                        fromSheet.isEmpty() -> code
                        code.isEmpty() || code == fromSheet -> fromSheet
                        baseVal == code -> fromSheet // only the sheet changed since last sync
                        baseVal == fromSheet -> code // only the code changed since last sync
                        else -> {
                            conflicts.add("CONFLICT: $bundle/$key [${locales[i]}]\n    code:  $code\n    sheet: $fromSheet")
                            fromSheet
                        }
                    }
                }
                val extra = sheetData?.extras?.get(bundle to key) ?: extraHeaders.map { "" }
                lines.add((listOf(bundle, key, en) + merged + extra).joinToString(",") { csvField(it) })
            }
        }
        val out = output.get().asFile
        out.parentFile.mkdirs()
        out.writeText("\uFEFF" + lines.joinToString("\r\n") + "\r\n", Charsets.UTF_8)
        // column-name -> Google Translate code mapping, sent along by the sync workflow so
        // the sheet's Apps Script needs no edit when a language is registered here
        out.resolveSibling("sheet-langs.txt")
            .writeText(locales.indices.joinToString(",") { "${localeNames[it]}:${locales[it]}" })
        val dropped = sheet.keys.filter { it !in seen }
        if (dropped.isNotEmpty()) {
            logger.warn("Dropped ${dropped.size} sheet row(s) whose key no longer exists in the code:")
            dropped.forEach { logger.warn("  ${it.first}/${it.second}") }
        }
        if (conflicts.isNotEmpty()) {
            logger.warn("${conflicts.size} conflict(s), sheet value kept. Rerun import/export after resolving in the sheet:")
            conflicts.forEach { logger.warn(it) }
        }
        if (sheet.isNotEmpty()) println("$newKeys new key(s) not yet in the sheet")
        println("Wrote ${lines.size - 1} strings to $out")
        println("Import it in Google Sheets via File > Import > Replace spreadsheet.")
    }
}

tasks.register("importTranslations") {
    group = "translations"
    description = "Rewrite locale .properties files from a downloaded sheet CSV (-PsheetCsv=...)"
    val sheetCsv = providers.gradleProperty("sheetCsv").orNull
    doLast {
        requireNotNull(sheetCsv) { "Pass the downloaded sheet: -PsheetCsv=path/to/sheet.csv" }
        val sheet = readSheetCsv(sheetCsv).values
        val bundles = findBundles()
        sheet.keys.filter { it.first !in bundles }.forEach {
            logger.warn("Sheet row for unknown bundle, ignored: ${it.first}/${it.second}")
        }
        var written = 0
        val baselineLines = mutableListOf(csvHeader.joinToString(",") { csvField(it) })
        bundles.forEach { (bundle, baseFile) ->
            val english = parseProperties(baseFile)
            sheet.keys.filter { it.first == bundle && it.second !in english }.forEach {
                logger.warn("Sheet row for unknown key, ignored: $bundle/${it.second}")
            }
            english.forEach { (key, en) ->
                val row = listOf(bundle, key, en) + (sheet[bundle to key] ?: locales.map { "" })
                baselineLines.add(row.joinToString(",") { csvField(it) })
            }
            locales.forEachIndexed { i, loc ->
                val desired = LinkedHashMap<String, String>()
                english.keys.forEach { key ->
                    sheet[bundle to key]?.get(i)?.takeIf { it.isNotEmpty() }?.let { desired[key] = it }
                }
                val target = localeFile(baseFile, loc)
                // empty values are equivalent to falling back to the base bundle, so they
                // are not exported to the sheet and must not count as a difference here
                val current = target.takeIf { it.isFile }
                    ?.let { parseProperties(it).filterValues { v -> v.isNotEmpty() } }
                when {
                    desired.isEmpty() && current.isNullOrEmpty() -> {}
                    desired.isEmpty() -> logger.warn("Sheet has no ${localeNames[i]} strings for $bundle, keeping $target")
                    desired == current -> {}
                    else -> {
                        target.writeText(propertiesText(desired), Charsets.ISO_8859_1)
                        written++
                        println("Wrote ${target.relativeTo(projectDir)}")
                    }
                }
            }
        }
        baselineFile.writeText("\uFEFF" + baselineLines.joinToString("\r\n") + "\r\n", Charsets.UTF_8)
        println(if (written == 0) "All locale files already up to date" else "$written locale file(s) updated")
    }
}
