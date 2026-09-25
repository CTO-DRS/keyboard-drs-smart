/*
 * Copyright (C) 2022-2025 The DRS Smart Keyboard Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.drs.smartkeyboard.ime.nlp.latin

import android.content.Context
import android.os.SystemClock
import com.drs.smartkeyboard.app.DrsPreferenceStore
import com.drs.smartkeyboard.appContext
import com.drs.smartkeyboard.ime.core.Subtype
import com.drs.smartkeyboard.ime.dictionary.DictionaryManager
import com.drs.smartkeyboard.ime.dictionary.UserDictionaryEntry
import com.drs.smartkeyboard.ime.editor.EditorContent
import com.drs.smartkeyboard.ime.nlp.AutocorrectDecider
import com.drs.smartkeyboard.ime.nlp.SpellingProvider
import com.drs.smartkeyboard.ime.nlp.SpellingResult
import com.drs.smartkeyboard.ime.nlp.SuggestionCandidate
import com.drs.smartkeyboard.ime.nlp.SuggestionProvider
import com.drs.smartkeyboard.ime.nlp.WordSuggestionCandidate
import com.drs.smartkeyboard.lib.devtools.flogDebug
import com.drs.smartkeyboard.lib.devtools.flogError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.drs.lib.android.readText
import org.drs.lib.kotlin.guardedByLock

private const val CORRECTION_MIN_LENGTH = 3
private const val CORRECTION_MAX_LENGTH = 12
private const val CORRECTION_INDEX_LIMIT = 12000

// DRS personal word learning constants
private const val USER_WORDS_TTL_MS = 15_000L
private const val USER_WORDS_MAX = 4000
private const val LEARNED_INSERT_FREQ_FROM_SUGGESTION = 160
private const val LEARNED_INSERT_FREQ_TYPED = 120

/** All single-character deletion variants of [word] (deduplicated). */
private fun delete1Variants(word: String): Set<String> {
    if (word.length < 2) return emptySet()
    val out = HashSet<String>(word.length * 2)
    val sb = StringBuilder(word)
    for (i in word.indices) {
        val ch = sb[i]
        sb.deleteCharAt(i)
        out.add(sb.toString())
        sb.insert(i, ch)
    }
    return out
}

/** Returns true when [a] and [b] differ by at most a single edit. */
private fun isEditDistanceAtMostOne(a: String, b: String): Boolean {
    val la = a.length
    val lb = b.length
    if (kotlin.math.abs(la - lb) > 1) return false
    var i = 0
    var j = 0
    var edits = 0
    while (i < la && j < lb) {
        if (a[i] == b[j]) {
            i++; j++
        } else {
            if (++edits > 1) return false
            if (la > lb) i++ else if (lb > la) j++ else { i++; j++ }
        }
    }
    return true
}

/**
 * DRS language provider delivering real dictionary-based word suggestions, next-word
 * prediction and spelling support.
 *
 * Dictionaries are language-aware assets stored under `ime/dict/`:
 *  - `ar.json`   : 50k-word Arabic frequency dictionary (with smart normalization)
 *  - `fr/de/es/it/pt/tr/ru/fa.json` : DRS v1.18.0 curated high-frequency
 *    dictionaries per language (rank-based score curve) — Latin-script
 *    languages and Persian no longer fall back to ENGLISH suggestions
 *  - `data.json` : generic Latin dictionary (English, also the honest
 *    fallback for languages without a bundled dictionary)
 *  - `ar_bigrams.json` / `en_bigrams.json` : next-word prediction tables built from
 *    real corpus co-occurrence counts (OpenSubtitles). Head words are stored in their
 *    normalized form; the suggested next words keep their original (correct) spelling.
 *
 * Arabic matching is performed on a normalized form (hamza variants unified, ta-marbuta,
 * alif maqsura, diacritics and tatweel stripped), so typing "مدرسه" still surfaces the
 * correct spelling "مدرسة" as a tap-to-commit suggestion. DRS v1.18.0: Latin input is
 * matched through the same pipeline with accent folding, so "eleve" surfaces "élève".
 *
 * When the composing region is empty (right after a space/commit), the provider predicts
 * the NEXT word from the preceding word using the bigram table.
 */
