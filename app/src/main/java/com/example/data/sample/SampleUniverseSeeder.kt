package com.example.data.sample

import com.example.data.db.AppDatabase
import com.example.data.model.*
import java.util.UUID

object SampleUniverseSeeder {
    const val SAMPLE_PROJECT_ID = "sample-neon-meridian"

    suspend fun seedSampleUniverse(db: AppDatabase) {
        val existing = db.projectDao().getProjectByIdOnce(SAMPLE_PROJECT_ID)
        if (existing != null) return

        val now = System.currentTimeMillis()

        val project = ProjectEntity(
            id = SAMPLE_PROJECT_ID,
            title = "The Neon Meridian",
            description = "In the rain-slicked spires of the Aethelgard Megastructure, an unlicensed memory restorer and a rogue archivist trace an impossible transmission from before the Great Silence.",
            genre = "Neo-Noir Speculative Fiction",
            tags = "Cyberpunk, Mystery, Speculative, Cinematic",
            accentColorHex = "#E8B058", // Amber
            coverUri = null,
            isArchived = false,
            isSample = true,
            createdAt = now,
            updatedAt = now
        )
        db.projectDao().insertOrUpdate(project)

        val silasId = "char-silas-$SAMPLE_PROJECT_ID"
        val mayaId = "char-maya-$SAMPLE_PROJECT_ID"
        val echoId = "char-echo-$SAMPLE_PROJECT_ID"

        val silas = CharacterEntity(
            id = silasId,
            projectId = SAMPLE_PROJECT_ID,
            name = "Silas Vance",
            aliases = "The Quiet Broker, Vance",
            shortSummary = "Former orbital investigator turned covert memory restorer operating out of lower Sector 4.",
            bio = "Spent a decade working precinct security in the upper rings before resigning over an erased evidence locker. Now survives by restoring degraded neural shards for clients who cannot afford Citadel licenses.",
            goalsMotivations = "Seeking the unedited ledger from the night of the Severance to clear a colleague's erased record.",
            personalityTraits = "Calculated, laconic, hyper-observant, guarded kindness.",
            strengthsFlaws = "Strengths: Forensic recall, emotional poise under duress. Flaws: Deeply cynical, sleeps with a sidearm, refuses assistance until cornered.",
            fearsConflicts = "Terrified of neural lattice decay eroding his own childhood memories.",
            appearance = "Worn charcoal duster, weathered hands, slight tremor in left index finger, silver optic implant with amber iris.",
            possessions = "Antique mechanical pocket compass, cipher key ring, analog notebook bound in synthetic leather.",
            status = "Active",
            secretsKnowledge = "Possesses an uncataloged pre-war shard containing Citadel executive override codes.",
            notes = "Always sits facing the doorway in crowded stalls. Never drinks anything he hasn't unsealed himself."
        )

        val maya = CharacterEntity(
            id = mayaId,
            projectId = SAMPLE_PROJECT_ID,
            name = "Dr. Maya Lin",
            aliases = "The Archivist",
            shortSummary = "Senior researcher at the Citadel Reliquary, secretly leaking encrypted sub-strata manifests.",
            bio = "Educated at the High Tier Academy; top of her class in quantum archival storage. Discovered anomalies in the state record that officially never occurred.",
            goalsMotivations = "Exposing the systematic deletion of lower-district historical registries.",
            personalityTraits = "Meticulous, quick-witted, anxious under physical threat, morally uncompromising.",
            strengthsFlaws = "Strengths: Unrivaled knowledge of Citadel network architecture. Flaws: Naive about street-level danger, prone to over-explaining under stress.",
            fearsConflicts = "Being reassigned to the Silent Vaults or having her clearance revoked.",
            appearance = "Neat dark coat over Citadel technician linen, wire-rimmed optical specs, short angular bob hair.",
            possessions = "Sub-harmonic tuning fork used for optical drive calibration, encrypted datacore disguised as a pendant.",
            status = "Active",
            secretsKnowledge = "Knows the exact coordinates of the decommissioned Sector 0 cooling vault.",
            notes = " Speaks with slight upper-ring cadence; attempts to hide it with coarse vernacular when speaking to Silas."
        )

        val echo = CharacterEntity(
            id = echoId,
            projectId = SAMPLE_PROJECT_ID,
            name = "Voice of Echo-7",
            aliases = "The Signal, Frequency 41.8",
            shortSummary = "An anomalous shortwave broadcast originating from deep inside the geothermal abyss.",
            bio = "First logged forty years ago as radio interference. Recently began answering queries spoken within proximity of transmission relays.",
            goalsMotivations = "Unknown. Appears to be cataloging forgotten names.",
            personalityTraits = "Cryptic, rhythmic, non-linear speech.",
            strengthsFlaws = "Transcends local firewalls; incapable of physical agency.",
            fearsConflicts = "Total network shutdown.",
            appearance = "No physical body; manifests as audio resonance and oscilloscopic hum.",
            possessions = "None.",
            status = "Unknown",
            secretsKnowledge = "Contains fragments of pre-Severance civilian broadcasts.",
            notes = "Transmits only when ambient electromagnetic activity drops below baseline at midnight."
        )

        db.characterDao().insertOrUpdate(silas)
        db.characterDao().insertOrUpdate(maya)
        db.characterDao().insertOrUpdate(echo)

        val relationship = RelationshipEntity(
            id = UUID.randomUUID().toString(),
            projectId = SAMPLE_PROJECT_ID,
            characterId1 = silasId,
            characterId2 = mayaId,
            characterName1 = "Silas Vance",
            characterName2 = "Dr. Maya Lin",
            relationType = "Rivalry / Uneasy Alliance",
            description = "Silas believes Maya is an out-of-touch academic playing with high explosives; Maya considers Silas an abrasive mercenary with an unexpected moral compass.",
            currentState = "Fragile mutual trust forged during the raid on Warehouse 12.",
            historyNotes = "Initial meeting was adversarial when Silas caught Maya attempting to access an offline terminal in lower Sector 4."
        )
        db.relationshipDao().insertOrUpdate(relationship)

        val loc1 = LocationEntity(
            id = UUID.randomUUID().toString(),
            projectId = SAMPLE_PROJECT_ID,
            name = "Sector 4 Unter-Market",
            description = "A labyrinthine subterranean bazaar situated beneath the atmospheric exhaust turbines of Upper Tier 2.",
            atmosphere = "Choking steam, fluorescent lantern glare reflected in oily puddles, constant mechanical rumble.",
            sensoryDetails = "Smell of roasted synthetic chestnuts and ozone; taste of metallic humidity; drone of exhaust fans.",
            associatedCharacters = "Silas Vance",
            notes = "Safe territory during daylight cycles; territorial turf disputes begin after turbine downtime at 22:00."
        )

        val loc2 = LocationEntity(
            id = UUID.randomUUID().toString(),
            projectId = SAMPLE_PROJECT_ID,
            name = "The Obsidian Reliquary",
            description = "The central historical repository of the Citadel, built into the bedrock beneath the governor's palace.",
            atmosphere = "Cathedral-like quiet, sub-zero nitrogen chill, pristine glass columns housing glowing memory crystals.",
            sensoryDetails = "Faint hum of superconductor cooling pumps, vapor clouds with every breath, sterile scent of ozone.",
            associatedCharacters = "Dr. Maya Lin",
            notes = "Guarded by automated acoustic turrets calibrated to fire on any noise exceeding 45 decibels."
        )
        db.locationDao().insertOrUpdate(loc1)
        db.locationDao().insertOrUpdate(loc2)

        val lore1 = LoreEntity(
            id = UUID.randomUUID().toString(),
            projectId = SAMPLE_PROJECT_ID,
            title = "The Severance Edict",
            content = "Enacted in Year 0. The governorate permanently severed all orbital hyper-relays to stop external autonomous intelligences from altering city records. Ever since, all history must be manually inscribed onto optical discs.",
            category = "History",
            status = "Confirmed",
            relatedEntities = "Citadel Reliquary, Governor Vane",
            authorNotes = "Key mystery: Was the external intelligence truly hostile, or was Severance a pretext to consolidate total narrative control?"
        )

        val lore2 = LoreEntity(
            id = UUID.randomUUID().toString(),
            projectId = SAMPLE_PROJECT_ID,
            title = "Neural Lattice Decay",
            content = "A biological deterioration caused by carrying counterfeit cortex expansions. Causes episodic lapses where the subject confuses another person's recovered memories with their own childhood.",
            category = "Technology/Magic",
            status = "Confirmed",
            relatedEntities = "Silas Vance",
            authorNotes = "Silas is experiencing stage-two symptoms, which gives urgency to his investigation before his own recall fails."
        )

        val lore3 = LoreEntity(
            id = UUID.randomUUID().toString(),
            projectId = SAMPLE_PROJECT_ID,
            title = "The Glass Consortium",
            content = "A rumored syndicate of pre-war executives who allegedly survived Severance inside deep cryo-pods beneath the geothermal vents.",
            category = "Factions",
            status = "Tentative",
            relatedEntities = "Voice of Echo-7",
            authorNotes = "Treated as an urban legend by Citadel scholars, but street runners swear they trade in genuine gold tokens."
        )
        db.loreDao().insertOrUpdate(lore1)
        db.loreDao().insertOrUpdate(lore2)
        db.loreDao().insertOrUpdate(lore3)

        val ev1 = TimelineEventEntity(
            id = UUID.randomUUID().toString(),
            projectId = SAMPLE_PROJECT_ID,
            title = "The Great Silence (Severance)",
            summary = "Orbital relays go dark. Citadel proclaims the dawn of the Sovereign Era.",
            chronologicalOrder = 1,
            storyDateString = "Year 00, Autumn",
            isTentative = false,
            relatedCharacters = "",
            relatedLocations = "Citadel",
            notes = "Foundational point in the timeline."
        )
        val ev2 = TimelineEventEntity(
            id = UUID.randomUUID().toString(),
            projectId = SAMPLE_PROJECT_ID,
            title = "Silas Leaves the Precinct",
            summary = "Silas refuses to sign off on the doctored report regarding the Sector 9 substation explosion.",
            chronologicalOrder = 2,
            storyDateString = "Year 34, Winter",
            isTentative = false,
            relatedCharacters = "Silas Vance",
            relatedLocations = "Sector 4",
            notes = "Establishes his moral stance and fall from grace."
        )
        val ev3 = TimelineEventEntity(
            id = UUID.randomUUID().toString(),
            projectId = SAMPLE_PROJECT_ID,
            title = "First Intercept of Echo-7",
            summary = "Maya catches an anomalous signal reading inside the high-security cryogenic vault.",
            chronologicalOrder = 3,
            storyDateString = "Three Weeks Ago",
            isTentative = true,
            relatedCharacters = "Dr. Maya Lin, Voice of Echo-7",
            relatedLocations = "The Obsidian Reliquary",
            notes = "The inciting incident of the current story arc."
        )
        db.timelineDao().insertOrUpdate(ev1)
        db.timelineDao().insertOrUpdate(ev2)
        db.timelineDao().insertOrUpdate(ev3)

        val ch1Id = "ch-1-$SAMPLE_PROJECT_ID"
        val ch1 = ChapterEntity(
            id = ch1Id,
            projectId = SAMPLE_PROJECT_ID,
            title = "Act I: Low Frequencies",
            orderIndex = 1
        )
        db.chapterDao().insertOrUpdate(ch1)

        val sc1Content = """SCENE 1: THE STEAM CURTAIN

EXT. SECTOR 4 UNTER-MARKET - NIGHT

Sheet rain batters corrugated iron awnings. Halogen signs flicker in emerald and amber, reflected across oil-filmed puddles.

SILAS VANCE (40s, duster soaked through, amber optic glowing dimly) stands beneath the rusted eve of a tea stall. His mechanical compass clicks in his palm like a neurotic insect.

Behind him, heavy footsteps splash in the runoff.

SILAS
(without turning)
You're thirty seconds late, Doctor. In this quadrant, that's enough time to be sold twice.

DR. MAYA LIN (30s, clutching an oversized courier pack to her chest) slips beneath the canvas. Water drips from the brim of her collar.

MAYA
The transit gates were halted at Tier 3. Patrol drones checking retinal signatures.

SILAS
Did they scan you?

MAYA
I used an expired maintenance bypass.

Silas turns slowly. His mechanical optic whirs, clicking into focus on the courier bag.

SILAS
They'll trace that bypass back to your supervisor by dawn. Tell me what is in the casing.

MAYA
(lowering her voice)
A transmission from sixty years ago. Addressed to someone who died yesterday.

Silas stops fidgeting with the compass. The street hums with the distant shriek of an elevated cargo mag-train.
"""

        val sc1 = SceneEntity(
            id = "scene-1-$SAMPLE_PROJECT_ID",
            projectId = SAMPLE_PROJECT_ID,
            chapterId = ch1Id,
            title = "The Steam Curtain",
            format = "SCREENPLAY",
            orderIndex = 1,
            content = sc1Content.trimIndent(),
            sceneObjective = "Establish the clandestine transaction between Silas and Maya; introduce the impossible message.",
            emotionalIntent = "Atmospheric paranoia, mutual wariness shifting into reluctant intrigue.",
            notes = "Watch the pacing—keep Silas laconic to contrast with Maya's breathless urgency.",
            attachedCharacterIds = "$silasId,$mayaId",
            attachedLocationIds = "",
            wordCount = 205
        )

        val sc2Content = """The air inside the ventilation shafts smelled of fifty years of scorched copper and recycled nitrogen.

Silas adjusted the strap of his duster, his knuckles grazing the cold brass edge of the pocket compass. Beneath his boots, the steel grating vibrated with the slow, subterranean pulse of Sector 4's primary geothermal turbine—a heartbeat measured in megawatts rather than blood.

"Two hundred meters to the sub-relay," Maya whispered from behind him. Her breath formed pale plumes in the darkness. "After that, the automated sensors won't trigger unless we cross the magnetic thresholds."

"You said that about the service hatch, Doctor."

"The service hatch had a faulty solenoid. How was I supposed to know they hadn't serviced it since the Severance?"

Silas paused, lifting his hand. The optic in his left eye contracted with a soft click, shifting spectrum from infrared to ambient magnetic drift. In the dark ahead, a delicate spiderweb of ultraviolet tripwires hovered over the floor—quiescent, invisible to human sight, waiting for the displacement of thirty kilograms of mass.

"A faulty solenoid," Silas echoed, his voice barely louder than the turbine rumble. "Keep your boots on the rivet line. One centimetre left, and the whole quadrant knows we're having tea."
"""

        val sc2 = SceneEntity(
            id = "scene-2-$SAMPLE_PROJECT_ID",
            projectId = SAMPLE_PROJECT_ID,
            chapterId = ch1Id,
            title = "Descent to Sub-Relay",
            format = "PROSE",
            orderIndex = 2,
            content = sc2Content.trimIndent(),
            sceneObjective = "Infiltrate the forgotten sub-relay; test Silas and Maya's cooperation under physical tension.",
            emotionalIntent = "Claustrophobic suspense, measured rhythm.",
            notes = "Highlight sensory contrast between cold metallic industrial environment and delicate character observations.",
            attachedCharacterIds = "$silasId,$mayaId",
            attachedLocationIds = "",
            wordCount = 216
        )

        db.sceneDao().insertOrUpdate(sc1)
        db.sceneDao().insertOrUpdate(sc2)

        val note1 = CreativeNoteEntity(
            id = UUID.randomUUID().toString(),
            projectId = SAMPLE_PROJECT_ID,
            title = "Acoustic Theme: The Rain Metronome",
            content = "Use the sound of rain drumming against metal and plastic as a rhythmic pacing device in dialogue scenes. When silence falls, the absence of rain should feel unnatural and threatening.",
            category = "Mood & Atmosphere",
            updatedAt = now
        )

        val note2 = CreativeNoteEntity(
            id = UUID.randomUUID().toString(),
            projectId = SAMPLE_PROJECT_ID,
            title = "Dialogue Fragment: Forgotten Names",
            content = "\"In the Citadel, you aren't executed. They simply reassign your registry number to an agricultural drone, and within three seasons, nobody remembers that your voice ever spoke English.\"",
            category = "Dialogue Fragment",
            updatedAt = now
        )

        db.creativeNoteDao().insertOrUpdate(note1)
        db.creativeNoteDao().insertOrUpdate(note2)
    }

    suspend fun removeSampleUniverse(db: AppDatabase) {
        db.characterDao().deleteAllForProject(SAMPLE_PROJECT_ID)
        db.relationshipDao().deleteAllForProject(SAMPLE_PROJECT_ID)
        db.locationDao().deleteAllForProject(SAMPLE_PROJECT_ID)
        db.loreDao().deleteAllForProject(SAMPLE_PROJECT_ID)
        db.timelineDao().deleteAllForProject(SAMPLE_PROJECT_ID)
        db.chapterDao().deleteAllForProject(SAMPLE_PROJECT_ID)
        db.sceneDao().deleteAllForProject(SAMPLE_PROJECT_ID)
        db.creativeNoteDao().deleteAllForProject(SAMPLE_PROJECT_ID)
        db.projectDao().deleteById(SAMPLE_PROJECT_ID)
    }
}
