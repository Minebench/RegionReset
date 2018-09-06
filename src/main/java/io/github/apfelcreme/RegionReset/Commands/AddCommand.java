package io.github.apfelcreme.RegionReset.Commands;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.apfelcreme.RegionReset.Blueprint;
import io.github.apfelcreme.RegionReset.Exceptions.MissingFileException;
import io.github.apfelcreme.RegionReset.RegionManager;
import io.github.apfelcreme.RegionReset.RegionReset;
import io.github.apfelcreme.RegionReset.RegionResetConfig;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
public class AddCommand implements SubCommand {

    /**
     * executes the command
     *
     * @param sender  the sender
     * @param strings the command args
     */
    public void execute(CommandSender sender, String[] strings) {

        // Fügt eine Region zu einem Blueprint hinzu

        if (sender.hasPermission("RegionReset.add")) {
            if (strings.length > 2) {
                String regionName = strings[1];
                String blueprintName = strings[2].replace(".schematic", "");
    
                World world = null;
                if (sender instanceof Player) {
                    world = ((Player) sender).getWorld();
                } else if (strings.length > 3) {
                    world = RegionReset.getInstance().getServer().getWorld(strings[2]);
                    if (world == null) {
                        RegionReset.sendMessage(sender, RegionResetConfig.getText("error.unknownWorld")
                                .replace("{0}", strings[3]));
                        return;
                    }
                }
    
                if (world == null) {
                    RegionReset.sendMessage(sender, RegionResetConfig.getText("error.wrongUsage")
                            .replace("{0}", "/rr add <Region> <Blueprint> <World>"));
                    return;
                }

                com.sk89q.worldguard.protection.managers.RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(world));
                if (rm != null) {
                    ProtectedRegion region = rm.getRegion(strings[1]);
                    if (region != null) {
                        Blueprint blueprint = RegionManager.getInstance().getBlueprint(blueprintName);
                        if (blueprint != null) {
                            try {
                                RegionManager.getInstance().addRegion(sender, region, blueprint, world);
                                RegionReset.sendMessage(sender, RegionResetConfig.getText("info.add.added")
                                        .replace("{0}", regionName));
                                RegionReset.getInstance().getLogger()
                                        .info("Region '" + region.getId() + "' in World '" + blueprint.getWorld().getName()
                                                + "' has been added to the blueprint '"
                                                + blueprint.getName() + "' by " + sender.getName());
                            } catch (MissingFileException e) {
                                RegionReset.sendMessage(sender, RegionResetConfig.getText("error.unknownBlueprintFile")
                                        .replace("{0}", blueprint.getName()).replace("{1}", e.getFile().getPath()));
                            }
                        } else {
                            RegionReset.sendMessage(sender, RegionResetConfig.getText("error.unknownBlueprint")
                                    .replace("{0}", blueprintName));
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
                        .replace("{0}", "/rr add <Region> <Blueprint>"));
            }
        } else {
            RegionReset.sendMessage(sender, RegionResetConfig.getText("error.noPermission"));
        }

    }
}