class LatinLanguageProvider(context: Context) : SpellingProvider, SuggestionProvider {
    companion object {
        // Default user ID used for all subtypes, unless otherwise specified.
        // See `ime/core/Subtype.kt` Line 210 and 211 for the default usage
        const val ProviderId = "org.drs.nlp.providers.latin"

        private const val MAX_WORD_LENGTH = 32

        private const val ARABIC_LANGUAGE = "ar"

        /** Prefix of the Arabic unicode block used for script detection. */
        private const val ARABIC_SCRIPT_START = '\u0600'
        private const val ARABIC_SCRIPT_END = '\u06FF'
    }

    private val appContext by context.appContext()
    private val prefs by DrsPreferenceStore

    /**
     * One dictionary entry, pre-normalized at load time for fast prefix lookups.
     * [norm] is the normalized form of [word] used for matching only; suggestions
     * are always returned in their original (correct) spelling.
     */
    private class DictEntry(val norm: String, val word: String, val freq: Int)

    /** A language dictionary: entries sorted lexicographically by [DictEntry.norm]. */
    private class DictIndex(val entries: List<DictEntry>) {
        val words: Map<String, Int> by lazy { entries.associate { it.word to it.freq } }

        /**
         * Delete-1 neighborhood index for "did you mean?" corrections, built lazily
         * on first use and bounded to the [CORRECTION_INDEX_LIMIT] most frequent words
         * to keep memory usage predictable.
         */
        val correctionIndex: Map<String, List<Int>> by lazy {
            val cutoff = entries.asSequence()
                .map { it.freq }
                .sortedDescending()
                .drop(CORRECTION_INDEX_LIMIT - 1)
                .firstOrNull() ?: 0
            val map = HashMap<String, MutableList<Int>>()
            fun addKey(key: String, index: Int) {
                map.getOrPut(key) { ArrayList(2) }.add(index)
            }
            for (i in entries.indices) {
                val entry = entries[i]
                if (entry.freq < cutoff || entry.norm.length !in CORRECTION_MIN_LENGTH..CORRECTION_MAX_LENGTH) continue
                addKey(entry.norm, i) // catches user typed an extra char (delete1(s) == w)
                for (t in delete1Variants(entry.norm)) {
                    addKey(t, i) // catches substitutions and user dropped a char
                }
            }
            map
        }

        /**
         * Collects up to [limit] dictionary entries within edit distance 1 of [norm]
         * (single substitution, insertion or deletion), ranked by frequency.
         */
        fun corrections(norm: String, limit: Int): List<DictEntry> {
            if (norm.length < CORRECTION_MIN_LENGTH) return emptyList()
            val candidateIdx = HashSet<Int>()
            correctionIndex[norm]?.let { candidateIdx.addAll(it) }
            for (t in delete1Variants(norm)) {
                correctionIndex[t]?.let { candidateIdx.addAll(it) }
            }
            return candidateIdx.asSequence()
                .map { entries[it] }
                .filter { it.norm != norm && isEditDistanceAtMostOne(it.norm, norm) }
                .sortedByDescending { it.freq }
                .take(limit)
                .toList()
        }

        /**
         * Collects up to [limit] entries whose normalized form starts with [prefix],
         * using a binary search over the sorted entries (O(log n + k)).
         */
        fun findByPrefix(prefix: String, limit: Int): List<DictEntry> {
            if (prefix.isEmpty()) return emptyList()
            var lo = 0
            var hi = entries.size
            while (lo < hi) {
                val mid = (lo + hi) ushr 1
                if (entries[mid].norm < prefix) lo = mid + 1 else hi = mid
            }
            val out = ArrayList<DictEntry>(limit)
            for (i in lo until entries.size) {
                val entry = entries[i]
                if (!entry.norm.startsWith(prefix)) break
                out.add(entry)
                if (out.size >= limit) break
            }
            return out
        }
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val wordDataSerializer = MapSerializer(String.serializer(), Int.serializer())
    private val bigramDataSerializer =
        MapSerializer(String.serializer(), MapSerializer(String.serializer(), Int.serializer()))

    /** Language code -> loaded dictionary index. */
    private val dictCache = guardedByLock { mutableMapOf<String, DictIndex>() }

