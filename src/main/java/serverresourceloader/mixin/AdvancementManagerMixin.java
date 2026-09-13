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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Mixin(AdvancementManager.class)
public abstract class AdvancementManagerMixin {

    @Shadow @Final public static Gson GSON;

    // Adds the overrides to the advancements vanilla collected from the world folder, minecraft and mods, replacing the ones with the same name
    @ModifyArg(
            method = "reload",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementList;loadAdvancements(Ljava/util/Map;)V")
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
                ServerResourceLoader.LOGGER.error("Failed to load advancement override {} from {}, using the default advancement", entry.getKey(), entry.getValue(), e);
            }
        }
        return advancements;
    }
}
