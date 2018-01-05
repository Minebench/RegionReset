package io.github.apfelcreme.RegionReset;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import io.github.apfelcreme.RegionReset.Exceptions.*;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
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
public class RegionManager {

    /**
     * the region manager instance
     */
    private static RegionManager instance = null;

    /**
     * the plugin instance
     */
    private RegionReset plugin;

    /**
     * the list of blueprints
     */
    private List<Blueprint> blueprints = null;

    /**
     * the blueprintConfig that contains the assignments between regions and blueprints
     */
    private FileConfiguration blueprintConfig;

    /**
     * the blueprintConfig-File (default: /plugins/RegionReset/portable
     */
    private File blueprintConfigFile;

    /**
     * constructor
     */
    private RegionManager() {
        this.plugin = RegionReset.getInstance();

        // initialize the region config
        blueprintConfigFile = new File(plugin.getDataFolder() + "/regions.yml");
        blueprintConfig = YamlConfiguration
                .loadConfiguration(blueprintConfigFile);
        saveBlueprintConfig();
        this.blueprints = loadBlueprints();
        plugin.getLogger().info(blueprints.size() + " Blueprints have been loaded");
    }

    public static RegionManager getInstance() {
        if (instance == null) {
            instance = new RegionManager();
        }
        return instance;
    }

