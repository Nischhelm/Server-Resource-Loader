package serverresourceloader;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import serverresourceloader.config.ConfigHandler;
import serverresourceloader.fixes.MC83039_EndCityStructureFix;

// after:* so the recipe overrides in postInit come after the recipes of all other mods
@Mod(modid = Tags.MOD_ID, version = Tags.VERSION, name = Tags.MOD_NAME, dependencies = "after:*", acceptableRemoteVersions = "*")
public class ServerResourceLoader {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (ConfigHandler.fixEndCityChests) MC83039_EndCityStructureFix.generate(ResourceOverrideManager.DIRECTORY.toPath());
        ResourceOverrideManager.scan();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        RecipeOverrides.load();
    }
}
