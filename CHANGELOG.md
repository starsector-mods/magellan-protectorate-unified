# Changelog

## v1.4.0 — Unified 0.98a Overhaul

### 🚀 Engine & Java 17 Compatibility
- **Java 17 Bytecode:** Fully recompiled all mod scripts for Java 17 and Starsector 0.98a compatibility.
- **Modern Test Suite:** Added JUnit 5 and JaCoCo automated test suites (39 unit and integration tests passing).
- **Strict JSON Compliance:** Sanitized 70+ `.faction`, `.skin`, and `.json` files, stripping all trailing commas, malformed arrays, and unquoted values to prevent crashes on Starsector 0.98a's strict parser.
- **WorldGen & Scavenge Safety:** Ensured all procedurally generated terrain entities (`DEBRIS_FIELD`, `ASTEROID_BELT`, ring bands) properly call `.setName()`, eliminating fatal NullPointerExceptions during campaign Scavenge ability usage.

---

### ⚔️ Faction Identity & Doctrine Overhaul
- **Doctrine Cap Parity:** Rebalanced all 9 faction doctrine profiles to strictly obey the vanilla 15-point soft cap (`officerQuality`, `shipQuality`, `numShips`, `shipSize`), eliminating hidden engine fleet-generation truncation.
- **Dedicated Fleet Doctrines:**
  - **Blackcollars:** Heavy shock assault doctrine, prioritizing heavily armored battlecruisers, elite officers, and 0 carriers.
  - **Skytiger Guards:** Dedicated aerospace carrier-vanguard with high carrier weight, disciplined officer cadres, and custom strikecraft wings.
  - **Tichel Yellowtails:** Corporate trade flotillas and defensive escorts with balanced carrier support and merchant escorts.
  - **The Herd:** Frontier scavenger wolfpacks with overwhelming numbers, low officer quality, and aggressive swarm tactics.
  - **Leveller Insurgents:** Asymmetric hit-and-run guerilla doctrine with high aggression and modified combat hulls.
  - **Derelict AI:** Permanently hostile automated drone swarms with 0 officers, max aggression, and fearless combat behavior.
  - **Ancient Starfarers:** Modernized doctrine array replacing legacy pre-0.95a format.
- **Hull Tag Isolation:** Overhauled `hullFrequency` tags across all subfactions: boosted signature hulls (e.g., `"magellan_blackcollar": 4`) while explicitly zeroing out competing subfaction tags to prevent parent-faction hull contamination.

---

### 🌐 Nexerelin 4X Integration
- **Playable Subfactions:** Blackcollars, Levellers, Tichel Yellowtails, and The Herd are now fully selectable as custom faction starts (`playableFaction: true`).
- **Faction Traits & Alignment:** Configured rich diplomacy matrices (militarist, authoritarian, predatory, lawless, subversive, ideological) and custom alignment profiles.
- **Starter Fleet Packages:** Built comprehensive starter packages across all tiers (`startShipsSolo`, `startShipsCombatSmall`, `startShipsCombatLarge`, `startShipsCarrierSmall`, `startShipsCarrierLarge`, `startShipsTradeSmall`, `startShipsTradeLarge`, `startShipsExplorerSmall`, `startShipsSuper`, `startShipsGrandFleet`).
- **Campaign Safety:** Fixed Ancient Starfarer start (`corvusCompatible: false`) to prevent procedural sector generation failures while preserving their custom start background.
- **Ground Warfare & Mining:** Added faction-specific ground invasion/defense bonuses and resource mining weight profiles.

---

### 📜 Campaign & Dialogue Systems (`rules.csv`)
- **Rosebriar Station Insurgent Logistics:** Reworked Leveller sanctuary interaction at Rosebriar Station into a 3-tiered material contribution system with UI text highlights, cooldown timers, and fallback paths:
  - *Field Repair Aid:* 50 Heavy Machinery → Reputation boost (`SHRINE_OFFERING`), 20-day cooldown.
  - *Industrial Foundry Support:* 200 Heavy Machinery → Major reputation boost (`SYSTEM_BOUNTY`), 40-day cooldown.
  - *Arms & Munitions Delivery:* 100 Hand Weapons + 100 Supplies → Maximum reputation boost (`HIGH_IMPACT_BOUNTY`), 40-day cooldown.
