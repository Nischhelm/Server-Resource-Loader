package serverresourceloader.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(Advancement.Builder.class)
public interface AdvancementBuilderAccessor {

    @Accessor("parentId")
    @Nullable
    ResourceLocation serverresourceloader$getParentId();
}