    /** Language code -> next-word table (normalized head -> next words sorted by score desc). */
    private val bigramCache = guardedByLock { mutableMapOf<String, Map<String, List<Pair<String, Int>>>>() }

    /** DRS: cached personal user dictionary entries with a short TTL. */
    private class UserDataCacheState {
        var loadedAtElapsed: Long = Long.MIN_VALUE
        var entries: List<UserDictionaryEntry> = emptyList()
    }

    private val userDataCache = guardedByLock { UserDataCacheState() }

    /** DRS: privacy flag of the most recent suggest() call, used to gate learning. */
    @Volatile
    private var lastSuggestWasPrivate = false

    override val providerId = ProviderId

    override suspend fun create() {
        // One-time provider initialization; dictionaries are loaded per language in preload().
    }

    override suspend fun preload(subtype: Subtype) = withContext(Dispatchers.IO) {
        // Preload the dictionary for the active primary language (and secondaries lazily on demand).
        val languages = listOf(subtype.primaryLocale.language) +
            subtype.secondaryLocales.map { it.language }
        val lang = languages.firstOrNull() ?: return@withContext
        if (dictCache.withLock { it.containsKey(lang) }) return@withContext
        runCatching { loadDict(lang) }
            .onSuccess { index -> dictCache.withLock { it[lang] = index } }
            .onFailure { e -> flogError { "Failed to load dictionary for '$lang': ${e}" } }
    }

    private suspend fun loadDict(lang: String): DictIndex {
        val asset = dictAssetFor(lang)
        val rawData = appContext.assets.readText(asset)
        val data = json.decodeFromString(wordDataSerializer, rawData)
        val entries = data
            .map { (word, freq) -> DictEntry(normalize(word), word, freq) }
            .sortedBy { it.norm }
        return DictIndex(entries)
    }

    // DRS v1.18.0: bundled per-language dictionaries live in the pure
    // LatinWordNormalize object (tested): every Latin-script language below
    // gets its own frequency dictionary instead of silently receiving
    // ENGLISH suggestions (the old generic data.json fallback); unknown
    // languages still degrade to the English fallback honestly.
    private fun dictAssetFor(lang: String): String = LatinWordNormalize.dictAssetFor(lang)

    private fun bigramAssetFor(lang: String): String? = when (lang) {
        ARABIC_LANGUAGE -> "ime/dict/ar_bigrams.json"
        "en" -> "ime/dict/en_bigrams.json"
        else -> null
    }

    /**
     * Loads the next-word table for the given language lazily. A missing or invalid asset
     * degrades gracefully to an empty table (feature silently off, never throws).
     */
    private suspend fun bigramsFor(subtype: Subtype): Map<String, List<Pair<String, Int>>> {
        val languages = listOf(subtype.primaryLocale.language) +
            subtype.secondaryLocales.map { it.language }
        val lang = languages.firstOrNull() ?: return emptyMap()
        bigramCache.withLock { it[lang] }?.let { return it }
        // DRS v1.18.0: bigram tables exist for Arabic and English only —
        // the other bundled languages degrade to an empty next-word table
        // (prefix suggestions still work) instead of a dishonest lookup in
        // the English table that can never match.
        val asset = bigramAssetFor(lang) ?: return emptyMap()
        val table = runCatching {
            val rawData = appContext.assets.readText(asset)
            val data = json.decodeFromString(bigramDataSerializer, rawData)
            data.mapValues { (_, nexts) ->
                nexts.entries.sortedByDescending { it.value }.map { it.key to it.value }
            }
        }.getOrElse { e ->
            flogError { "Failed to load bigrams for '$lang': ${e}" }
            emptyMap()
        }
        bigramCache.withLock { it[lang] = table }
        return table
    }

    private suspend fun dictFor(subtype: Subtype): DictIndex? {
        val languages = listOf(subtype.primaryLocale.language) +
            subtype.secondaryLocales.map { it.language }
        for (lang in languages) {
            dictCache.withLock { it[lang] }?.let { return it }
        }
        val lang = languages.firstOrNull() ?: return null
        return runCatching { loadDict(lang) }
            .getOrNull()
            ?.also { index -> dictCache.withLock { it[lang] = index } }
    }

