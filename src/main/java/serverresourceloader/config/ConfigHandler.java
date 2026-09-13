package serverresourceloader.config;

import net.minecraftforge.common.config.Config;
import serverresourceloader.Tags;

@Config(modid = Tags.MOD_ID)
public class ConfigHandler {

    @Config.Name("Fix End City Chests")
    @Config.Comment({
            "Fixes MC-83039, where end city towers generating on top of a house destroy its loot chests.",
            "Done by generating a fixed serverresources/structures/minecraft/endcity/tower_base.nbt whenever that file doesn't exist.",
            "To remove the fix, set this to false and delete that file."
    })
    @Config.RequiresMcRestart
    public static boolean fixEndCityChests = true;

    @Config.Name("Ignored Recipes")
    @Config.Comment("Recipes that will not be overridden even if a file for them exists, e.g. minecraft:iron_pickaxe")
    @Config.RequiresMcRestart
    public static String[] ignoredRecipes = {};

    @Config.Name("Ignored Loot Tables")
    @Config.Comment("Loot tables that will not be overridden even if a file for them exists, e.g. minecraft:chests/simple_dungeon")
    @Config.RequiresMcRestart
    public static String[] ignoredLootTables = {};

    @Config.Name("Ignored Structure Templates")
    @Config.Comment("Structure templates that will not be overridden even if a file for them exists, e.g. minecraft:igloo/igloo_top")
    @Config.RequiresMcRestart
    public static String[] ignoredStructures = {};

    @Config.Name("Ignored Advancements")
    @Config.Comment("Advancements that will not be overridden even if a file for them exists, e.g. minecraft:story/mine_stone")
    @Config.RequiresMcRestart
    public static String[] ignoredAdvancements = {};

    @Config.Name("Ignored Functions")
    @Config.Comment("Functions that will not be loaded even if a file for them exists, e.g. mypack:starter_kit")
    @Config.RequiresMcRestart
    public static String[] ignoredFunctions = {};
}
