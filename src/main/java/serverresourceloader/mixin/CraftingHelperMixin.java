package serverresourceloader.mixin;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.FilenameUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import serverresourceloader.RecipeOverrides;
import serverresourceloader.ResourceOverrideManager;

import java.nio.file.Path;

@Mixin(value = CraftingHelper.class, remap = false)
public abstract class CraftingHelperMixin {

    /**
     * Skips removed recipes before forge parses them, so recipes that would fail to load (e.g. unknown items) don't spam the log.
     * Targets the per-file lambda in CraftingHelper.loadRecipes(ModContainer), the id is built the same way forge does it.
     */
    @Inject(method = "lambda$loadRecipes$22", at = @At("HEAD"), cancellable = true)
    private static void serverresourceloader$skipRemovedRecipe(ModContainer mod, JsonContext ctx, Path root, Path file, CallbackInfoReturnable<Boolean> cir) {
        String relative = root.relativize(file).toString();
        if (!"json".equals(FilenameUtils.getExtension(file.toString())) || relative.startsWith("_")) return;

        ResourceLocation id = new ResourceLocation(ctx.getModId(), FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/"));
        if (ResourceOverrideManager.getRemovedRecipes().contains(id)) {
            RecipeOverrides.SKIPPED.add(id);
            cir.setReturnValue(true);
        }
    }
}
