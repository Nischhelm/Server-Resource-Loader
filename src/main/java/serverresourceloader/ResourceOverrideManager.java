package serverresourceloader;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import serverresourceloader.config.ConfigHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class ResourceOverrideManager {

    private static final Map<ResourceLocation, Path> recipes = new HashMap<>();
    private static final Map<ResourceLocation, Path> lootTables = new HashMap<>();
    private static final Map<ResourceLocation, Path> structures = new HashMap<>();
    private static final Map<ResourceLocation, Path> advancements = new HashMap<>();
    private static final Map<ResourceLocation, Path> functions = new HashMap<>();

    private static final Set<ResourceLocation> removedRecipes = new HashSet<>();
    private static final Set<ResourceLocation> removedLootTables = new HashSet<>();
    private static final Set<ResourceLocation> removedAdvancements = new HashSet<>();
    private static final Set<ResourceLocation> removedFunctions = new HashSet<>();

    public static final File DIRECTORY = new File("serverresources");

    public static void scan() {
        scan("recipes", ".json", ConfigHandler.ignoredRecipes, recipes, true);
        scan("loot_tables", ".json", ConfigHandler.ignoredLootTables, lootTables, true);
        scan("structures", ".nbt", ConfigHandler.ignoredStructures, structures, true);
        scan("advancements", ".json", ConfigHandler.ignoredAdvancements, advancements, false);
        scan("functions", ".mcfunction", ConfigHandler.ignoredFunctions, functions, false);

        ServerResourceLoader.LOGGER.info("Found {} recipe, {} loot table, {} structure, {} advancement and {} function overrides in {}",
                recipes.size(), lootTables.size(), structures.size(), advancements.size(), functions.size(), DIRECTORY.getAbsolutePath());

        parseRemoved("recipe", ConfigHandler.removedRecipes, removedRecipes);
        parseRemoved("loot table", ConfigHandler.removedLootTables, removedLootTables);
        parseRemoved("advancement", ConfigHandler.removedAdvancements, removedAdvancements);
        parseRemoved("function", ConfigHandler.removedFunctions, removedFunctions);
    }

    private static void parseRemoved(String type, String[] entries, Set<ResourceLocation> removed) {
        removed.clear();
        Arrays.stream(entries)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(ResourceLocation::new)
                .forEach(removed::add);
        if (!removed.isEmpty()) ServerResourceLoader.LOGGER.info("Removing {} {}(s) via config", removed.size(), type);
    }

    /**
     * Maps [folder]/[namespace]/[path].[ext] to namespace:path, files directly in [folder] go to the minecraft namespace
     */
    private static void scan(String folder, String extension, String[] ignored, Map<ResourceLocation, Path> overrides, boolean namespaceIsMod) {
        overrides.clear();
        Path dir = DIRECTORY.toPath().resolve(folder);

        try {
            Files.createDirectories(dir);
            try (Stream<Path> files = Files.walk(dir)) {
                files.filter(Files::isRegularFile).forEach(file -> {
                    String name = dir.relativize(file).toString().replace('\\', '/');
                    if (!name.endsWith(extension)) return;
                    name = name.substring(0, name.length() - extension.length());

                    int slash = name.indexOf('/');
                    String namespace = slash < 0 ? "minecraft" : name.substring(0, slash);
                    ResourceLocation id = new ResourceLocation(namespace, name.substring(slash + 1));

                    if (Arrays.asList(ignored).contains(id.toString())) {
                        ServerResourceLoader.LOGGER.debug("Ignoring {} override {} (in ignore list)", folder, id);
                        return;
                    }
                    overrides.put(id, file);
                    ServerResourceLoader.LOGGER.debug("Found {} override: {}", folder, id);
                    if (namespaceIsMod && !Loader.isModLoaded(id.getNamespace()))
                        ServerResourceLoader.LOGGER.info("{} override {} is for a mod that is not loaded", folder, id);
                });
            }
        } catch (IOException e) {
            ServerResourceLoader.LOGGER.error("Failed to scan {}", dir, e);
        }
    }

    public static Map<ResourceLocation, Path> getRecipes() {
        return recipes;
    }

    public static Path getLootTable(ResourceLocation id) {
        return lootTables.get(id);
    }

    public static Path getStructure(ResourceLocation id) {
        return structures.get(id);
    }

    public static Map<ResourceLocation, Path> getAdvancements() {
        return advancements;
    }

    public static Map<ResourceLocation, Path> getFunctions() {
        return functions;
    }

    public static Set<ResourceLocation> getRemovedRecipes() {
        return removedRecipes;
    }

    public static boolean isLootTableRemoved(ResourceLocation id) {
        return removedLootTables.contains(id);
    }

    public static Set<ResourceLocation> getRemovedAdvancements() {
        return removedAdvancements;
    }

    public static Set<ResourceLocation> getRemovedFunctions() {
        return removedFunctions;
    }
}