    override suspend fun spell(
        subtype: Subtype,
        word: String,
        precedingWords: List<String>,
        followingWords: List<String>,
        maxSuggestionCount: Int,
        allowPossiblyOffensive: Boolean,
        isPrivateSession: Boolean,
    ): SpellingResult {
        // DRS v1.19.0: REAL typo marking, governed by the pure SpellingDecider.
        // Until v1.18.0 this function unconditionally returned validWord(), so the
        // spell-checker service was a dead channel: fully wired sessions, ~180
        // advertised subtypes, and never a single red underline. The gates are
        // deliberately conservative — only rich dictionaries (ar/en, >=10k entries)
        // judge, plain single-script words only, all-caps acronyms and mid-sentence
        // capitalized proper nouns excluded, and a word is only flagged when a
        // plausible edit-distance-1 correction actually exists. Users can switch
        // the whole channel off via prefs.spelling.typoFlaggingEnabled.
        if (!prefs.spelling.typoFlaggingEnabled.get()) return SpellingResult.validWord()
        val raw = word.trim()
        if (!SpellingDecider.isSpellableWord(raw, midSentence = precedingWords.isNotEmpty())) {
            return SpellingResult.validWord()
        }
        val dict = dictFor(subtype) ?: return SpellingResult.validWord()
        val norm = normalize(raw)
        val exactInDict = dict.words.containsKey(raw)
        val normInDict = norm.isNotEmpty() && norm != raw &&
            dict.findByPrefix(norm, limit = 1).firstOrNull()?.norm == norm
        if (exactInDict || normInDict) return SpellingResult.validWord()
        val isArabic = raw.any { it in ARABIC_SCRIPT_START..ARABIC_SCRIPT_END }
        val inUserWords = userDataFor(subtype).any { entry ->
            entry.word == raw ||
                (isArabic && normalize(entry.word) == norm) ||
                (!isArabic && entry.word.lowercase() == raw.lowercase())
        }
        if (inUserWords) return SpellingResult.validWord()
        val corrections = dict.corrections(norm, maxSuggestionCount.coerceIn(1, 5))
        if (!SpellingDecider.shouldFlagTypo(
                wordInDict = false,
                wordInUserWords = false,
                dictEntries = dict.entries.size,
                correctionCount = corrections.size,
            )
        ) {
            return SpellingResult.validWord()
        }
        return SpellingResult.typo(corrections.map { it.word }.toTypedArray())
    }

