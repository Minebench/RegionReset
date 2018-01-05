package io.github.apfelcreme.RegionReset;

import com.griefcraft.lwc.LWC;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.minebench.plotsigns.PlotSigns;
import io.github.apfelcreme.RegionReset.Exceptions.ChunkNotLoadedException;
import io.github.apfelcreme.RegionReset.Exceptions.DifferentRegionSizeException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Plugin zum Reset von WorldGuard-Regionen mit einer Standard-Region
 * RegionReset
 * Copyright (C) 2015 Lord36 aka Apfelcreme
 * <p>
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * @author Lord36 aka Apfelcreme
 */
@SuppressWarnings("deprecation")
public class SchematicUtils {

    /**
     * pastes the schematic into a ProtectedRegion
     *
     * @param schematicFile the File the schematic is saved in
     * @param noAir         should air be pastet or not? (default = false)
     * @param region        the region the schematic shall be pasted into
     * @param world         the world the schematic is in
     * @throws DifferentRegionSizeException
     * @throws ChunkNotLoadedException
     */
    public static void pasteBlueprint(File schematicFile, boolean noAir, ProtectedRegion region, World world)
            throws DifferentRegionSizeException, ChunkNotLoadedException, MaxChangedBlocksException {
        try {
            EditSession editSession = new EditSession(new BukkitWorld(world), Integer.MAX_VALUE);
            SchematicFormat schematic = SchematicFormat.getFormat(schematicFile);
            CuboidClipboard clipboard = schematic.load(schematicFile);

            int length = Math.abs(region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX()) + 1;
            int height = Math.abs(region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY()) + 1;
            int width = Math.abs(region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ()) + 1;

            if (width != clipboard.getSize().getBlockX() || height != clipboard.getSize().getBlockY() || length != clipboard.getSize().getBlockZ()) {
                throw new DifferentRegionSizeException(region.getId(), schematicFile.getName());
            }

            // Check if chunks are loaded
            for (int x = region.getMinimumPoint().getBlockX(); x <= region.getMaximumPoint().getBlockX(); x++) {
                for (int z = region.getMinimumPoint().getBlockZ(); z < region.getMaximumPoint().getBlockZ(); z++) {
                    if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                        throw new ChunkNotLoadedException();
                    }
                }
            }

            clipboard.paste(editSession, region.getMinimumPoint(), noAir);
            editSession.flushQueue();
        } catch (DataException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * saves a schematic-file to /plugins/RegionReset/backups/[worldname] also
     * saves all the owners, flags and members into a separate .txt schematicfile
     *
     * @param schematicFile the file that contains the schematic
     * @param region        the region that shall be saved
     * @param world         the world that the region is in
     * @throws IOException
     * @throws EmptyClipboardException
     * @throws DataException
     * @throws ChunkNotLoadedException
     */
    public static void saveSchematic(File schematicFile, ProtectedRegion region, World world)
            throws IOException, EmptyClipboardException, DataException, ChunkNotLoadedException {

        if (world == null) {
            return;
        }
        if (!schematicFile.exists()) {
            schematicFile.getParentFile().mkdirs();
        }

        // Check if chunks are loaded
        for (int x = region.getMinimumPoint().getBlockX(); x <= region.getMaximumPoint().getBlockX(); x++) {
            for (int z = region.getMinimumPoint().getBlockZ(); z < region.getMaximumPoint().getBlockZ(); z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    throw new ChunkNotLoadedException();
                }
            }
        }
        // save the schematic
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1);
        CuboidClipboard clipboard = new CuboidClipboard(
                region.getMaximumPoint()
                        .subtract(region.getMinimumPoint())
                        .add(1, 1, 1),
                region.getMinimumPoint()
        );
        clipboard.copy(editSession);

        SchematicFormat.MCEDIT.save(clipboard, schematicFile);


        // save a .yml with members and owners in it
        File informationFile = new File(schematicFile.getAbsolutePath().replace(".schematic", ".yml"));
        if (!informationFile.exists()) {
            informationFile.getParentFile().mkdirs();
        }


        ArrayList<String> ownerList = new ArrayList<String>();
        ArrayList<String> memberList = new ArrayList<String>();

        for (UUID u : region.getOwners().getUniqueIds()) {
            ownerList.add(u.toString());
        }

        for (UUID u : region.getMembers().getUniqueIds()) {
            memberList.add(u.toString());
        }

