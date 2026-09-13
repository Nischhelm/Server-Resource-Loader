package serverresourceloader;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RecipeOverrides {

    /**
     * Recipes can reference ingredients from the _constants.json of their mod, which forge only loads for the recipes in the mod jar.
     * JsonContext can't load constants from outside of forge, so this is the same thing done by hand.
     */
    private static JsonContext createContext(String namespace) {
        Map<String, Ingredient> constants = new HashMap<>();
        JsonContext context = new JsonContext(namespace) {
            @Override
            public Ingredient getConstant(String name) {
                return constants.get(name);
            }
        };

        try (InputStream stream = RecipeOverrides.class.getResourceAsStream("/assets/" + namespace + "/recipes/_constants.json")) {
            if (stream == null) return context;
            for (JsonObject json : JsonUtils.fromJson(CraftingHelper.GSON, new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject[].class)) {
                if (CraftingHelper.processConditions(json, "conditions", context))
                    constants.put(JsonUtils.getString(json, "name"), CraftingHelper.getIngredient(json.get("ingredient"), context));
            }
        } catch (Exception e) {
            ServerResourceLoader.LOGGER.error("Failed to load recipe constants of {}", namespace, e);
        }
        return context;
    }

    public static void load() {
        ForgeRegistry<IRecipe> registry = (ForgeRegistry<IRecipe>) ForgeRegistries.RECIPES;
        ModContainer activeMod = Loader.instance().activeModContainer();
        Map<String, JsonContext> contexts = new HashMap<>();
        int replaced = 0, added = 0;

        for (Map.Entry<ResourceLocation, Path> entry : ResourceOverrideManager.getRecipes().entrySet()) {
            ResourceLocation id = entry.getKey();

            try (BufferedReader reader = Files.newBufferedReader(entry.getValue())) {
                JsonObject json = JsonUtils.fromJson(CraftingHelper.GSON, reader, JsonObject.class);
                JsonContext context = contexts.computeIfAbsent(id.getNamespace(), RecipeOverrides::createContext);

                if (!CraftingHelper.processConditions(json, "conditions", context)) {
                    ServerResourceLoader.LOGGER.debug("Skipping recipe override {}, conditions not met", id);
                    continue;
                }
                IRecipe recipe = CraftingHelper.getRecipe(json, context);

                // Forge warns about "dangerous alternative prefix" if the namespace doesn't match the active mod
                Loader.instance().setActiveModContainer(Loader.instance().getIndexedModList().get(id.getNamespace()));
                recipe.setRegistryName(id);

                if (registry.remove(id) != null) replaced++;
                else added++;
                registry.register(recipe);
                ServerResourceLoader.LOGGER.debug("Loaded recipe override: {}", id);
            } catch (Exception e) {
                ServerResourceLoader.LOGGER.error("Failed to load recipe override {} from {}", id, entry.getValue(), e);
            }
        }
        Loader.instance().setActiveModContainer(activeMod);

        // the client recipe book was already built when forge loaded the recipes
        FMLCommonHandler.instance().resetClientRecipeBook();
        ServerResourceLoader.LOGGER.info("Loaded recipe overrides: {} replaced, {} new", replaced, added);
    }
}
