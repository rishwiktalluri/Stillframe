package com.example

import com.example.data.critique.CritiqueMode
import com.example.data.critique.EditorialAnalysisEngine
import com.example.data.critique.FindingSeverity
import com.example.data.model.CharacterEntity
import com.example.data.model.LoreEntity
import com.example.data.model.SceneEntity
import org.junit.Assert.*
import org.junit.Test

class StillframeUnitTests {

    @Test
    fun testDialogueProportionAndMetrics() {
        val scene = SceneEntity(
            id = "test-scene-1",
            projectId = "test-proj",
            title = "The Meeting",
            format = "PROSE",
            content = "\"We cannot wait until the transit lines shut down,\" Silas said. \"The patrols are already mobilizing at Tier 2.\""
        )
        val char1 = CharacterEntity(id = "char-1", projectId = "test-proj", name = "Silas")
        val (metrics, findings) = EditorialAnalysisEngine.analyzeScene(
            scene = scene,
            characters = listOf(char1),
            loreList = emptyList(),
            mode = CritiqueMode.DIALOGUE
        )

        assertTrue(metrics.totalWords > 0)
        assertTrue(metrics.dialogueWords > 0)
        assertEquals(1, metrics.characterMentions["Silas"])
    }

    @Test
    fun testDeprecatedLoreContinuityDetection() {
        val deprecatedLore = LoreEntity(
            id = "lore-dep",
            projectId = "test-proj",
            title = "Neural Lattice Decay",
            content = "Declassified syndrome.",
            status = "Deprecated"
        )
        val scene = SceneEntity(
            id = "test-scene-2",
            projectId = "test-proj",
            title = "The Diagnosis",
            format = "PROSE",
            content = "Silas looked at the scanner. The symptoms of Neural Lattice Decay were undeniable."
        )

        val (_, findings) = EditorialAnalysisEngine.analyzeScene(
            scene = scene,
            characters = emptyList(),
            loreList = listOf(deprecatedLore),
            mode = CritiqueMode.CONTINUITY
        )

        val alert = findings.firstOrNull { it.severity == FindingSeverity.STRONG_CONCERN }
        assertNotNull("Should flag deprecated lore reference", alert)
        assertTrue(alert!!.explanation.contains("DEPRECATED"))
    }

    @Test
    fun testScreenplaySpeakerParsing() {
        val screenplay = """
            INT. VAULT - NIGHT
            
            SILAS
            Did you retrieve the key?
            
            MAYA
            It was already broken.
        """.trimIndent()

        val scene = SceneEntity(
            id = "test-scene-3",
            projectId = "test-proj",
            title = "Vault Infiltration",
            format = "SCREENPLAY",
            content = screenplay
        )

        val (metrics, _) = EditorialAnalysisEngine.analyzeScene(
            scene = scene,
            characters = listOf(
                CharacterEntity(id = "c1", projectId = "test-proj", name = "Silas"),
                CharacterEntity(id = "c2", projectId = "test-proj", name = "Maya")
            ),
            loreList = emptyList(),
            mode = CritiqueMode.DIALOGUE
        )

        assertTrue(metrics.detectedSpeakers.contains("SILAS"))
        assertTrue(metrics.detectedSpeakers.contains("MAYA"))
    }
}
