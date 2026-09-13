package serverresourceloader.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import serverresourceloader.ResourceOverrideManager;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    // /reload: look for new and removed files before vanilla reloads loot tables, advancements and functions
    @Inject(
            method = "reload",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;saveAllPlayerData()V")
    )
    private void serverresourceloader$rescanOnReload(CallbackInfo ci) {
        ResourceOverrideManager.scan();
    }
}