    override suspend fun suggest(
        subtype: Subtype,
        content: EditorContent,
        maxCandidateCount: Int,
        allowPossiblyOffensive: Boolean,
        isPrivateSession: Boolean,
    ): List<SuggestionCandidate> {
        val raw = content.composingText.toString().trim()
        lastSuggestWasPrivate = isPrivateSession
        if (raw.isEmpty()) {
            // Nothing is being composed (right after a space, punctuation or commit):
            // predict the next word from the word before the cursor. Honors the
            // dedicated next-word toggle so users can keep prefix-only suggestions.
            if (!prefs.suggestion.nextWordEnabled.get()) return emptyList()
            return nextWordCandidates(subtype, content, maxCandidateCount)
        }
        if (raw.length > MAX_WORD_LENGTH) return emptyList()
        if (!raw[0].isLetter() || raw.any { it.isDigit() }) return emptyList()

        val index = dictFor(subtype) ?: return emptyList()
        val isArabic = raw.any { it in ARABIC_SCRIPT_START..ARABIC_SCRIPT_END }
        // DRS v1.18.0: normalize the typed prefix through the SAME pipeline
        // the dictionary was pre-normalized with (Arabic unification + Latin
        // accent folding), so accent-less input matches accented words.
        val prefix = normalize(raw)
        if (prefix.isEmpty()) return emptyList()

        fun candidates(list: List<DictEntry>) = list.map { entry ->
            WordSuggestionCandidate(
                text = displayCase(entry.word, raw, isArabic),
                confidence = entry.freq / 255.0,
                sourceProvider = this,
            )
        }

        // Personally learned words matching the typed prefix always come first.
        val userMatches = userWordMatchesFor(subtype, prefix, isArabic, raw, maxCandidateCount)

        // Gather a wider slice next so we can rank the best matches by frequency.
        val dictMatches = index.findByPrefix(prefix, limit = maxCandidateCount * 6)
            .filter { it.word != raw }
            .sortedByDescending { it.freq }

        val merged = buildList {
            addAll(userMatches.map { (word, freq) ->
                WordSuggestionCandidate(
                    text = displayCase(word, raw, isArabic),
                    confidence = freq.coerceAtLeast(150) / 255.0,
                    sourceProvider = this@LatinLanguageProvider,
                )
            })
            addAll(candidates(dictMatches))
        }.distinctBy { it.text }.take(maxCandidateCount)
        if (merged.isNotEmpty()) return merged

        // No word starts with what was typed: offer the nearest known spellings
        // (edit distance <= 1) as "did you mean?" candidates, ranked by frequency.
        val correctionEntries = index.corrections(prefix, maxCandidateCount)
        val fallback = candidates(correctionEntries)
        if (fallback.isEmpty()) return fallback
        // DRS v1.17.0: TRUE autocorrect — the FIRST high-frequency correction of a
        // real typo becomes auto-commit eligible, so pressing space silently fixes
        // the word (backspace reverts). The decider is conservative: the typed
        // word matched no prefix at all (genuine typo branch), it is long enough,
        // and only corpus-head frequencies qualify. User-dictionary matches and
        // normal prefix suggestions never auto-commit.
        return if (AutocorrectDecider.shouldAutoCommit(
                typedLength = raw.length,
                typedIsPrefixMatch = false,
                correctionFreq = correctionEntries.first().freq,
                enabled = prefs.suggestion.autocorrectEnabled.get(),
            )
        ) {
            buildList {
                add(fallback.first().copy(isEligibleForAutoCommit = true))
                addAll(fallback.drop(1))
            }
        } else {
            fallback
        }
    }

    /**
     * Next-word prediction: extracts the last word before the cursor from the composed
     * content and looks it up in the language's bigram table. Candidates keep their
     * corpus spelling and are ranked by co-occurrence score. Purely static data, so it
     * is safe in every session type.
     */
    private suspend fun nextWordCandidates(
        subtype: Subtype,
        content: EditorContent,
        maxCandidateCount: Int,
    ): List<SuggestionCandidate> {
        if (maxCandidateCount <= 0) return emptyList()
        val before = content.textBeforeSelection
        if (before.isBlank()) return emptyList()

        // Extract the last letter-run before the cursor (works for Arabic and Latin).
        var end = before.length
        while (end > 0 && !before[end - 1].isLetter()) end--
        var start = end
        while (start > 0 && before[start - 1].isLetter()) start--
        if (start == end) return emptyList()
        val prev = before.substring(start, end)
        if (prev.length < 2 || prev.length > MAX_WORD_LENGTH || prev.any { it.isDigit() }) return emptyList()

        val isArabic = prev.any { it in ARABIC_SCRIPT_START..ARABIC_SCRIPT_END }
        val key = if (isArabic) normalize(prev) else prev.lowercase()
        if (key.isEmpty()) return emptyList()
        val nexts = bigramsFor(subtype)[key] ?: return emptyList()

        return nexts.take(maxCandidateCount).map { (word, score) ->
            WordSuggestionCandidate(
                text = displayCase(word, word, isArabic),
                confidence = score / 99.0,
                sourceProvider = this,
            )
        }
    }

    /**
     * DRS: personal user dictionary entries for the active primary language, cached for
     * a few seconds to keep per-keystroke cost low. Never throws; on any failure an empty
     * list is returned and personalization silently degrades.
     */
    private suspend fun userDataFor(subtype: Subtype): List<UserDictionaryEntry> {
        val locale = subtype.primaryLocale
        val now = SystemClock.elapsedRealtime()
        userDataCache.withLock { it ->
            if (now - it.loadedAtElapsed < USER_WORDS_TTL_MS) return it.entries
        }
        val fresh = runCatching {
            DictionaryManager.default().queryAllUserWords(locale).take(USER_WORDS_MAX)
        }.getOrElse { emptyList() }
        userDataCache.withLock { it ->
            it.loadedAtElapsed = now
            it.entries = fresh
        }
        return fresh
    }