- **Story Point Interactions:** Integrated vanilla-style Story Point dialogue actions:
  - *Crucible Base (Blackcollars):* Spend a Story Point to present rare Domain-era tactical algorithms to the memorial archivist for an immediate major reputation gain.
  - *Rosebriar Station (Levellers):* Spend a Story Point to share classified Protectorate patrol schedules for an immediate major reputation gain.
- **Strict CSV Normalization:** Normalized `rules.csv` to exact 7-column rows, Unix LF line endings, and removed all empty comma-only rows that caused 0.98a engine `IndexOutOfBoundsException`.

---

### 🛡️ Hullmods & S-Mods
- **0.98a Story Point S-Mod Integration:** Added custom S-Mod bonus effects to core Magellan hullmods:
  - *Magellan Ablative Composites:* S-Modding grants +10% Armor Rating and reduces EMP damage taken by 50%.
  - *Magellan Battleline Doctrine:* S-Modding increases ballistic weapon rate of fire by 10% and armor rating by 5%.
  - *Magellan Converted Shuttle Bay:* S-Modding completely removes the 50% fighter refit time penalty.
- **Refit Screen Registration:** Properly registered all S-Mod descriptions and tooltips in `hull_mods.csv`.

---

### 🔌 Ecosystem & Cross-Mod Compatibility
- **Backward-Compatible Graphics:** Restructured all custom mod graphics under `graphics/Magellan/` to maintain 100% backward compatibility with Sephira ecosystem submods (such as *Sephira Plus - Nightforge Skunkworks* and *Sephira Labs ADD*) without requiring edits to external mods.
- **Preserved Core Mod Hooks:** Retained mod ID (`mag_protect`), core ship hull IDs (`magellan_lightcruiser`, `magellan_cruiser`), blueprint tags (`magellan_core_bp`), and weapon FX scripts (`magellan_SuperSolenoidFX`, `magellan_FusbombFX`) for seamless cross-mod integration.
- **Vanilla Asset Protection:** Sanitized all engine-level asset references to point cleanly to standard `graphics/...` paths.

---

### 🎵 Asset & Size Optimization
- **High-Fidelity Audio Compression:** Re-encoded all 10 music soundtrack tracks in `sounds/music/` using Vorbis Quality 5 (~160 kbps VBR), reducing music folder size from **35.7 MB down to 13.1 MB (-63.3% / 22.6 MB saved)** with transparent, CD-grade audio quality.
- **Dead Asset Cleanup:** Removed unreferenced legacy test files and orphaned graphics folders.
- **Git & Build Pruning:** Purged build caches and pruned loose git objects, dropping runtime mod footprint significantly.

---

### ⚠️ Known Issues
- Using Console Commands to spawn the `Rusalka Mod-Destroyer-Leader` hull directly will result in a ship with no armor module — spawn a variant instead.
- Magellan ships with unique construction hullmods cannot mount modular Magellan hullmods due to legacy structural restrictions.

---

v1.3.2 - Starsector 0.98a Compatibility Update
- **0.98a Compatibility:** Fully updated and tested for Starsector 0.98a. The Protectorate flies again in the latest sector!
- **Standalone Core:** MagicLib is no longer required. The Magellan Protectorate now runs entirely on vanilla Starsector systems, ensuring better performance and a cleaner modlist. Custom fleet behaviors and weapons have been meticulously adapted to function natively.
- **Dunerunner's Rest Expansion:** Navigators have charted a new, treacherous system at the sector's fringe. Home to the elusive Herd fleets, this frontier features a White Dwarf star, three hostile worlds (Acrid, Scorched, Desolation), dense asteroid fields, and three hidden Battlestation outposts ripe for trading and scavenging.
- **Refit Screen Improvements:** Magellan engineering hullmods are now fully cataloged and selectable in the refit screen across all compatible hulls.
- **Combat Adjustments:** Standardized the officer corps of Magellan patrols for more consistent engagements. Weapon mounts across the fleet have also been re-calibrated to ensure burst sizes match their physical barrel counts, keeping combat authentic and balanced.
- **Visual Enhancements:** Upgraded the faction's visual identity using a new sprite processing engine, resulting in crisper and more cohesive hull liveries.
- **Stability Fixes:** Resolved critical navigation hazards that could cause fatal sensor crashes when scavenging in newly generated systems, as well as fixing databank conflicts that could disrupt game initialization.
- **Internal Cleanups:** Unified internal registry codes for smoother ongoing maintenance.

