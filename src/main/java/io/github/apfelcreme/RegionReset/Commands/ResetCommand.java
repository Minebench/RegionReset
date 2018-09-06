package io.github.apfelcreme.RegionReset.Commands;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.apfelcreme.RegionReset.Blueprint;
import io.github.apfelcreme.RegionReset.Exceptions.*;
import io.github.apfelcreme.RegionReset.RegionManager;
import io.github.apfelcreme.RegionReset.RegionReset;
import io.github.apfelcreme.RegionReset.RegionResetConfig;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
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
public class ResetCommand implements SubCommand {

    /**
     * executes the command
     *
     * @param sender  the sender
     * @param strings the command args
     */
    public void execute(CommandSender sender, String[] strings) {

        // resets a region

        if (sender.hasPermission("RegionReset.reset")) {
            if (strings.length > 1) {
                String regionName = strings[1];
                
                World world = null;
                if (sender instanceof Player) {
                    world = ((Player) sender).getWorld();
                } else if (strings.length > 2) {
                    world = RegionReset.getInstance().getServer().getWorld(strings[2]);
                    if (world == null) {
                        RegionReset.sendMessage(sender, RegionResetConfig.getText("error.unknownWorld")
                                .replace("{0}", strings[2]));
                        return;
                    }
                }
                
                if (world == null) {
                    RegionReset.sendMessage(sender, RegionResetConfig.getText("error.wrongUsage")
                            .replace("{0}", "/rr reset <Region> <World>"));
                    return;
                }

                com.sk89q.worldguard.protection.managers.RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(world));
                if (rm != null) {
                    ProtectedRegion region = rm.getRegion(strings[1]);
                    if (region != null) {
                        Blueprint blueprint = RegionManager.getInstance().getBlueprint(world, region);
                        if (blueprint != null) {
                            try {
                                File backupFile = RegionManager.getInstance().resetRegion(sender, region, world);
                                if (backupFile != null) {
                                    RegionReset.sendMessage(sender, RegionResetConfig.getText("info.reset.reset")
                                            .replace("{0}", regionName)
                                            .replace("{1}", backupFile.getName()));
                                }
                            } catch (UnknownException e) {
                                RegionReset.sendMessage(sender, RegionResetConfig.getText("error.unknownException")
                                        .replace("{0}", e.getException().getClass().getName()));
                                RegionReset.getInstance().getLogger().log(Level.SEVERE, e.getException().getMessage(), e.getException());
                            } catch (DifferentRegionSizeException e) {
                                RegionReset.sendMessage(sender, RegionResetConfig.getText("error.differentSize")
                                        .replace("{0}", e.getRegionName())
                                        .replace("{1}", e.getBlueprintName()));
                            } catch (NonCuboidRegionException e) {
                                RegionReset.sendMessage(sender, RegionResetConfig.getText("error.noCuboidRegion"));
                            } catch (ChunkNotLoadedException e) {
                                RegionReset.sendMessage(sender, RegionResetConfig.getText("error.chunkNotLoaded"));
                            } catch (MissingFileException e) {
                                RegionReset.sendMessage(sender, RegionResetConfig.getText("error.unknownBlueprintFile")
                                        .replace("{0}", blueprint.getName()).replace("{1}", e.getFile().getPath()));
                            }
                        } else {
                            RegionReset.sendMessage(sender, RegionResetConfig.getText("error.regionNotAssigned"));
                        }
                    } else {
                        RegionReset.sendMessage(sender, RegionResetConfig.getText("error.unknownRegion")
                                .replace("{0}", regionName));
                    }
                } else {
                    RegionReset.sendMessage(sender, RegionResetConfig.getText("error.noRegionContainer")
                            .replace("{0}", world.getName()));
                }
            } else {
                RegionReset.sendMessage(sender, RegionResetConfig.getText("error.wrongUsage")
                        .replace("{0}", "/rr reset <Region> [<World>]"));
            }
        } else {
            RegionReset.sendMessage(sender, RegionResetConfig.getText("error.noPermission"));
        }

    }
}