    /** DRS: user words whose (normalized) spelling starts with the typed prefix. */
    private suspend fun userWordMatchesFor(
        subtype: Subtype,
        prefix: String,
        isArabic: Boolean,
        raw: String,
        limit: Int,
    ): List<Pair<String, Int>> {
        if (limit <= 0 || prefix.isEmpty()) return emptyList()
        return userDataFor(subtype).mapNotNull { entry ->
            val word = entry.word
            if (word == raw || word.length > MAX_WORD_LENGTH || word.any { it.isDigit() }) return@mapNotNull null
            val normWord = if (isArabic) normalize(word) else word.lowercase()
            if (normWord.startsWith(prefix)) word to entry.freq else null
        }.sortedByDescending { it.second }.take(limit)
    }

    private suspend fun invalidateUserDataCache() {
        userDataCache.withLock { it -> it.loadedAtElapsed = Long.MIN_VALUE }
    }

    /**
     * DRS: shared learning entry point. Writes into the app-private user dictionary only,
     * never in private sessions, and only for plausible words (letters only, sane length).
     */
    private suspend fun learnWord(subtype: Subtype, word: String, insertFreq: Int): Boolean {
        val clean = word.trim()
        if (clean.length !in 2..MAX_WORD_LENGTH) return false
        if (!clean.all { it.isLetter() }) return false
        if (clean.any { it.isDigit() }) return false
        val changed = runCatching {
            DictionaryManager.default().insertOrBumpUserWord(clean, subtype.primaryLocale, insertFreq)
        }.getOrElse { false }
        if (changed) invalidateUserDataCache()
        return changed
    }

    override suspend fun notifySuggestionAccepted(subtype: Subtype, candidate: SuggestionCandidate) {
        flogDebug { candidate.toString() }
        // DRS: reinforce personally learned words when a suggestion is accepted.
        if (lastSuggestWasPrivate) return
        if (!prefs.dictionary.learnFromSuggestions.get()) return
        if (candidate !is WordSuggestionCandidate) return
        val word = candidate.text.toString()
        learnWord(subtype, word, LEARNED_INSERT_FREQ_FROM_SUGGESTION)
    }

    override suspend fun learnExternalWord(subtype: Subtype, word: String, isPrivateSession: Boolean) {
        if (isPrivateSession) return
        if (!prefs.dictionary.learnTypedWords.get()) return
        learnWord(subtype, word, LEARNED_INSERT_FREQ_TYPED)
    }

    override suspend fun notifySuggestionReverted(subtype: Subtype, candidate: SuggestionCandidate) {
        flogDebug { candidate.toString() }
    }

    override suspend fun removeSuggestion(subtype: Subtype, candidate: SuggestionCandidate): Boolean {
        flogDebug { candidate.toString() }
        return false
    }

    override suspend fun getListOfWords(subtype: Subtype): List<String> {
        return dictFor(subtype)?.words?.keys?.toList() ?: emptyList()
    }

    override suspend fun getFrequencyForWord(subtype: Subtype, word: String): Double {
        val index = dictFor(subtype) ?: return 0.0
        val freq = index.words[word]
            ?: index.findByPrefix(normalize(word), limit = 1).firstOrNull()
                ?.takeIf { it.norm == normalize(word) }?.freq
            ?: 0
        return freq / 255.0
    }

    override suspend fun destroy() {
        // Here we have the chance to de-allocate memory and finish our work. However this might never be called if
        // the app process is killed (which will most likely always be the case).
    }

    // DRS v1.18.0: the normalization pipeline lives in the pure
    // LatinWordNormalize object (strips Arabic diacritics/tatweel, unifies
    // hamza carriers, folds Latin accents, lowercases) — one tested path
    // for both the loaded dictionary and the typed prefix.
    private fun normalize(word: String): String = LatinWordNormalize.normalize(word)

    /**
     * Adapts the dictionary spelling to the user's capitalization for Latin scripts.
     * Arabic text is returned unchanged.
     */
    private fun displayCase(word: String, typed: String, isArabic: Boolean): String = when {
        isArabic -> word
        typed.first().isUpperCase() -> word.replaceFirstChar { it.uppercaseChar() }
        else -> word
    }
}
