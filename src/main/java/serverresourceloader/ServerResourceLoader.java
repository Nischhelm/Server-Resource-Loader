package serverresourceloader;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import serverresourceloader.capability.CapabilityExampleHandler;
import serverresourceloader.handlers.ModRegistry;
import serverresourceloader.proxy.CommonProxy;

@Mod(modid = ServerResourceLoader.MODID, version = ServerResourceLoader.VERSION, name = ServerResourceLoader.NAME, dependencies = "required-after:fermiumbooter")
public class ServerResourceLoader {
    public static final String MODID = "serverresourceloader";
    public static final String VERSION = "1.0.0";
    public static final String NAME = "Server Resource Loader";
    public static final Logger LOGGER = LogManager.getLogger();
    public static boolean completedLoading = false;

	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        completedLoading = true;
    }
}