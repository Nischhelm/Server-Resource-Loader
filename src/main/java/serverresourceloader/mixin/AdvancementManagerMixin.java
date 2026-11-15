package serverresourceloader.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import serverresourceloader.ResourceOverrideManager;
import serverresourceloader.ServerResourceLoader;
import serverresourceloader.config.ConfigHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Mixin(AdvancementManager.class)
public abstract class AdvancementManagerMixin {

    @Shadow @Final public static Gson GSON;

    /**
     * Runs after the world folder advancements are loaded, but before the ones of minecraft and mods.
     * Those skip every id already in the map, so the overrides and removed advancements put in here never get parsed
     * (no log errors for broken advancements that are overridden or removed anyway).
     */
    @ModifyArg(
            method = "reload",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementManager;loadBuiltInAdvancements(Ljava/util/Map;)V")
    )
    private Map<ResourceLocation, Advancement.Builder> serverresourceloader$loadAdvancementOverrides(Map<ResourceLocation, Advancement.Builder> advancements) {
        for (Map.Entry<ResourceLocation, Path> entry : ResourceOverrideManager.getAdvancements().entrySet()) {
            try {
                String json = new String(Files.readAllBytes(entry.getValue()), StandardCharsets.UTF_8);
                Advancement.Builder advancement = JsonUtils.gsonDeserialize(GSON, json, Advancement.Builder.class);
                if (advancement == null) throw new JsonParseException("File is empty");

                advancements.put(entry.getKey(), advancement);
                ServerResourceLoader.LOGGER.debug("Loaded advancement override: {}", entry.getKey());
            } catch (IOException | IllegalArgumentException | JsonParseException e) {
                // not in the map, so the default advancement still gets loaded
                ServerResourceLoader.LOGGER.error("Failed to load advancement override {} from {}, using the default advancement", entry.getKey(), entry.getValue(), e);
            }
        }

        if (!ResourceOverrideManager.getRemovedAdvancements().isEmpty()) {
            // placeholder that only blocks the id, taken out again before the advancements get built
            Advancement.Builder placeholder = JsonUtils.gsonDeserialize(GSON, "{\"criteria\":{\"removed\":{\"trigger\":\"minecraft:impossible\"}}}", Advancement.Builder.class);
            for (ResourceLocation id : ResourceOverrideManager.getRemovedAdvancements()) {
                // can't check the map for this later, the placeholder keeps minecraft and mods from loading it
                boolean exists = advancements.containsKey(id)
                        || AdvancementManager.class.getResource("/assets/" + id.getNamespace() + "/advancements/" + id.getPath() + ".json") != null;
                if (exists) ServerResourceLoader.LOGGER.debug("Removed advancement: {}", id);
                else ServerResourceLoader.LOGGER.warn("Could not remove advancement {}, it does not exist", id);
                advancements.put(id, placeholder);
            }
        }
        return advancements;
    }

    @ModifyArg(
            method = "reload",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementList;loadAdvancements(Ljava/util/Map;)V")
    )
    private Map<ResourceLocation, Advancement.Builder> serverresourceloader$removeAdvancements(Map<ResourceLocation, Advancement.Builder> advancements) {
        advancements.keySet().removeAll(ResourceOverrideManager.getRemovedAdvancements());
        if (!ConfigHandler.removeChildAdvancements) return advancements;

        // only follows the removed ones, advancements that were missing their parent before still error like in vanilla
        Set<ResourceLocation> gone = new HashSet<>(ResourceOverrideManager.getRemovedAdvancements());
        boolean changed = !gone.isEmpty();
        while (changed) {
            changed = false;
            Iterator<Map.Entry<ResourceLocation, Advancement.Builder>> iterator = advancements.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ResourceLocation, Advancement.Builder> entry = iterator.next();
                ResourceLocation parent = ((AdvancementBuilderAccessor) entry.getValue()).serverresourceloader$getParentId();
                if (parent != null && gone.contains(parent)) {
                    iterator.remove();
                    gone.add(entry.getKey());
                    changed = true;
                    ServerResourceLoader.LOGGER.debug("Removed advancement {}, its parent {} was removed", entry.getKey(), parent);
                }
            }
        }
        return advancements;
    }
}