1.2.2 release
- 0.96 compatability + patch for Magellan Protectorate
- The Wayward Scion: reworked some variants
- Duncan-class Dreadnought: removed ammo regen bonus, updated AI for defensive system
- Hada Mod-Corvette: reworked with new weapons
- Niun Mod-Fighter: firing arcs of all weapons increased from 20 to 30 degrees
- Bastardsword [LV]: replaced Heavy Electron Bolter with Pulsed Beam Laser
- Added bounty success text for The Marauders, Part I and Part II

1.1.4 release
- The Marauders, Part II and Part III now require successful completion, rather than any end result, of their respective previous missions
- The Sensor Ghost fleet now uses the ML_bounty faction to avoid inter-mod problems
- Corain Mod-Merchanter: Deployment point cost reduced from 9 to 8
- Gorodin Mod-Scoutship: Deployment point cost reduced from 18 to 15, skeleton crew reduced from 50 to 40, Slamfire Torpedo Tube ammo increased from 3 to 4
- Fixed capitalization issues with hullmod icon file names

1.1.3 release
- Slight update to version requirements
- The player may now recover The Wayward Scion's Leveller Contra vessels with swapped fighters
- Duncan-class Dreadnought: added AI to and improved the defensive system
- Added missing reputation reward to The Sensor Ghost
- Added some missing apostrophes

1.1.2 release
NOT SAVE COMPATIBLE WITH ANY PREVIOUS RELEASE

- This mod now requires Magiclib version 0.46.0 or better

Ships:
- All mod-ships now have unique construction hullmods
- Reworked entire "The Wrathful Flame" questline, including text, rewards, and ships - now "The Marauders" questline with a revised set of mod-ships
- Reworked Mazian [ANC] - now Duncan-class Dreadnought, with rebalanced lances, new secondary weapons, and new mechanics
- Added the Corain Mod-Merchanter, a powerful jack-of-all-trades mod-ship
- Added the Gorodin Mod-Scoutship, a powerful jack-of-all-trades mod-ship
- Edger [BCR]: Ordnance Points reduced from 125 to 100, receives 3 built-in Jitte [BCR] Fighter-Bombers
- Bastardsword [LV]: reduced drone wing from 6 to 3, improved main gun, reduced deployment cost
- Removed Sung-C & Sung-M

Optional Content - Requires Dassault-Mikoyan Engineering
- Added the Rusalka Mod-Destroyer-Leader, a bastardized Capella remoulded by SNRI Labs

Fighters:
- Added Abban Support Fighter Wing, boasting a pair of long-ranged Bonecrushers, available to mercenaries and Magellan Independents
- Lochaber [BCR] & Bastardsword [BCR]: No longer modular or available for sale
- Added two hullmods to swap out Jitte [BCR] for Bastardsword [BCR] or Lochaber [BCR] on applicable ships
- Lochaber [BCR] & Bastardsword [BCR]: now actually use Ripfire SRMs rather than Chasefire SRMs
- Lochaber [BCR] Missile Bomber: changed firing sequence again, now fires 4x Ripfire and 1x Heavy Balefire Fusion Missile every 10s
- Bastardsword [BCR] Corvette: replaced Bonecracker Assault Gun with Revolver Assault Cannon
- Removed Forge-class Mech Wing
- Removed Bastardsword [Herd] Bomber

Weapons:
- Updated various weapons to match new values
- Boneripper Battery: renamed to Bonerattler Cannon, sprite overhauled and mechanics redesigned
- Voidshatter Lance & Heavy Voidshatter Lance: new firing visuals
- Voidshatter Lance: flux efficiency from 0.35 to 0.4
- Removed Ripfire SRM Storm

Misc:
- Added "Death of a Dunerunner" MagicBounties mission
- Added optional "The Wayward Scion" MagicBounties mission if Dassault-Mikoyan Engineering is installed
- Made MagicBounties more easily accessible at more locations
- Made MagicBounty fleets use MLBounty faction to avoid accidental gankings
- Abban Support Fighter available to Roider Union if Roider Union is installed
- Edger [BCR] & two BCR fighter hullmods available after completing "The Marauders" MagicBounty questline
- Bastardsword [LV] available after completing "The Wayward Scion" MagicBounty quest
- Removed "The Valca Guard" and "The Annore Startigers" Nexerelin Mercenaries
- Added new icons for "The Dunerunners" and "The Starfarer" Nexerelin Mercenaries, and adjusted their pricing and S-mod rates
- Updated Advanced Gunnery Control suggested settings for Magellan weaponry
- Probably a lot of other stuff I've forgotten
- Housekeeping. A lot of housekeeping. Don't even think about trying to load this on a save using an older version of this mod.


