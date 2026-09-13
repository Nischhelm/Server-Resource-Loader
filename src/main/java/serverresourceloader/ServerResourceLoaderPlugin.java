package serverresourceloader;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public class ServerResourceLoaderPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        try {
            Mixins.addConfiguration("mixins." + Tags.MOD_ID + ".json");
        } catch (NoClassDefFoundError e) {
            throw new RuntimeException(Tags.MOD_NAME + " requires a mod that provides Mixin, like FermiumBooter, MixinBootstrap, MixinCompat or any other (there's lots)", e);
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
