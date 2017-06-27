package io.github.apfelcreme.RegionReset.Commands;

import io.github.apfelcreme.RegionReset.Blueprint;
import io.github.apfelcreme.RegionReset.RegionManager;
import io.github.apfelcreme.RegionReset.RegionReset;
import io.github.apfelcreme.RegionReset.RegionResetConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

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
public class CheckDetailCommand implements SubCommand {

    /**
     * executes the command
     *
     * @param commandSender the sender
     * @param strings       the command args
     */
    public void execute(CommandSender commandSender, String[] strings) {
        final long MSPERDAY = 86400000;
        if (commandSender.hasPermission("RegionReset.checkDetail")) {
            if (strings.length > 1) {
                String blueprintName = strings[1];
                Blueprint blueprint = RegionManager.getInstance().getBlueprint(blueprintName);
                if (blueprint != null) {
                    int page = 0;
                    if (strings.length > 2 && RegionReset.isNumeric(strings[2])) {
                        page = Integer.parseInt(strings[2]) - 1;
                    }

                    Integer pageSize = RegionReset.getInstance().getConfig().getInt("pageSize");
                    Integer maxPages = (int) Math.ceil((float) blueprint.getRegions().size() / pageSize);
                    if (page >= maxPages - 1) {
                        page = maxPages - 1;
                    }
                    RegionReset.sendMessage(commandSender, RegionResetConfig.getText("info.checkDetail.header")
                            .replace("{0}", blueprint.getName())
                            .replace("{1}", Integer.toString(RegionReset.getInstance().getConfig().getInt("resetLimit")))
                            .replace("{2}", Integer.toString(page + 1))
                            .replace("{3}", maxPages.toString()));
                    for (int i = page * pageSize; i < (page * pageSize) + pageSize; i++) {
                        if (i < blueprint.getRegions().size() && blueprint.getRegions().size() > 0) {
                            boolean atLeastOneActivePlayer = false;
                            List<UUID> members = new ArrayList<>(blueprint.getRegions().get(i).getOwners().getUniqueIds());
                            members.addAll(new ArrayList<>(blueprint.getRegions().get(i).getMembers().getUniqueIds()));
                            Map<UUID, Long> offlineTimes = new HashMap<>();
                            for (UUID uuid : members) {
                                long lastLogout = 0;
                                if (RegionReset.getInstance().getServer().getOfflinePlayer(uuid) != null) {
                                   lastLogout = RegionReset.getInstance().getServer().getOfflinePlayer(uuid).getLastPlayed();
                                }
                                Long offlineTime = new Date().getTime() - lastLogout;
                                offlineTimes.put(uuid, offlineTime);
                                if (offlineTime < RegionReset.getInstance().getConfig().getLong("resetLimit") * MSPERDAY) {
                                    atLeastOneActivePlayer = true;
                                }
                            }

                            //print the results
                            if (members.size() == 0) {
                                RegionReset.sendMessage(commandSender, RegionResetConfig.getText("info.checkDetail.region3")
                                        .replace("{0}", blueprint.getRegions().get(i).getId()));
                            } else if (!atLeastOneActivePlayer) {
                                RegionReset.sendMessage(commandSender, RegionResetConfig.getText("info.checkDetail.region1")
                                        .replace("{0}", blueprint.getRegions().get(i).getId()));
                            } else {
                                RegionReset.sendMessage(commandSender, RegionResetConfig.getText("info.checkDetail.region2")
                                        .replace("{0}", blueprint.getRegions().get(i).getId()));
                            }
                            for (Map.Entry<UUID, Long> entry : offlineTimes.entrySet()) {
                                String name = RegionReset.getInstance().getNameByUUID(entry.getKey());
                                String offlineTime = RegionReset.formatTimeDifference(entry.getValue());
                                RegionReset.sendMessage(commandSender, RegionResetConfig.getText("info.checkDetail.member")
                                        .replace("{0}", name)
                                        .replace("{1}", offlineTime));
                            }
                        }
                    }
                    RegionReset.sendMessage(commandSender, RegionResetConfig.getText("info.checkDetail.footer"));
                } else {
                    RegionReset.sendMessage(commandSender, RegionResetConfig.getText("error.unknownBlueprint")
                            .replace("{0}", blueprintName));
                }
            } else {
                RegionReset.sendMessage(commandSender, RegionResetConfig.getText("error.wrongUsage")
                        .replace("{0}", "/rr check <Blueprint>"));
            }
        } else {
            RegionReset.sendMessage(commandSender, RegionResetConfig.getText("error.noPermission"));
        }
    }
}