    /**
     * loads all blueprints
     *
     * @return a list of all blueprints
     */
    private List<Blueprint> loadBlueprints() {
        ArrayList<Blueprint> blueprints = new ArrayList<>();
        for (String worldName : blueprintConfig.getKeys(true)) {
            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                ConfigurationSection worldConfig = blueprintConfig.getConfigurationSection(worldName);
                for (String blueprintName : worldConfig.getKeys(true)) {
                    List<String> regionNames = worldConfig.getStringList(blueprintName);
                    List<ProtectedRegion> blueprintRegions = new ArrayList<>();
                    File blueprintFile = new File(plugin.getDataFolder() + "/blueprints/"
                            + world.getName() + "/" + blueprintName + ".schematic");
                    for (String regionName : regionNames) {
                        ProtectedRegion region = RegionReset.getInstance().getWorldGuard().getRegionManager(world).getRegion(regionName);
                        if (region != null) {
                            blueprintRegions.add(region);
                        }
                    }
                    blueprints.add(new Blueprint(blueprintName, world, blueprintFile, blueprintRegions));
                }
            }
        }
        return blueprints;
    }

    /**
     * saves the blueprint-config
     */
    public void saveBlueprintConfig() {
        try {
            blueprintConfig.save(blueprintConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * reloads the blueprint-config
     */
    public void reloadBlueprintConfig() {
        blueprintConfig = YamlConfiguration.loadConfiguration(blueprintConfigFile);
        saveBlueprintConfig();
    }

    /**
     * assigns a region to an existing blueprint
     *
     * @param sender    the name of the executing player
     * @param region    the region that shall be added to the blueprint
     * @param blueprint the blueprint the region shall be added to
     * @param world
     * @throws MissingFileException if the file for this blueprint doesn't exist
     */
    public void addRegion(CommandSender sender, ProtectedRegion region, Blueprint blueprint, World world) throws MissingFileException {
        if (!blueprint.getBlueprintFile().exists()) {
            throw new MissingFileException(blueprint.getBlueprintFile());
        }

        // delete multiple entries + the current one
        if (blueprintConfig.getConfigurationSection(world.getName()) == null) {
            blueprintConfig.createSection(world.getName());
            saveBlueprintConfig();
        }
        ConfigurationSection worldConfig = blueprintConfig.getConfigurationSection(world.getName());

        //remove duplicate entry
        for (String key : worldConfig.getKeys(true)) {
            List<String> regions = worldConfig.getStringList(key);
            regions.remove(region.getId());
            worldConfig.set(key, regions);
            saveBlueprintConfig();
        }

        Blueprint oldBlueprint = getBlueprint(world, region);
        if (oldBlueprint != null) {
            oldBlueprint.getRegions().remove(region);
        }

        List<String> regions = worldConfig.getStringList(blueprint.getName());

        regions.add(region.getId());
        blueprintConfig.getConfigurationSection(world.getName()).set(blueprint.getName(),
                regions);

        blueprint.getRegions().add(region);
        saveBlueprintConfig();
    }

    /**
     * restores a region after it has been resetted
     *
     * @param sender the name of the executing player
     * @param region the region that shall be restored
     * @param world
     * @throws DifferentRegionSizeException
     * @throws UnknownException
     * @throws ChunkNotLoadedException
     */
    public void restoreRegion(CommandSender sender, ProtectedRegion region, World world)
            throws DifferentRegionSizeException, UnknownException, ChunkNotLoadedException {
        if (region != null) {
            File restoreSchematic = new File(plugin.getDataFolder() + "/backups/" + world.getName() + "/" + region.getId() + ".schematic");
            if (restoreSchematic.exists()) {
                File informationFile = new File(plugin.getDataFolder() + "/backups/" + world.getName() + "/" + region.getId() + ".yml");
                if (informationFile.exists()) {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(informationFile);
                    List<String> ownerUUIDs = config.getStringList(region.getId() + ".owners");
                    List<String> memberUUIDs = config.getStringList(region.getId() + ".members");
                    for (String ownerUUID : ownerUUIDs) {
                        region.getOwners().addPlayer(UUID.fromString(ownerUUID));
                    }
                    for (String memberUUID : memberUUIDs) {
                        region.getMembers().addPlayer(UUID.fromString(memberUUID));
                    }
                }
                try {
                    SchematicUtils.pasteBlueprint(restoreSchematic, false,
                            region, world);
                } catch (MaxChangedBlocksException e) { // MaxChangedBlocksException shouldn't be happening as the EditSession can paste Integer.MAX_VALUE
                    throw new UnknownException(e);
                }
            }
        }
    }

    /**
     * resets a region to a set blueprint
     *
     * @param sender the executing sender
     * @param region the region that shall be reset
     * @param world  the world the region is in
     * @return the File the backup has been saved into
     * @throws UnknownException
     * @throws DifferentRegionSizeException
     * @throws NonCuboidRegionException
     * @throws ChunkNotLoadedException
     */
    public File resetRegion(CommandSender sender, ProtectedRegion region, World world)
            throws UnknownException, DifferentRegionSizeException, NonCuboidRegionException, ChunkNotLoadedException, MissingFileException {
        Blueprint blueprint = RegionManager.getInstance().getBlueprint(world, region);
        if (blueprint == null) {
            RegionReset.sendMessage(sender, RegionResetConfig.getText("error.regionNotAssigned"));
            return null;
        }
        try {
            if (blueprint.getBlueprintFile().exists()) {
                if (region != null) {
                    if (region.getType() == RegionType.CUBOID) {
                        File backupFile = new File(plugin.getDataFolder()
                                + "/backups/" + world.getName() + "/" + region.getId() + ".schematic");
                        SchematicUtils.saveSchematic(backupFile, region, world);
                        SchematicUtils.pasteBlueprint(blueprint.getBlueprintFile(), false, region, world);
                        if (plugin.getPlotSigns() != null) {
                            SchematicUtils.buildPlotSignsSign(sender, region, world);
                        }
                        SchematicUtils.removeProtections(sender, region, world);
                        region.getOwners().removeAll();
                        region.getMembers().removeAll();
                        plugin.getWorldGuard().getRegionManager(world).save();
                        plugin.getLogger().info("Region '" + region.getId() + "' in World '" + world.getName()
                                + "' has been reset by " + sender.getName());
                        return backupFile;
                    } else {
                        throw new NonCuboidRegionException(region.getId());
                    }
                } else {
                    RegionReset.sendMessage(sender, RegionResetConfig.getText("error.unknownRegion")
                            .replace("{0}", "???"));
                }
            } else {
                throw new MissingFileException(blueprint.getBlueprintFile());
            }
        } catch (DataException | EmptyClipboardException | StorageException | MaxChangedBlocksException e) {
            // MaxChangedBlocksException shouldn't be happening as the EditSession can paste Integer.MAX_VALUE
            throw new UnknownException(e);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * saves a region to a schematic file. Can later be restored by /rr restore <Region>
     *
     * @param sender the player that does the action
     * @param region the region that is the copy base
     * @param world
     * @throws NonCuboidRegionException
     * @throws UnknownException
     * @throws ChunkNotLoadedException
     */
    public void saveRegion(CommandSender sender, ProtectedRegion region, World world)
            throws NonCuboidRegionException, UnknownException, ChunkNotLoadedException {
        if (region != null) {
            File saveFile = new File(plugin.getDataFolder()
                    + "/backups/" + world.getName() + "/" + region.getId() + ".schematic");
            if (region.getType() == RegionType.CUBOID) {
                try {
                    SchematicUtils.saveSchematic(saveFile, region, world);
                } catch (EmptyClipboardException | DataException e) {
                    throw new UnknownException(e);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                throw new NonCuboidRegionException(region.getId());
            }
        }
    }

    /**
     * defines a new Blueprint
     *
     * @param sender        the player that does the action
     * @param selection     the players current WorldEdit selection
     * @param blueprintName the name of the blueprint which is being defined
     * @throws NonCuboidSelectionException the selection is not cuboid
     * @throws UnknownException            something else happened
     * @throws ChunkNotLoadedException
     */
    public void defineBlueprint(Player sender, Selection selection, String blueprintName)
            throws NonCuboidSelectionException, UnknownException, ChunkNotLoadedException {
        if (!blueprintName.endsWith(".schematic")) {
            blueprintName += ".schematic";
        }
        File blueprintFile = new File(plugin
                .getDataFolder() + "/blueprints/" + sender.getWorld().getName() + "/" + blueprintName);
        Blueprint blueprint = new Blueprint(blueprintName.replace(".schematic", ""), sender.getWorld(), blueprintFile);
        try {
            if (selection instanceof CuboidSelection) {
                SchematicUtils.saveBlueprintSchematic(blueprint, selection);

                if (blueprintConfig.getConfigurationSection(sender.getWorld().getName()) == null) {
                    blueprintConfig.createSection(sender.getWorld().getName());
                }
                ConfigurationSection worldConfig = blueprintConfig.getConfigurationSection(sender.getWorld().getName());
                worldConfig.set(blueprint.getName(), new ArrayList<>());
                saveBlueprintConfig();

                blueprints.add(blueprint);
                plugin.getLogger().info("Blueprint '" + blueprintName + "' in World '" + sender.getWorld().getName()
                        + "' has been defined by " + sender.getName() + "'");


            } else {
                throw new NonCuboidSelectionException(selection);
            }
        } catch (DataException e) {
            throw new UnknownException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * deletes a blueprint
     *
     * @param sender    a player
     * @param blueprint the blueprint
     */
    public void deleteBlueprint(Player sender, Blueprint blueprint) {
        if (blueprint != null) {
            ConfigurationSection worldConfig = blueprintConfig.getConfigurationSection(sender.getWorld().getName());
            if (worldConfig != null) {

                // remove the blueprint from regions.yml
                worldConfig.set(blueprint.getName(), null);
                saveBlueprintConfig();

                // remove all files of regions that were assigned to this blueprint
                for (ProtectedRegion region : blueprint.getRegions()) {
                    File schematic = new File(plugin.getDataFolder() + "/backups/" +
                            sender.getWorld().getName() + "/" + region.getId() + ".schematic");
                    if (schematic.exists()) {
                        schematic.delete();
                    }
                    File informationFile = new File(plugin.getDataFolder() + "/backups/" +
                            sender.getWorld().getName() + "/" + region.getId() + ".yml");
                    if (informationFile.exists()) {
                        informationFile.delete();
                    }
                    plugin.getLogger().info("Region-Backup for Region '" + region.getId() + "' in World '"
                            + sender.getWorld().getName() + "' has been deleted by " + sender.getName() + "'");
                }

                // delete the blueprint schematic file
                blueprints.remove(blueprint);
                blueprint.getBlueprintFile().delete();
                plugin.getLogger().info("Blueprint '" + blueprint.getName() + "' in World '"
                        + sender.getWorld().getName() + "' has been deleted by " + sender.getName() + "'");
            }
        }
    }

    /**
     * returns all blueprints on the given world
     *
     * @param world a world
     * @return a list of blueprints
     */
    public List<Blueprint> getBlueprints(World world) {
        List<Blueprint> blueprints = new ArrayList<>();
        ConfigurationSection worldConfig = blueprintConfig.getConfigurationSection(world.getName());
        if (worldConfig != null) {
            for (String blueprintName : worldConfig.getKeys(true)) {
                Blueprint blueprint = getBlueprint(blueprintName);
                if (blueprint != null) {
                    blueprints.add(blueprint);
                }
            }
        }
        return blueprints;
    }

    /**
     * returns the blueprint with the given name
     *
     * @param name the blueprint name
     * @return the blueprint
     */
    public Blueprint getBlueprint(String name) {
        for (Blueprint blueprint : blueprints) {
            if (blueprint.getName().equalsIgnoreCase(name)) {
                return blueprint;
            }
        }
        return null;
    }

    /**
     * returns the blueprint the region is assigned to
     *
     * @param region a worldguard region
     * @return the blueprint the region is assigned to
     */
    public Blueprint getBlueprint(World world, ProtectedRegion region) {
        for (Blueprint blueprint : blueprints) {
            if (blueprint.getWorld() == world && blueprint.getRegions().contains(region)) {
                return blueprint;
            }
        }
        return null;
    }


}