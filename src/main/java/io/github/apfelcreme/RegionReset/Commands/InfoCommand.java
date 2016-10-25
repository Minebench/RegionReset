package io.github.apfelcreme.RegionReset.Commands;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.apfelcreme.RegionReset.Blueprint;
import io.github.apfelcreme.RegionReset.RegionManager;
import io.github.apfelcreme.RegionReset.RegionReset;
import io.github.apfelcreme.RegionReset.RegionResetConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Date;
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
public class InfoCommand implements SubCommand {

    /**
     * executes the command
     *
     * @param commandSender the sender
     * @param strings       the command args
     */
    public void execute(CommandSender commandSender, String[] strings) {
        Player sender = (Player) commandSender;
        if (sender.hasPermission("RegionReset.info")) {
            if (strings.length >= 2) {
                String regionName = strings[1];
                ProtectedRegion region = RegionReset.getInstance().getWorldGuard().getRegionManager(sender.getWorld()).getRegion(regionName);
                if (region != null) {
                    Blueprint blueprint = RegionManager.getInstance().getBlueprint(region);
                    if (blueprint != null) {
                        RegionReset.sendMessage(sender, RegionResetConfig.getText("info.info.info")
                                .replace("{0}", regionName)
                                .replace("{1}", blueprint.getName()));
                        List<UUID> members = new ArrayList<>(region.getOwners().getUniqueIds());
                        members.addAll(new ArrayList<>(region.getMembers().getUniqueIds()));
                        for (UUID uuid : members) {
                            long lastLogout = 0;
                            if (RegionReset.getInstance().getServer().getOfflinePlayer(uuid) != null) {
                                lastLogout = RegionReset.getInstance().getServer().getOfflinePlayer(uuid).getLastPlayed();
                            }
                            String offlineTime = RegionReset.formatTimeDifference(new Date().getTime() - lastLogout);
                            String name = RegionReset.getInstance().getNameByUUID(uuid);
                            if (name != null) {
                                RegionReset.sendMessage(sender, RegionResetConfig.getText("info.info.member")
                                        .replace("{0}", name)
                                        .replace("{1}", offlineTime));
                            }
                        }
                    } else {
                        RegionReset.sendMessage(sender, RegionResetConfig.getText("error.regionNotAssigned"));
                    }
                } else {
                    RegionReset.sendMessage(sender, RegionResetConfig.getText("error.unknownRegion")
                            .replace("{0}", regionName));
                }
            } else {
                RegionReset.sendMessage(sender, RegionResetConfig.getText("error.wrongUsage")
                        .replace("{0}", "/rr info <Region>"));
            }
        } else {
            RegionReset.sendMessage(sender, RegionResetConfig.getText("error.noPermission"));
        }

    }
}
