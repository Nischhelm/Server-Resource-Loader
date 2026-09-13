package serverresourceloader.mixin;

import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import serverresourceloader.ResourceOverrideManager;
import serverresourceloader.ServerResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(LootTableManager.class)
public abstract class LootTableManagerMixin {

    @Shadow @Final private static Gson GSON_INSTANCE;
    @Shadow @Final private LoadingCache<ResourceLocation, LootTable> registeredLootTables;

    // Puts the override into the vanilla cache before vanilla would load its own table into it
    @Inject(method = "getLootTableFromLocation", at = @At("HEAD"))
    private void serverresourceloader$loadLootTableOverride(ResourceLocation id, CallbackInfoReturnable<LootTable> cir) {
        Path path = ResourceOverrideManager.getLootTable(id);
        if (path == null || this.registeredLootTables.getIfPresent(id) != null) return;

        try {
            String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            // custom = true: same handling as loot tables in world/data/loot_tables, no LootTableLoadEvent is fired
            this.registeredLootTables.put(id, ForgeHooks.loadLootTable(GSON_INSTANCE, id, json, true, (LootTableManager) (Object) this));
            ServerResourceLoader.LOGGER.debug("Loaded loot table override: {}", id);
        } catch (IOException | IllegalArgumentException | JsonParseException e) {
            ServerResourceLoader.LOGGER.error("Failed to load loot table override {} from {}, using the default table", id, path, e);
        }
    }
}
