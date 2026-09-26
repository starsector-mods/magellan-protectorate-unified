package data.variants;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VariantIntegrityTest {

    private static final Set<String> FIGHTER_INCOMPATIBLE_HULLMODS = Set.of(
            "unstable_injector",
            "heavyarmor",
            "fluxdistributor",
            "fluxcoil",
            "surveying_equipment",
            "militarized_subsystems",
            "degraded_drive_field",
            "targetingunit",
            "dedicated_targeting_core"
    );

    private static final Pattern HULL_ID_PATTERN = Pattern.compile("\"hullId\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern HULL_SIZE_PATTERN = Pattern.compile("\"hullSize\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern HULL_MODS_BLOCK_PATTERN = Pattern.compile("\"hullMods\"\\s*:\\s*\\[([^\\]]*)\\]", Pattern.DOTALL);
    private static final Pattern STRING_ENTRY_PATTERN = Pattern.compile("\"([^\"]+)\"");

    @Test
    public void testFighterVariantsDoNotContainCrashingHullmods() throws Exception {
        File hullsDir = new File("data/hulls");
        assertTrue(hullsDir.exists(), "data/hulls directory must exist");

        Set<String> fighterHulls = new HashSet<>();
        File[] shipFiles = hullsDir.listFiles((dir, name) -> name.endsWith(".ship"));
        if (shipFiles != null) {
            for (File shipFile : shipFiles) {
                String content = Files.readString(shipFile.toPath());
                Matcher idMatcher = HULL_ID_PATTERN.matcher(content);
                Matcher sizeMatcher = HULL_SIZE_PATTERN.matcher(content);
                if (idMatcher.find() && sizeMatcher.find()) {
                    if ("FIGHTER".equalsIgnoreCase(sizeMatcher.group(1))) {
                        fighterHulls.add(idMatcher.group(1));
                    }
                }
            }
        }
        assertFalse(fighterHulls.isEmpty(), "Fighter hulls set should not be empty");

        File variantsDir = new File("data/variants");
        assertTrue(variantsDir.exists(), "data/variants directory must exist");

        List<File> variantFiles = new ArrayList<>();
        collectVariantFiles(variantsDir, variantFiles);
        assertFalse(variantFiles.isEmpty(), "Variant files should not be empty");

        List<String> violations = new ArrayList<>();
        for (File variantFile : variantFiles) {
            String content = Files.readString(variantFile.toPath());
            Matcher idMatcher = HULL_ID_PATTERN.matcher(content);
            if (idMatcher.find()) {
                String hullId = idMatcher.group(1);
                if (fighterHulls.contains(hullId)) {
                    Matcher modsBlockMatcher = HULL_MODS_BLOCK_PATTERN.matcher(content);
                    if (modsBlockMatcher.find()) {
                        String modsBlock = modsBlockMatcher.group(1);
                        Matcher entryMatcher = STRING_ENTRY_PATTERN.matcher(modsBlock);
                        while (entryMatcher.find()) {
                            String mod = entryMatcher.group(1);
                            if (FIGHTER_INCOMPATIBLE_HULLMODS.contains(mod)) {
                                violations.add(variantFile.getName() + " (hull: " + hullId + ") contains incompatible hullmod: " + mod);
                            }
                        }
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(), "Fighter variants must not contain hullmods that crash the game on HullSize.FIGHTER:\n" + String.join("\n", violations));
    }

    private void collectVariantFiles(File dir, List<File> accumulator) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collectVariantFiles(f, accumulator);
            } else if (f.getName().endsWith(".variant")) {
                accumulator.add(f);
            }
        }
    }
}
