package serverresourceloader.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import serverresourceloader.ResourceOverrideManager;
import serverresourceloader.ServerResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Mixin(TemplateManager.class)
public abstract class TemplateManagerMixin {

    // vanilla caches templates by path only, ignoring the namespace
    @Shadow @Final private Map<String, Template> templates;

    @Shadow protected abstract void readTemplateFromStream(String id, InputStream stream) throws IOException;

    // Puts the override into the vanilla cache before vanilla would load its own template into it.
    // Not done in readTemplate, as end cities and mansions pass no server and skip that method.
    @Inject(method = "get", at = @At("HEAD"))
    private void serverresourceloader$loadTemplateOverride(MinecraftServer server, ResourceLocation id, CallbackInfoReturnable<Template> cir) {
        Path path = ResourceOverrideManager.getStructure(id);
        if (path == null || this.templates.containsKey(id.getPath())) return;

        try (InputStream stream = Files.newInputStream(path)) {
            this.readTemplateFromStream(id.getPath(), stream);
            ServerResourceLoader.LOGGER.debug("Loaded structure template override: {}", id);
        } catch (Throwable t) {
            ServerResourceLoader.LOGGER.error("Failed to load structure template override {} from {}, using the default template", id, path, t);
        }
    }
}
