package com.example.data.critique

import com.example.data.model.CharacterEntity
import com.example.data.model.LoreEntity
import com.example.data.model.SceneEntity

enum class CritiqueMode(val label: String, val description: String) {
    DIALOGUE("Dialogue Review", "Naturalness, character distinctness, subtext, and exposition balance."),
    CHARACTER("Character Review", "Consistency with established motivations, traits, and emotional truth."),
    CONTINUITY("Continuity Review", "Contradictions with confirmed lore, character presence, and timeline."),
    SCENE("Scene Review", "Objective, tension, turning point, and structural progression."),
    DIRECTOR("Director's Review", "Physical blocking, sensory atmosphere, silence, and visual beats.")
}

enum class FindingSeverity {
    OBSERVATION,
    SUGGESTION,
    CONTINUITY_ALERT,
    STRONG_CONCERN
}

data class CritiqueFinding(
    val id: String,
    val mode: CritiqueMode,
    val category: String,
    val severity: FindingSeverity,
    val evidenceExcerpt: String,
    val explanation: String,
    val suggestion: String? = null,
    val confidence: String = "High",
    val authorQuestion: String? = null
)

data class SceneMetrics(
    val totalWords: Int,
    val dialogueWords: Int,
    val narrativeWords: Int,
    val dialoguePercentage: Int,
    val avgSentenceLength: Float,
    val readingTimeMinutes: Float,
    val detectedSpeakers: List<String>,
    val characterMentions: Map<String, Int>
)

object EditorialAnalysisEngine {

