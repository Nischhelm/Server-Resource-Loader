package serverresourceloader.fixes;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import serverresourceloader.ServerResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * MC-83039: end city towers generating on top of a house destroy its loot chests.
 * The bottom layer of the tower base has air blocks that replace the chests of the house below it.
 * The tower is randomly shifted by one block, so the chests end up at x = 3 and 4 or at x = 4 and 5 of the tower base.
 * Blocks missing from a template are left untouched during generation, so the fix is a tower base without those three.
 */
public class MC83039_EndCityStructureFix {

    /**
     * Writes the fixed tower base as a structure override, unless there already is an override for it
     */
    public static void generate(Path serverResourcesDir) {
        Path target = serverResourcesDir.resolve("structures/minecraft/endcity/tower_base.nbt");
        if (Files.exists(target)) return;

        try (InputStream vanilla = MinecraftServer.class.getResourceAsStream("/assets/minecraft/structures/endcity/tower_base.nbt")) {
            NBTTagCompound template = CompressedStreamTools.readCompressed(vanilla);
            NBTTagList blocks = template.getTagList("blocks", 10);
            for (int i = blocks.tagCount() - 1; i >= 0; i--) {
                NBTTagList pos = blocks.getCompoundTagAt(i).getTagList("pos", 3);
                if (pos.getIntAt(0) >= 3 && pos.getIntAt(0) <= 5 && pos.getIntAt(1) == 0 && pos.getIntAt(2) == 0) blocks.removeTag(i);
            }

            Files.createDirectories(target.getParent());
            try (OutputStream out = Files.newOutputStream(target)) {
                CompressedStreamTools.writeCompressed(template, out);
            }
            ServerResourceLoader.LOGGER.info("Generated {} to fix end city chests (MC-83039)", target);
        } catch (IOException | NullPointerException e) {
            ServerResourceLoader.LOGGER.error("Failed to generate {}", target, e);
        }
    }
}