        FileConfiguration config = YamlConfiguration
                .loadConfiguration(informationFile);
        config.set(region.getId() + ".owners", ownerList.size() > 0 ? ownerList
                : "");
        config.set(region.getId() + ".members",
                memberList.size() > 0 ? memberList : "");
        config.save(informationFile);

    }

    /**
     */

    /**
     * saves a selection as a blueprint
     *
     * @param blueprint the blueprint that shall be saved
     * @param selection the selection
     * @throws IOException
     * @throws DataException
     * @throws ChunkNotLoadedException
     */
    public static void saveBlueprintSchematic(Blueprint blueprint, Selection selection)
            throws IOException, DataException, ChunkNotLoadedException {

        if (selection == null || selection.getWorld() == null) {
            return;
        }
        if (!blueprint.getBlueprintFile().exists()) {
            blueprint.getBlueprintFile().getParentFile().mkdirs();
        }

        // Check if chunks are loaded
        for (int x = selection.getMinimumPoint().getBlockX(); x <= selection.getMaximumPoint().getBlockX(); x++) {
            for (int z = selection.getMinimumPoint().getBlockZ(); z < selection.getMaximumPoint().getBlockZ(); z++) {
                if (!selection.getWorld().isChunkLoaded(x >> 4, z >> 4)) {
                    throw new ChunkNotLoadedException();
                }
            }
        }
        // save the schematic
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(selection.getWorld()), -1);
        CuboidClipboard clipboard = new CuboidClipboard(
                selection.getNativeMaximumPoint()
                        .subtract(selection.getNativeMinimumPoint())
                        .add(1, 1, 1),
                selection.getNativeMinimumPoint()
        );
        clipboard.copy(editSession);

        SchematicFormat.MCEDIT.save(clipboard, blueprint.getBlueprintFile());
    }

    /**
     * Looks for signs with the sell line from PlotSigns written on the recently reset region.
     * If there is one, things will be written on it.
     *  @param sender a player
     * @param region the region that shall be inspected
     * @param world  the world the region is in
     */
    public static void buildPlotSignsSign(final CommandSender sender, final ProtectedRegion region, final World world) {
        if (RegionReset.getInstance().getPlotSigns() == null) {
            return;
        }

        List<Sign> signs = new ArrayList<>();

        String signSellLine = RegionReset.getInstance().getPlotSigns().getSellLine();
        for (int x = region.getMinimumPoint().getBlockX(); x <= region.getMaximumPoint().getBlockX(); x++) {
            for (int y = region.getMinimumPoint().getBlockY(); y <= region.getMaximumPoint().getBlockY(); y++) {
                for (int z = region.getMinimumPoint().getBlockZ(); z <= region.getMaximumPoint().getBlockZ(); z++) {
                    if (world.getBlockAt(x, y, z).getType() == Material.SIGN_POST
                            || world.getBlockAt(x, y, z).getType() == Material.WALL_SIGN) {
                        Sign tempSign = (Sign) world.getBlockAt(x, y, z).getState();
                        if (tempSign.getLines()[0].equals(signSellLine)) {
                            signs.add(tempSign);
                        }
                    }
                }
            }
        }
        if (signs.size() > 0) {
            Bukkit.getScheduler().runTask(RegionReset.getInstance(), () -> {
                if (region.getFlag(DefaultFlag.BUYABLE) != null && region.getFlag(DefaultFlag.PRICE) != null) {
                    PlotSigns plotSigns = RegionReset.getInstance().getPlotSigns();
                    region.setFlag(DefaultFlag.BUYABLE, true);
                    String[] lines = plotSigns.getSignLines(region);
                    for (Sign sign : signs) {
                        for (int i = 0; i < lines.length; i++) {
                            sign.setLine(i, lines[i]);
                        }
                        sign.update();
                    }
                } else {
                    for (Sign sign : signs) {
                        for (int i = 0; i < 4; i++) {
                            sign.setLine(i, "");
                        }
                        sign.update();
                    }
                }

            });
        }
    }

    public static void removeProtections(CommandSender sender, ProtectedRegion region, World world) {
        if (RegionReset.getInstance().getLWC() != null) {
            Bukkit.getScheduler().runTaskAsynchronously(RegionReset.getInstance(), () -> {
                LWC.getInstance().fastRemoveProtections(sender,
                        "world = '" + world.getName() + "'"
                                + " AND x >= " + region.getMinimumPoint().getBlockX()
                                + " AND y >= " + region.getMinimumPoint().getBlockY()
                                + " AND z >= " + region.getMinimumPoint().getBlockZ()
                                + " AND x <= " + region.getMaximumPoint().getBlockX()
                                + " AND y <= " + region.getMaximumPoint().getBlockY()
                                + " AND z <= " + region.getMaximumPoint().getBlockZ()
                        , false);
            });
        }
    }
}