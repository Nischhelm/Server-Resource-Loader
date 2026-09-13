package serverresourceloader.mixin;

import net.minecraft.advancements.FunctionManager;
import net.minecraft.command.FunctionObject;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import serverresourceloader.ResourceOverrideManager;
import serverresourceloader.ServerResourceLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Mixin(FunctionManager.class)
public abstract class FunctionManagerMixin {

    @Shadow @Final private Map<ResourceLocation, FunctionObject> functions;

    // Adds the functions after vanilla loaded the ones from the world folder, replacing the ones with the same name
    @Inject(method = "loadFunctions", at = @At("TAIL"))
    private void serverresourceloader$loadFunctions(CallbackInfo ci) {
        for (Map.Entry<ResourceLocation, Path> entry : ResourceOverrideManager.getFunctions().entrySet()) {
            try {
                this.functions.put(entry.getKey(), FunctionObject.create((FunctionManager) (Object) this, Files.readAllLines(entry.getValue(), StandardCharsets.UTF_8)));
                ServerResourceLoader.LOGGER.debug("Loaded function: {}", entry.getKey());
            } catch (Throwable t) {
                ServerResourceLoader.LOGGER.error("Failed to load function {} from {}", entry.getKey(), entry.getValue(), t);
            }
        }
    }
}