0.4 release
Ships:
- Added Sung-C [Herd], a Herd missile cruiser
- Added Sung-M [Herd], a Herd missile cruiser
- Added a relic from the past
Optional Content - Requires Arma Armature
- Added the Kapisi Mod-Carrier, a uniquely dangerous foe
- Added Bastardsword [MOD], a unique strikecraft
- Added Jitte [MOD], a unique strikecraft
- Added Bastardsword [LV], a Leveller strikecraft

Fighters:
- Bastardsword [Herd] Bomber Dirty Fusion Bombs Energy damage from 2000 to 1000, added 800-1200 HE damage on hull hit effect
- Lochaber [BCR] Missile Bomber OP cost from 18 to 20, Ripfire missile count from 20 to 16, modified Ripfire fire pattern, increased firing arc of missile mounts
- Modified Lochaber [BCR] Missile Bomber and Bastardsword [BCR] Gunship attack patterns

Weapons:
- Added Voidshatter Lance
- Updated Bonesaw-based weapons to match new values
- Updated Ripfire-based weapons to match new values
- Updated Barrage Flenser CIWS to use new Light Flenser projectiles, now uses magazine system

Misc:
- Added optional "The Wrathful Flame" MagicBounties mission arc if Arma Armature is enabled
- Added Advanced Gunnery Controls suggested weapon modes support
- Removed Magellan ships and weapons from Nex agent pool and Prism Freeport


0.3 release
Fighters:
- Added Forge Mech Fighter, a powerful but slow mech wing
- Changed Bastardsword [SKT] to Bastardsword [Herd]; new painjob and weapons

Misc:
- Added 3 mercenary groups for Nexerelin


0.2 release
Ships:
- Added Dekker (P) Frigate, armed with a Fusion Bomb Launcher
Fighters:
- Modified Bastardsword [BCR] Gunship to use Bonecracker Assault Gun rather than mini-Bonecrusher

Weapons:
- Added Flenser CIWS Barrage


0.1 release
Ships: Added Edger [BCR] Carrier, a powerful attack carrier

Fighters:
- Added Bastardsword [BCR] Gunship, a strong heavy fighter
- Added Lochaber [BCR] Missile Bomber, a standoff missile bomber

Weapons:
- Added Boneripper Battery
- Added Ripfire SRM Storm1.67a - The big Leveller/campaign update.

Content:
- Added several Magellan-specific derelict drones.
- Added Terschad-class frigate and TMC multi-skin frigate family.
- 

Balance:
- Reduced shot damage of Bonecrusher family to 600.
- Renamed Quad Autogun to Tribarrel Autogun; now fires bursts of 3, sustained DPS is 200.
- ER Light Autogun now fires 25x5 round bursts, same DPS (75).
- Revised Longfire TBM family; now fires a two-stage dumbfire missile that does impressive damage, but can easily miss.
- 

Campaign:
- First draft of Mothership Start concept
- Sprinkled some unique caches with unique defenders around
- Procgen spawning of Exile Fleet systems.
- 

