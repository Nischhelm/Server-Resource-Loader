package serverresourceloader.handlers;

import fermiumbooter.annotations.MixinConfig;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import serverresourceloader.ServerResourceLoader;

@Config(modid = ServerResourceLoader.MODID)
public class ForgeConfigHandler {
	
	@Config.Comment("Server-Side Options")
	@Config.Name("Server Options")
	public static final ServerConfig server = new ServerConfig();

	@Config.Comment("Client-Side Options")
	@Config.Name("Client Options")
	public static final ClientConfig client = new ClientConfig();

	@MixinConfig(name = ServerResourceLoader.MODID) //Needed on config classes that contain MixinToggles for those mixins to be added
	public static class ServerConfig {

		@Config.Comment("Example server side config option")
		@Config.Name("Example Server Option")
		public boolean exampleServerOption = true;

		@Config.Comment("Example Early Mixin Toggle Config")
		@Config.Name("Enable Vanilla Player Mixin (Vanilla)")
		@MixinConfig.MixinToggle(earlyMixin = "mixins.serverresourceloader.vanilla.json", defaultValue = false)
		public boolean enableVanillaMixin = false;

		@Config.Comment("Example Late Mixin Toggle Config")
		@Config.Name("Enable JEI Init Mixin (JEI)")
		@MixinConfig.MixinToggle(lateMixin = "mixins.serverresourceloader.jei.json", defaultValue = false)
		@MixinConfig.CompatHandling(
				modid = "jei",
				desired = true,
				reason = "Mod needed for this Mixin to properly work",
				warnIngame = false //use this if the mixin is for an optional mod dependency that can be skipped with no issue if the mod is not present
		)
		public boolean enableJeiMixin = false;

		@Config.Comment("Enables the tutorial mixin.")
		@Config.Name("Enable Example Mixins")
		@MixinConfig.MixinToggle(lateMixin = "mixins.serverresourceloader.examples.json", defaultValue = true)
		public boolean enableExampleMixin = true;
	}

	public static class ClientConfig {

		@Config.Comment("Example client side config option")
		@Config.Name("Example Client Option")
		public boolean exampleClientOption = true;
	}

	@Mod.EventBusSubscriber(modid = ServerResourceLoader.MODID)
	private static class EventHandler{

		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.getModID().equals(ServerResourceLoader.MODID)) {
				ConfigManager.sync(ServerResourceLoader.MODID, Config.Type.INSTANCE);
			}
		}
	}
}