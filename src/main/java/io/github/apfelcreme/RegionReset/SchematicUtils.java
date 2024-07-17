package io.github.apfelcreme.RegionReset;

import com.griefcraft.lwc.LWC;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.minebench.plotsigns.PlotSigns;
import io.github.apfelcreme.RegionReset.Exceptions.ChunkNotLoadedException;
import io.github.apfelcreme.RegionReset.Exceptions.DifferentRegionSizeException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

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
        try (Closer closer = Closer.create()) {
            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(world), Integer.MAX_VALUE);
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                RegionReset.getInstance().getLogger().log(Level.SEVERE, "Could not determinate schematic format of file " + schematicFile.getPath());
                return;
            }
            FileInputStream fis = closer.register(new FileInputStream(schematicFile));
            BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
            ClipboardReader reader = closer.register(format.getReader(bis));

            Clipboard clipboard = reader.read();

            int length = Math.abs(region.getMaximumPoint().x() - region.getMinimumPoint().x()) + 1;
            int height = Math.abs(region.getMaximumPoint().y() - region.getMinimumPoint().y()) + 1;
            int width = Math.abs(region.getMaximumPoint().z() - region.getMinimumPoint().z()) + 1;

            if (width != clipboard.getDimensions().x() || height != clipboard.getDimensions().y() || length != clipboard.getDimensions().z()) {
                throw new DifferentRegionSizeException(region.getId(), schematicFile.getName());
            }
            
            loadChunks(world, region);

            LocalSession session = new LocalSession();
            session.setClipboard(new ClipboardHolder(clipboard));
            Operation operation = session.getClipboard().createPaste(editSession).to(region.getMinimumPoint()).build();
            Operations.complete(operation);
            editSession.flushSession();
        } catch (IOException | WorldEditException e) {
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
     * @throws ChunkNotLoadedException
     */
    public static void saveSchematic(File schematicFile, ProtectedRegion region, World world)
            throws IOException, WorldEditException, ChunkNotLoadedException {

        if (world == null) {
            return;
        }
        if (!schematicFile.exists()) {
            schematicFile.getParentFile().mkdirs();
        }

        loadChunks(world, region);
        
        // save the schematic
        CuboidRegion cuboidRegion = new CuboidRegion(
                BukkitAdapter.adapt(world),
                region.getMinimumPoint(),
                region.getMaximumPoint()
                        .subtract(region.getMinimumPoint())
                        .add(1, 1, 1)
        );
        writeToSchematic(schematicFile, cuboidRegion);

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
     * Load all chunks
     *
     * @param world The world the chunks are in
     * @param minX The x coordinate of the minimum location
     * @param minZ The z coordinate of the minimum location
     * @param maxX The x coordinate of the maximum location
     * @param maxZ The z coordinate of the maximum location
     * @throws ChunkNotLoadedException
     */
    private static void loadChunks(World world, int minX, int minZ, int maxX, int maxZ) throws ChunkNotLoadedException {
        // Check if chunks are loaded
        for (int x = minX >> 4; x <= maxX >> 4; x++) {
            for (int z = minZ >> 4; z <= maxZ >> 4; z++) {
                if (!world.isChunkLoaded(x, z)) {
                    if (!world.loadChunk(x, z, false)) {
                        throw new ChunkNotLoadedException();
                    }
                }
            }
        }
    }
    
    private static void loadChunks(World world, ProtectedRegion region) throws ChunkNotLoadedException {
        loadChunks(world,
                region.getMinimumPoint().x(), region.getMinimumPoint().z(),
                region.getMaximumPoint().x(), region.getMaximumPoint().z());
    }
    
    private static void loadChunks(Region selection) throws ChunkNotLoadedException {
        if (selection.getWorld() != null) {
            loadChunks(BukkitAdapter.adapt(selection.getWorld()),
                    selection.getMinimumPoint().x(), selection.getMinimumPoint().z(),
                    selection.getMaximumPoint().x(), selection.getMaximumPoint().z());
        }
    }

    /**
     * saves a selection as a blueprint
     *
     * @param blueprint the blueprint that shall be saved
     * @param selection the selection
     * @throws IOException
     * @throws ChunkNotLoadedException
     */
    public static void saveBlueprintSchematic(Blueprint blueprint, Region selection)
            throws IOException, ChunkNotLoadedException, WorldEditException {

        if (selection == null || selection.getWorld() == null) {
            return;
        }
        if (!blueprint.getBlueprintFile().exists()) {
            blueprint.getBlueprintFile().getParentFile().mkdirs();
        }

        loadChunks(selection);

        writeToSchematic(blueprint.getBlueprintFile(), selection);
    }

    private static void writeToSchematic(File schematicFile, Region cuboidRegion) throws WorldEditException, IOException {
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(cuboidRegion.getWorld(), -1);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(cuboidRegion);
        clipboard.setOrigin(cuboidRegion.getMinimumPoint());
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, cuboidRegion, clipboard, cuboidRegion.getMinimumPoint());

        Operations.complete(copy);

        try (Closer closer = Closer.create()) {
            // Create parent directories
            File parent = schematicFile.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new IOException("Could not create folder for schematics!");
                }
            }

            FileOutputStream fos = closer.register(new FileOutputStream(schematicFile));
            BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
            ClipboardWriter writer = closer.register(BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(bos));
            writer.write(clipboard);
        }
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

        Bukkit.getScheduler().runTask(RegionReset.getInstance(), () -> {
            List<Sign> signs = new ArrayList<>();

            String signSellLine = RegionReset.getInstance().getPlotSigns().getSellLine();
            for (int x = region.getMinimumPoint().x(); x <= region.getMaximumPoint().x(); x++) {
                for (int y = region.getMinimumPoint().y(); y <= region.getMaximumPoint().y(); y++) {
                    for (int z = region.getMinimumPoint().z(); z <= region.getMaximumPoint().z(); z++) {
                        Block block = world.getBlockAt(x, y, z);
                        BlockData data = block.getBlockData();
                        if (data instanceof org.bukkit.block.data.type.Sign || data instanceof WallSign) {
                            Sign tempSign = (Sign) world.getBlockAt(x, y, z).getState();
                            if (tempSign.getSide(Side.FRONT).getLines()[0].equalsIgnoreCase(signSellLine)) {
                                signs.add(tempSign);
                            }
                        }
                    }
                }
            }
            if (!signs.isEmpty()) {
                if (RegionReset.getInstance().getPlotSigns() != null && region.getFlag(PlotSigns.BUYABLE_FLAG) != null && region.getFlag(PlotSigns.PRICE_FLAG) != null) {
                    PlotSigns plotSigns = RegionReset.getInstance().getPlotSigns();
                    region.setFlag(PlotSigns.BUYABLE_FLAG, true);
                    String[] lines = plotSigns.getSignLines(region);
                    for (Sign sign : signs) {
                        for (int i = 0; i < lines.length; i++) {
                            sign.getSide(Side.FRONT).setLine(i, lines[i]);
                        }
                        sign.update();
                    }
                } else {
                    for (Sign sign : signs) {
                        for (int i = 0; i < 4; i++) {
                            sign.getSide(Side.FRONT).setLine(i, "");
                        }
                        sign.update(true, false);
                    }
                }
            }
        });
    }

    public static void removeProtections(CommandSender sender, ProtectedRegion region, World world) {
        if (RegionReset.getInstance().getLWC() != null) {
            Bukkit.getScheduler().runTaskAsynchronously(RegionReset.getInstance(), () -> {
                LWC.getInstance().fastRemoveProtections(sender,
                        "world = '" + world.getName() + "'"
                                + " AND x >= " + region.getMinimumPoint().x()
                                + " AND y >= " + region.getMinimumPoint().y()
                                + " AND z >= " + region.getMinimumPoint().z()
                                + " AND x <= " + region.getMaximumPoint().x()
                                + " AND y <= " + region.getMaximumPoint().y()
                                + " AND z <= " + region.getMaximumPoint().z()
                        , false);
            });
        }
    }

    public static void removeEntities(CommandSender sender, ProtectedRegion region, World world) {
        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof Player) && region.contains(entity.getLocation().getBlockX(), entity.getLocation().getBlockY(), entity.getLocation().getBlockZ())) {
                try {
                    entity.remove();
                } catch (UnsupportedOperationException e) {
                    sender.sendMessage("Can't remove entity " + entity.getType() + " at " + entity.getLocation().toBlockLocation().toVector() + ": " + e.getMessage());
                }
            }
        }
    }
}