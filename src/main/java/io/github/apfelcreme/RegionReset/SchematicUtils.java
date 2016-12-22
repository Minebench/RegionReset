package io.github.apfelcreme.RegionReset;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.apfelcreme.RegionReset.Exceptions.ChunkNotLoadedException;
import io.github.apfelcreme.RegionReset.Exceptions.DifferentRegionSizeException;
import me.ChrisvA.MbRegionConomy.MbRegionConomy;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
    public static void pasteBlueprint(final File schematicFile, final boolean noAir,
                                      final ProtectedRegion region, final World world)
            throws DifferentRegionSizeException, ChunkNotLoadedException {
        try {
            final EditSession editSession = new EditSession(new BukkitWorld(world), Integer.MAX_VALUE);
            SchematicFormat schematic = SchematicFormat
                    .getFormat(schematicFile);
            final CuboidClipboard clipboard = schematic.load(schematicFile);
            final int length = Math.abs(region.getMaximumPoint().getBlockX()
                    - region.getMinimumPoint().getBlockX()) + 1;
            final int height = Math.abs(region.getMaximumPoint().getBlockY()
                    - region.getMinimumPoint().getBlockY()) + 1;
            final int width = Math.abs(region.getMaximumPoint().getBlockZ()
                    - region.getMinimumPoint().getBlockZ()) + 1;
            if (length != clipboard.getSize().getBlockX()
                    || height != clipboard.getSize().getBlockY()
                    || width != clipboard.getSize().getBlockZ()) {
                throw new DifferentRegionSizeException(region.getId(), schematicFile.getName());
            }

            for (int x = 0; x < length; ++x) {
                for (int y = 0; y < height; ++y) {
                    for (int z = 0; z < width; ++z) {

                        if (!world.isChunkLoaded(region.getMinimumPoint()
                                .getBlockX() + x >> 4, region.getMinimumPoint()
                                .getBlockZ() + z >> 4)) {
                            throw new ChunkNotLoadedException();
                        }
                        final BaseBlock block = clipboard.getBlock(new Vector(
                                x, y, z));
                        if (block == null) {
                            continue;
                        }
                        if (noAir && block.isAir()) {
                            continue;

                        }

                        final int finalX = x;
                        final int finalY = y;
                        final int finalZ = z;

                        RegionReset.getInstance().getServer().getScheduler().runTask(RegionReset.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    editSession.setBlock(new Vector(finalX, finalY, finalZ).add(region
                                            .getMinimumPoint()), block);
                                } catch (MaxChangedBlocksException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
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

        // save a schematic schematicfile
        CuboidClipboard clipboard = new CuboidClipboard(region
                .getMaximumPoint().subtract(region.getMinimumPoint())
                .add(1, 1, 1));
        int length = Math.abs(region.getMaximumPoint().getBlockX()
                - region.getMinimumPoint().getBlockX()) + 1;
        int height = Math.abs(region.getMaximumPoint().getBlockY()
                - region.getMinimumPoint().getBlockY()) + 1;
        int width = Math.abs(region.getMaximumPoint().getBlockZ()
                - region.getMinimumPoint().getBlockZ()) + 1;
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < width; z++) {
                    if (!world.isChunkLoaded(region.getMinimumPoint()
                            .getBlockX() + x >> 4, region.getMinimumPoint()
                            .getBlockZ() + z >> 4)) {
                        throw new ChunkNotLoadedException();
                    }
                    final BaseBlock block = BukkitUtil
                            .getLocalWorld(world).getBlock(
                                    new Vector(region.getMinimumPoint()
                                            .getBlockX() + x, region
                                            .getMinimumPoint().getBlockY() + y,
                                            region.getMinimumPoint()
                                                    .getBlockZ() + z));
                    clipboard.setBlock(new Vector(x, y, z), block);
                }
            }
        }

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

        CuboidClipboard clipboard = new CuboidClipboard(
                selection.getNativeMaximumPoint()
                        .subtract(selection.getNativeMinimumPoint())
                        .add(1, 1, 1));
        int length = Math.abs(selection.getMaximumPoint().getBlockX()
                - selection.getMinimumPoint().getBlockX()) + 1;
        int height = Math.abs(selection.getMaximumPoint().getBlockY()
                - selection.getMinimumPoint().getBlockY()) + 1;
        int width = Math.abs(selection.getMaximumPoint().getBlockZ()
                - selection.getMinimumPoint().getBlockZ()) + 1;
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < width; z++) {
                    if (!selection.getWorld().isChunkLoaded(selection.getMinimumPoint()
                            .getBlockX() + x >> 4, selection.getMinimumPoint()
                            .getBlockZ() + z >> 4)) {
                        throw new ChunkNotLoadedException();
                    }
                    final BaseBlock block = BukkitUtil
                            .getLocalWorld(blueprint.getWorld()).getBlock(
                                    new Vector(selection.getMinimumPoint()
                                            .getBlockX() + x, selection
                                            .getMinimumPoint().getBlockY() + y,
                                            selection.getMinimumPoint()
                                                    .getBlockZ() + z));
                    clipboard.setBlock(new Vector(x, y, z), block);
                }
            }
        }
        SchematicFormat.MCEDIT.save(clipboard, blueprint.getBlueprintFile());
    }

    /**
     * Looks for signs with [Verkauf] written on the recently reset region.
     * If there is one, things will be written on it.
     *
     * @param sender a player
     * @param region the region that shall be inspected
     * @param world  the world the region is in
     */
    public static void buildRegionConomySign(final Player sender, final ProtectedRegion region, final World world) {

        RegionReset.getInstance().getServer().getScheduler().runTask(RegionReset.getInstance(), new Runnable() {
            @Override
            public void run() {
                Sign sign = null;
                for (int x = region.getMinimumPoint().getBlockX(); x <= region.getMaximumPoint().getBlockX(); x++) {
                    for (int y = region.getMinimumPoint().getBlockY(); y <= region.getMaximumPoint().getBlockY(); y++) {
                        for (int z = region.getMinimumPoint().getBlockZ(); z <= region.getMaximumPoint().getBlockZ(); z++) {
                            if (world.getBlockAt(x, y, z).getType() == Material.SIGN_POST
                                    || world.getBlockAt(x, y, z).getType() == Material.WALL_SIGN) {
                                Sign tempSign = (Sign) world.getBlockAt(x, y, z).getState();
                                String signSellLine = RegionReset.getInstance().getRegionConomy().getConf().getSignSell();
                                if (tempSign.getLines()[0].equals(signSellLine)) {
                                    sign = tempSign;
                                }

                            }
                        }
                    }
                }
                if (sign != null) {
                    MbRegionConomy regionConomy = RegionReset.getInstance().getRegionConomy();
                    if (regionConomy.getRegionDatabase().regionExists(world.getName(),
                            region.getId())) {
                        regionConomy.getRegions().sale(sender, region.getId(),
                                regionConomy.getRegionDatabase().getPrice(sender.getWorld().getName(), region.getId()));
                        sign.setLine(0, regionConomy.getConf().getSignSell());
                        sign.setLine(1, region.getId());
                        sign.setLine(2, Double.toString(regionConomy.getRegionDatabase().getPrice(world.getName(), region.getId())));
                        sign.setLine(3, regionConomy.getRegionDatabase().getPermission(world.getName(), region.getId()));
                        sign.update();
                    } else {
                        sign.setLine(0, "");
                        sign.setLine(1, "");
                        sign.setLine(2, "");
                        sign.setLine(3, "");
                        sign.update();
                    }
                }
            }
        });
    }
}