1.5a (hotfix #2) - Much new good stuff. New content, heavy revisions, campaign changes.

Content:
- Externalized a bunch of strings to ease future translations.
- Implemented [TMC] skin subfaction. Unique Chela [TMC].
- Implemented [CIV] skins for a few ships. Unique Chela and Jitte [CIV].
- Three new modular Magellan-only hullmods, available at Magellan markets;
 • Magellan Engine Rebuild
 • Magellan Fighter Bay Crowding
 • Magellan Shield Tuning
- New Almarshad gunship frigate and Blackcollar skin.
- New Phillips destroyer freighter and Herd combat conversion.
- New small combat freighters, the Musa and Nan; they can be restored to a powerful original configuration.
- New Goforth command carrier for the Herd; replaces the old Edger skin, slower but more combat-capable.
- New weapon, the Micro Pulsed Laser, an unremarkable 4OP utility beam.
- New weapon, the Revolver Cannon, a 6OP close-combat grenade launcher.
- New weapon, the Blast Hammer Artillery, which will ruin your day, and everyone else's day around you, too.
- New built-in hullmods on some subfaction ships; Ablative Composites and Converted Shuttle Bay.
- New, excellent encounter and market music for the faction by MesoTroniK.
- New Leveller encounter and market music by Eric Matyas.

Balance:
- Mazian, Kreshov, and Graff have been converted to shieldless ships using Ablative Composites and high armor/hull values. New slot layouts, sprite tweaks.
- Janz, Yeager, and Dekker ship systems changed. Minor tweaks to many stats.
- Revised Bonesaw; now continuous fire, 200 damage/shot at 400DPS. OP cost changed to 5.
- Revised Boneshaker/Boneshaker Battery; now fire salvoes of 400x2 and 300x3 frag damage at 800 DPS. OP costs changed to 10/12OP, respectively.
- Returned the Beehive Cannon to a 150x1/50x5 shotgun instead of 175x1/75x3, because I got it right the first time.
- Adjusted Salvo Assault Gun stats (75dmg>50dmg) and OP cost (6OP>4OP) downwards; its old role as high-end small slot HE is now filled by the Revolver Cannon.
- Foxfire LRM damage decreased to 100 from 125.

Campaign:
- Independent markets in Magellan space now stock Magellan ships and hardware.
- Added a special military-grade arms market to Ghammol Station.
- Added a new independent world, Turan, and enlarged Valca. Minor market/industry tweaks to make trade more interesting.
- Magellan Starfarer start now includes a unique Demilitarized blueprint package.
- Added Leveller guard fleet to Rosebriar Station.
- Added Herd fleets to Khamn and Secundus Graveyard systems.
- Herd spawning script generates small Herd fleets around the unique open markets.
- Magellan space now has unique scavenger fleets.
- Restored Pariya's Black Market, upgraded to Heavy Batteries.
- Idiot-proofed some potential mod integration issues.

1.3a - 0.95.1a compatibility.

Content:
- Added Rounder Wing - heavy fighter version to Mallory, and bomber version to Mallory [LV].
- Added small Balefire MRM Tube for 5OP.

Balance:
- Reduced Silverdart cost to 8OP.
- Adjusted Kreshov slot locations (swapped Composite and Ballistic mediums) and speed (55 down to 50).
- Changed Longfire LRM to 150 HE base damage, with a 750 Fragmentation AoE on-hit effect. Top speed and missile HP both improved.
- Removed direct armor damage on Bonesaw/Boneshaker weapon family, upped base per-shot damage considerably (120 to 200) to compensate.

Campaign:
- Added characters to Magellan markets.
- Added Skytiger Guard HQ to Annore, to spawn everybody's favorite semi-competent-aristo-brute-squad in all their garish finery.
- Fixed Starfarer starting reputation irregularities.
- Fixed arms-dealer tags for Magellan weapons, ships, and subfaction skins.
- Added minor dialogue interactions at nonecon markets for Blackcollars (Crucible Base) and Levellers (Rosebriar Station).
- Hotfix for missing system tags.

1.25a - Minor new content, script refactoring, balance tweaks.

Content:
- Added Silverdart ESD Gun.

Balance:
- Most crit scripts now scale damage and other effects based on their parent projectile (especially useful for energy weapons).
- Added a check to keep onHit flux damage from being applied to ships with less total flux than the effect.
- Muzzle effects and shotgun scripts converted to onFire effects. Substantial performance improvements.
- Renamed Twin Autocannon to Silverbolt Autocannon, reduced shot damage to 150.
- Ripfire SRM base damage changed to 300 frag damage, with 150 additional scripted damage to armor.
- Changed Fusion Lance on Porey to Electron Lance; now has added EMP damage.
- Swapped out missile system on baseline Sung cruiser to an HE cluster MRM. Blackcollar Sung retains the Balefire launcher.
- Flenser family of PD weapons revamped, now use snazzy tracer FX.
- Electra CIWS now uses ammo, shots expire randomly towards max range. Should be less of a no-brainer.

Campaign:
- Added a special condition (City-Warrens) to make Jeshad a harder target for raids and invasions.

1.15 - New content, balance tweaks.

Content:
- Added Porey [BCR] ship skin
- Added Jitte [BCR] fighter-bomber wing
- Added Bastardsword [SKT] corvette wing as built-in on Edger [SKT]
- Added Flashfire Rocket Barrage, a Squall sidegrade with a unique bite.
- Added Ramey drone frigate (Leveller)
- Added Stunfire Grenade Pod (Leveller)

Balance:
- Reduced per-shot damage of Bonesaw/Boneshaker weapon family to 120, increased burst length from 3/6/9 to 4/8/12 shots. Removed proximity fuzing, and made on-hit effect do 30 damage that ignores armor.
- Changed Bonecracker core shot to HE damage.
- Tightened Beehive grouping, changed to 175 HE center projectile and 3x75 HE submunitions.
- Edger [SKT] now has a single Large Missile slot instead of two Mediums. Carries a Bastardsword [SKT] built-in wing.
- Changed Ripfire damage type to Fragmentation, increased shot damage to 300, with 150 extra armor damage on-hit.

Campaign:
- Added pirate market, Calicheman, to Secundus graveyard system.

1.08 RC2 - Fixes and new content.

Content:
- Added ER Light Autogun, a 3OP poking kinetic with 800 range and just about no other redeeming features.
- Added Balefire MRM Pod, 12OP's worth of energy-based support/finisher missile with lots of stored ammo.
- Added Porey Battlecruiser - a smaller, faster, more frontally-oriented capital, and arguably a more dangerous one.
- Added Keu Phase Cruiser, a slightly cheaper Doom with Fast Missile Racks and a very angry built-in Fusion Cannon.
- Revised Sung sprite. No more freaky missile tube geometry.
- Revised Shockfire MRM Pod sprite.

Balance:
- (RC2) Upped Bonecrusher Battery DPS to 800 from 750 (mainly cosmetic, but also cleaner numbers on the backend).
- (RC2) Reduced base shot damage of Bonsaw/Boneshaker weapons to 120 from 150, upped burst count to 4/8/12 (from 3/6/9) in compensation.
- (RC2) Changed core shot of Bonecracker to 60 HE damage (from 125 Frag), submunitions to 6x30 Frag damage (up from 5x25); tightened spread pattern
- (RC2) Reduced fighter damage bonus of Command Datalink shipsystem from 30% to 20%. Clarified tooltip description and system status indicators.
- Added single Chasefire AFM to Chela, improved Chela hull and armor to match Talons - should be more useful in dogfights and earn that 5OP price on your carriers.
- Added frag crits to small and large Mag Drivers. Now hitting armor with a big, slow slug sometimes spalls a chunk of it through some poor bastard inside.
- Small Mag Driver OP cost increased to 9 from 8.
- Reduced built-in Balefire launcher to four tubes, and increased base damage to 600 to compensate. Increased on-hit crit damage as well.
- Reduced Shockfire salvo to 5, ammo to 30, changed damage type to Energy and raised damage to 400.
- Reduced Ripfire damage to 135, small/med ammo to 30/100, reduced range slightly and added flameout on reaching max range. Should make them a little harder to run hog-wild with. Crit damage and frequency increased; they're deadly once armor has been stripped.
- Adjusted Cluster Autocannon; removed ammo, now 180/150 DPS/flux, doing 40 damage per pellet. Should be a more reliable weapon overall.
- Reduced small Foxfire ammo from 48 to 32. Should make them a little less useful in longer fights.
- Renamed 'Porey' destroyer to 'Pollard', 'Quillon' destroyer to 'Capella' and 'Keu' phase frigate to 'Yeager' to accommodate new content. Yes, it's confusing; shouldn't need to happen again.
- Trimmed Quillon/Capella top speed down to 125 from 145. A little more reasonable, still very fast.
- Revised Bonecrusher (but not Bonesaw/Boneshaker) weapons as rare Hybrid mount guns; 300 per-shot frag damage raised to 500, EMP and arc frequency doubled.

Campaign:
- Revised constellation geography. There's a bit more going on now.
- Tweaked some weapon availability.
- Added support for SCC's Reputation Decay mod.
- Added support for Commissioned Crews.
- Fixed missing relationships for Starfarer start, added several new relationships.

1.0 - Initial public release.

- 0.95a compatible.
- Adds a brand-new faction with unique low-tech and midline gear.
- Certified Bad™ artwork that ruins the game.
- Purposely breaks your immersion, then breaks it again.
- Communist propaganda that saps and impurifies your precious bodily fluids.
- *Probably* will not give you cancer.
- Probably. The lab rats are fine.Magellan Protectorate Plus v1.3 added content for Magellan v1.67

v1.3
Bugfix
-Fixed crashes on startup with Starsector v0.98a
-Removed bugged custom scenario Mothership start, the ship can now be picked as a supership start with Magellan or Magellan Starfarers

Ships
-Herd and Leveller Hager conversions can no longer accept Adaptive Drone Bay

Misc
-Added Take No Prisoners compatibility for Magellain factions

v1.2
Colonies
-Added Leveller ship and weapon market to independent Ghammol Station
-Skytiger market will now sell the antique Light/Heavy Bonegrinder Chaingun

Ships
-Fixed the Capella (BCR) showing up twice in build lists
-Konstantin (TMC) Supercarrier will very rarely be offered for sale if the player trades or upgrades the ship with Tichel
-Fixed a few Yellowtail ships showing as Tichel rather than Tichel Mercantile Concern in the ship building category list
-Carrier Droneship OP increased from 70 to 85, DP from 6 to 12, drone bays from 1 to 2 and are no longer built-in to give players chance to loot its drones
-Chenel (SKT) large ballistic hardpoint changed to large hybrid hardpoint
-Goforth renamed Goforth (Herd) and DP lowered from 30 to 24
-Hager all variants shield radius increased
-Hager (Herd) OP reduced from 90 to 60, DP increased from 8 to 12, cargo lowered from 700 to 200
-Hager (LV) Drone Carrier added to Leveller ship list
-Kant (LV) hybrid slots changed to energy due to Leveller faction no longer using ballistic weapons
-Mallory renamed Mallory (CIV), made slightly more common
-Mallory (M) made slightly more common, updated engine details
-Mallory (LV) renamed Mallory-C (LV), unique fleet leader located in the Leveller station system
-Mallory (LV) new generic variant with large universal slot in place of built-in weapon added to Leveller ship list
-Ramey (A) renamed Ramey-A (LV), missile hardpoints updated to synergy
-Ramey (B) renamed Ramey-B (LV), main weapon description updated
-Leveller Swarmfighter Assault Drone added to Leveller blueprint
-NPC ship variant updates

Weapons
-Witchfire missiles damage reduced from 1500 to 1000 kinetic, sprint range lowered from 1000 to 800, secondary stage HP made to match the initial 500
-Stunfire Grenade Pod renamed to Stunfire Missile Pod and text showns tracking to match actual weapon performance, will no longer conserve fire while shields are up
-Triple Fusion Cannon reduced recoil
-Added Modular Fusion Cannon large energy weapon, mostly used by the Skytigers
-Added Diffusion Heavy Repeater large energy weapon for the Levellers
-Added all Leveller weapons to their blueprint
-Made individual Magellan weapon blueprints easier to find as rare salvage

Misc
-Levellers now exclusively use Leveller weapons and the Electra CIWS
-Fixed a bug with Tichel executives appearing on random colonies
-Added Konstantin Mothership to Nexerelin supership start
-Deleted source folders since the files are several updates out of date and no longer being maintained

v1.1
Colonies
-Tichel Shipyard market on Pariya now sells modified Yellowtail cargo haulers and modified Yellowtail strike craft
-Tichel HQ structure on Pariya will no longer disappear if the player builds a Patrol HQ on the planet
-Slightly lowered the quality of Skytiger, Blackcollar, Yellowtail ships for sale

Hullmods
-Added Trajectory Analyzer hullmod for enhancing missile and archaic composite weapon range
-Tichel Merchantile Refit now reduces deployment point cost by 1/2/3/4 based on hull size
-Fixed a display bug with the Spartacus Reactor on Leveller ships

Ships
-All basic Magellan ship blueprints can now be acquired individually as salvage, and through the historian
-Several Magellan capital ships added to the advanced Magellan blueprint
-Added Rounder (BCR) Escort Fighter
-Bastardsword gains Ballistic Rangefinder
-Enabled Bastardsword (TMC) Corvette
-Bastardsword/Bastardsword (TMC) Corvette engagement range reduced to 0 to be more clear, since their support fighter AI behavior already imposed this limit
-Lowered OP value of Bastardsword (SKT) from 20 to 18
-Updated description of the Mallory and enabled the militarized version to very rarely appear in fleets and be sold on the market
-Increased Talley-class merchanter to destroyer size classification
-Added Ayres/Ayres (BCR) Light Carrier
-Added Mazian (BCR) Fleet Carrier
-Added Porey (TMC) Command Carrier
-Chenel shield radius enlarged
-Chenel (SKT) Light Cruiser gains Heavy Ballistic Integration, universal medium hardpoint changed to large ballistic hardpoint
-Chenel (TMC) gains energy weapon slots, Interdictor Array ship system
-Kant (BCR) ship system changed from Damper Field to Ballistic Accelerator
-Sung (BCR) Missile Cruiser various weapon slots changed to Hybrid, loses Heavy Ballistic Integration, system changed to Fast Missile Racks
-Added Philips (M) Light Freighter militarized variant
-Philipson fuel hauler given civilian tag so they will no longer charge into combat, engines also made to match Magellan style
-Reverted the weird black bar on Hagar supply ships added by v1.67 of base, except for the Herd version which is now even more cursed
-Adjusted the fleet point values of various strike craft wings to be more in line with the core game
-Minor graphics update for all Mazian variants
-Reduced excessive ship bounds for faster gameplay
-Updated NPC ship fittings

Weapons
-PD Maser/Heavy PD Maser will engage fighters in addition to missiles
-Added Witchfire ASM Launcher/Battery, mostly used by Blackcollars
-Micro Pulse Laser renamed Micro Burst Laser, range increased to 800, added to Magellan known weapons
-Added Combat Burst Laser
-Added Balefire MRM Battery (reuses Fusion Bomb Barrage launcher sprite)
-Balefire flight time reduced to 6 seconds to closer match range
-New weapon sprite for Fusion Bomb Barrage
-Fusion Bomb Launcher/Barrage changed to archaic composite weapon type
-Light/Heavy Bonegrinder, Scramfire LRBM antique weapons added to Skytiger known weapon list and market
-Converging Beam Laser antique weapon added to Blackcollar known weapon list and market
-Increased beam speed of various weapons since slow beams are silly but slow flicker beams are especially silly

Misc
-Enabled derelict content added in v1.67 of base mod
-Added quest event for trading a recovered Konstantin-class mothership to Tichel at Pariya
-Added Blackcollar Regiment blueprint
-Yellowtail ships now included in Tichel Blueprint
-Blueprint package prices increased
-Custom image for interacting with Magellan wrecks
-Various text updates and other minor tweaks

v1.0
Colonies
-New Blackcollar [BCR], Skytiger [SKT], and Tichel (Yellowtail) [TMC] faction flags
-Added Blackcollar market on Annore, Skytiger market on Jeshad, Tichel market on Pariya
-Changed High Command on Jeshad to Magellan Fleet HQ, which spawns regular and Blackcollar patrols
-Added Tichel Distribution Hub to Pariya, which spawn Tichel patrols and sells their ships
-Valca Bastion station is moved into the orbit of Valca to match the planet's description
-Moved hyperspace location of Karic system slightly to avoid spawning on top of Elysium Abyss/Silence system from Knights of Ludd
-Increased the size of Pariya to 4 so Annore and Valca Bastion will no longer be chronically short of fuel, Jeshad is still hooped
-New colony images

Ships and weapons
-Added Phillipson fuel hauler
-Added Graff [BCR] Destroyer and Janz [BCR] Frigate
-Capella [BCR] Destroyer-Leader added to regular Blackcollars lineup
-Chenel [SKT] Light Cruiser added to regular Skytigers lineup
-Tichel Mercantile Concern (Yellowtail) ships added to playable lineup, a few have lost their Interdictor Array and Shield Shunt for balance
-Maizan battleship given Converted Shuttle Bay
-Added Authority Heavy Maser large energy weapon, used by Blackcollars and Skytigers
-Blackcollar ships are fitted with more Antique Magellan weapons, which they also sell at Jeshad
-Fighter bays on Blackcollar and Skytiger ships are no longer built-in, and their unique strike craft variants can be bought and looted
-NPC ship variant updates

Misc
-Various text updates and other minor tweaks