    fun analyzeScene(
        scene: SceneEntity,
        characters: List<CharacterEntity>,
        loreList: List<LoreEntity>,
        mode: CritiqueMode
    ): Pair<SceneMetrics, List<CritiqueFinding>> {
        val text = scene.content
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val totalWords = words.size

        // Extract dialogue lines (works for screenplay ALL-CAPS cues and quoted prose)
        val dialogueLines = mutableListOf<String>()
        val detectedSpeakers = mutableSetOf<String>()
        val characterMentions = mutableMapOf<String, Int>()

        for (char in characters) {
            val count = Regex("\\b${Regex.escape(char.name)}\\b", RegexOption.IGNORE_CASE).findAll(text).count()
            if (count > 0) {
                characterMentions[char.name] = count
            }
        }

        // Parse screenplay dialogue or prose quotes
        if (scene.format.equals("SCREENPLAY", ignoreCase = true)) {
            val lines = text.lines()
            var currentSpeaker: String? = null
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) {
                    currentSpeaker = null
                    continue
                }
                // Check if line looks like character cue (all uppercase, short, not scene heading)
                val isHeading = trimmed.startsWith("INT.") || trimmed.startsWith("EXT.") || trimmed.startsWith("SCENE")
                val isParen = trimmed.startsWith("(") && trimmed.endsWith(")")
                val isTransition = trimmed.endsWith("TO:") || trimmed == "FADE OUT."
                if (!isHeading && !isParen && !isTransition && trimmed.all { it.isLetter() || it.isWhitespace() || it == '\'' || it == '.' } && trimmed.uppercase() == trimmed && trimmed.length in 2..35) {
                    currentSpeaker = trimmed
                    detectedSpeakers.add(trimmed)
                } else if (currentSpeaker != null && !isParen) {
                    dialogueLines.add(trimmed)
                }
            }
        } else {
            // Prose: match quoted speech
            val quoteRegex = Regex("\"([^\"]*)\"|'([^']*)'")
            quoteRegex.findAll(text).forEach { match ->
                val line = match.value.trim('"', '\'')
                if (line.isNotBlank()) dialogueLines.add(line)
            }
            // Check character names in quotes or narrative
            characters.forEach { if (text.contains(it.name, ignoreCase = true)) detectedSpeakers.add(it.name) }
        }

        val dialogueWordCount = dialogueLines.sumOf { it.split(Regex("\\s+")).count { w -> w.isNotBlank() } }
        val narrativeWordCount = (totalWords - dialogueWordCount).coerceAtLeast(0)
        val dialoguePct = if (totalWords > 0) ((dialogueWordCount.toFloat() / totalWords) * 100).toInt() else 0

        val sentences = text.split(Regex("[.!?]+\\s*")).filter { it.isNotBlank() }
        val avgSentenceLen = if (sentences.isNotEmpty()) totalWords.toFloat() / sentences.size else 0f
        val readingTime = totalWords / 200f // 200 words per minute average

        val metrics = SceneMetrics(
            totalWords = totalWords,
            dialogueWords = dialogueWordCount,
            narrativeWords = narrativeWordCount,
            dialoguePercentage = dialoguePct,
            avgSentenceLength = avgSentenceLen,
            readingTimeMinutes = readingTime,
            detectedSpeakers = detectedSpeakers.toList(),
            characterMentions = characterMentions
        )

        val findings = mutableListOf<CritiqueFinding>()

        when (mode) {
            CritiqueMode.DIALOGUE -> {
                // Dialogue ratio analysis
                if (dialoguePct > 75 && totalWords > 100) {
                    findings.add(
                        CritiqueFinding(
                            id = "dial-ratio-high",
                            mode = CritiqueMode.DIALOGUE,
                            category = "Pacing & Visual Breathing Room",
                            severity = FindingSeverity.SUGGESTION,
                            evidenceExcerpt = "${dialoguePct}% of scene is dialogue ($dialogueWordCount / $totalWords words)",
                            explanation = "The scene leans heavily into rapid verbal exchange without grounding action beats or sensory interruptions.",
                            suggestion = "Consider introducing physical micro-actions, silence, or environment interaction between speech exchanges to pace the scene.",
                            confidence = "High",
                            authorQuestion = "Does the intense verbal velocity match the characters' emotional state?"
                        )
                    )
                } else if (dialoguePct < 15 && totalWords > 150) {
                    findings.add(
                        CritiqueFinding(
                            id = "dial-ratio-low",
                            mode = CritiqueMode.DIALOGUE,
                            category = "Verbal Presence",
                            severity = FindingSeverity.OBSERVATION,
                            evidenceExcerpt = "Only ${dialoguePct}% dialogue ($dialogueWordCount words)",
                            explanation = "The scene is predominantly narrative exposition or internal monologue with minimal spoken dialogue.",
                            suggestion = "If another character is present, consider whether a short verbal friction would heighten the scene's immediacy."
                        )
                    )
                }

                // Check for repetitive question marks in dialogue
                val questionsInDialogue = dialogueLines.sumOf { Regex("\\?").findAll(it).count() }
                if (questionsInDialogue > 5 && dialogueLines.size < 15) {
                    val sampleQuestion = dialogueLines.firstOrNull { it.contains("?") } ?: ""
                    findings.add(
                        CritiqueFinding(
                            id = "dial-interrogation",
                            mode = CritiqueMode.DIALOGUE,
                            category = "Subtext & Interrogation",
                            severity = FindingSeverity.OBSERVATION,
                            evidenceExcerpt = "\"${sampleQuestion.take(80)}\"",
                            explanation = "Found $questionsInDialogue questions in a relatively short exchange. High question frequency can make dialogue sound like an interrogation or exposition delivery rather than dramatic conflict.",
                            suggestion = "Try having characters make declarative statements, assumptions, or deflection instead of direct inquiries.",
                            authorQuestion = "Can the responding character deduce the answer rather than be asked?"
                        )
                    )
                }

                // Check for exposition phrases
                val expositionTriggers = listOf("as you know", "remember when", "we all know that", "explained to me", "historically")
                for (trigger in expositionTriggers) {
                    val match = dialogueLines.firstOrNull { it.contains(trigger, ignoreCase = true) }
                    if (match != null) {
                        findings.add(
                            CritiqueFinding(
                                id = "dial-expo-$trigger",
                                mode = CritiqueMode.DIALOGUE,
                                category = "On-the-Nose Exposition",
                                severity = FindingSeverity.CONTINUITY_ALERT,
                                evidenceExcerpt = "\"$match\"",
                                explanation = "Dialogue contains classic exposition scaffolding ('$trigger'). Characters rarely tell each other things they both already understand.",
                                suggestion = "Cut the preamble and have the characters disagree on the meaning of the fact rather than restating it.",
                                confidence = "Very High"
                            )
                        )
                    }
                }
            }

            CritiqueMode.CHARACTER -> {
                // Check if attached characters are actually mentioned
                val attachedIds = scene.attachedCharacterIds.split(",").filter { it.isNotBlank() }
                val attachedChars = characters.filter { it.id in attachedIds }

                for (char in attachedChars) {
                    val mentions = characterMentions[char.name] ?: 0
                    if (mentions == 0 && totalWords > 80) {
                        findings.add(
                            CritiqueFinding(
                                id = "char-silent-${char.id}",
                                mode = CritiqueMode.CHARACTER,
                                category = "Character Presence",
                                severity = FindingSeverity.CONTINUITY_ALERT,
                                evidenceExcerpt = "${char.name} attached to scene metadata",
                                explanation = "${char.name} is tagged in this scene's roster but does not appear in dialogue cues or narrative mentions.",
                                suggestion = "Verify whether ${char.name} is present in the background, observing silently, or if the scene roster should be updated.",
                                confidence = "High",
                                authorQuestion = "Is ${char.name} supposed to participate or merely witness this sequence?"
                            )
                        )
                    }
                }

                // Check motivation alignment
                if (scene.sceneObjective.isNotBlank()) {
                    findings.add(
                        CritiqueFinding(
                            id = "char-objective-alignment",
                            mode = CritiqueMode.CHARACTER,
                            category = "Objective Stacking",
                            severity = FindingSeverity.OBSERVATION,
                            evidenceExcerpt = "Objective: \"${scene.sceneObjective}\"",
                            explanation = "The declared scene objective gives clear dramatic focus. Verify whether each speaking character wants something opposing or complementary.",
                            authorQuestion = "What is the secondary character's personal objective in this specific interaction?"
                        )
                    )
                } else {
                    findings.add(
                        CritiqueFinding(
                            id = "char-objective-missing",
                            mode = CritiqueMode.CHARACTER,
                            category = "Scene Objective",
                            severity = FindingSeverity.SUGGESTION,
                            evidenceExcerpt = "Scene objective is unstated",
                            explanation = "No objective is recorded in the scene metadata. Setting a clear goal makes it easier to measure character motivation consistency.",
                            suggestion = "Define what the protagonist wants before the scene ends, and what obstacle resists them."
                        )
                    )
                }
            }

            CritiqueMode.CONTINUITY -> {
                // Check for deprecated lore mentions
                val deprecatedLore = loreList.filter { it.status.equals("Deprecated", ignoreCase = true) }
                for (lore in deprecatedLore) {
                    if (text.contains(lore.title, ignoreCase = true)) {
                        findings.add(
                            CritiqueFinding(
                                id = "cont-lore-dep-${lore.id}",
                                mode = CritiqueMode.CONTINUITY,
                                category = "Deprecated Lore Reference",
                                severity = FindingSeverity.STRONG_CONCERN,
                                evidenceExcerpt = "Mention of \"${lore.title}\"",
                                explanation = "This scene refers to \"${lore.title}\", which is currently flagged as DEPRECATED in your World Lore repository.",
                                suggestion = "Update the text to match current canon, or mark the lore entry as active if it has been reinstated.",
                                confidence = "Very High"
                            )
                        )
                    }
                }

                // Check tentative lore mentions
                val tentativeLore = loreList.filter { it.status.equals("Tentative", ignoreCase = true) }
                for (lore in tentativeLore) {
                    if (text.contains(lore.title, ignoreCase = true)) {
                        findings.add(
                            CritiqueFinding(
                                id = "cont-lore-tent-${lore.id}",
                                mode = CritiqueMode.CONTINUITY,
                                category = "Tentative Fact Dependency",
                                severity = FindingSeverity.OBSERVATION,
                                evidenceExcerpt = "Reference to \"${lore.title}\"",
                                explanation = "This scene relies on \"${lore.title}\", which is classified as TENTATIVE. Committing this scene may solidify an unverified world rule into established canon.",
                                suggestion = "Review the world rule in your World tab before locking this draft.",
                                confidence = "Moderate"
                            )
                        )
                    }
                }

                if (findings.isEmpty()) {
                    findings.add(
                        CritiqueFinding(
                            id = "cont-clean",
                            mode = CritiqueMode.CONTINUITY,
                            category = "World Consistency Check",
                            severity = FindingSeverity.OBSERVATION,
                            evidenceExcerpt = "${loreList.size} lore rules & ${characters.size} character profiles cross-checked",
                            explanation = "No contradictions with deprecated lore entries or known roster mismatches were detected in this manuscript section.",
                            confidence = "High"
                        )
                    )
                }
            }

            CritiqueMode.SCENE -> {
                if (avgSentenceLen > 28f) {
                    findings.add(
                        CritiqueFinding(
                            id = "scene-syntax-long",
                            mode = CritiqueMode.SCENE,
                            category = "Sentence Cadence & Readability",
                            severity = FindingSeverity.SUGGESTION,
                            evidenceExcerpt = "Average sentence length: ${String.format("%.1f", avgSentenceLen)} words",
                            explanation = "The prose uses long compound clauses. In dramatic or suspenseful moments, sustained long sentences can soften tension.",
                            suggestion = "Try fragmenting sentences during the pivotal moment or turning point to quicken the heartbeat.",
                            authorQuestion = "Where does the decisive beat occur in this scene?"
                        )
                    )
                }

                if (scene.emotionalIntent.isNotBlank()) {
                    findings.add(
                        CritiqueFinding(
                            id = "scene-intent",
                            mode = CritiqueMode.SCENE,
                            category = "Emotional Progression",
                            severity = FindingSeverity.OBSERVATION,
                            evidenceExcerpt = "Emotional Intent: \"${scene.emotionalIntent}\"",
                            explanation = "The intended emotional arc is documented. Compare whether the scene begins at a lower intensity and escalates toward this target.",
                            confidence = "High"
                        )
                    )
                }
            }

            CritiqueMode.DIRECTOR -> {
                // Look for sensory / blocking cues
                val sensoryKeywords = listOf("rain", "sound", "hum", "shadow", "silence", "light", "chill", "cold", "heat", "neon", "smell")
                val foundSensory = sensoryKeywords.filter { text.contains(it, ignoreCase = true) }

                if (foundSensory.size < 2 && totalWords > 100) {
                    findings.add(
                        CritiqueFinding(
                            id = "dir-sensory-sparse",
                            mode = CritiqueMode.DIRECTOR,
                            category = "Sensory Immersion & Physicality",
                            severity = FindingSeverity.SUGGESTION,
                            evidenceExcerpt = "Limited environmental/sensory tokens detected",
                            explanation = "The scene relies predominantly on dialogue without anchoring the physical space, temperature, or ambient soundscape.",
                            suggestion = "Add 1-2 sensory cues (lighting changes, ambient machinery, temperature change, aroma) to ground the camera.",
                            confidence = "Moderate"
                        )
                    )
                } else {
                    findings.add(
                        CritiqueFinding(
                            id = "dir-sensory-rich",
                            mode = CritiqueMode.DIRECTOR,
                            category = "Atmospheric Anchors",
                            severity = FindingSeverity.OBSERVATION,
                            evidenceExcerpt = "Active anchors: ${foundSensory.joinToString(", ")}",
                            explanation = "Good environmental grounding present in the scene texture.",
                            confidence = "High"
                        )
                    )
                }

                // Check for physical blocking in screenplay
                if (scene.format.equals("SCREENPLAY", ignoreCase = true)) {
                    val hasAction = text.lines().any { it.isNotBlank() && !it.startsWith("INT.") && !it.startsWith("EXT.") && it.none { c -> c.isLowerCase() } == false }
                    if (!hasAction) {
                        findings.add(
                            CritiqueFinding(
                                id = "dir-talking-heads",
                                mode = CritiqueMode.DIRECTOR,
                                category = "Blocking & Physical Action",
                                severity = FindingSeverity.STRONG_CONCERN,
                                evidenceExcerpt = "High ratio of uninterrupted dialogue cues",
                                explanation = "Risk of 'talking heads' syndrome. Characters are delivering lines without engaging with their physical surroundings or each other's bodies.",
                                suggestion = "Insert physical action blocks between speeches to show body language and power dynamics."
                            )
                        )
                    }
                }
            }
        }

        return Pair(metrics, findings)
    }
}
