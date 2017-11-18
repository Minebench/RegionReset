package io.github.apfelcreme.RegionReset.Commands;

import io.github.apfelcreme.RegionReset.Blueprint;
import io.github.apfelcreme.RegionReset.RegionManager;
import io.github.apfelcreme.RegionReset.RegionReset;
import io.github.apfelcreme.RegionReset.RegionResetConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Copyright (C) 2016 Lord36 aka Apfelcreme
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
public class CheckCommand implements SubCommand {
    
    private final static long MSPERDAY = 86400000;
    /**
     * executes the command
     *
     * @param commandSender the sender
     * @param strings       the command args
     */
    @Override
    public void execute(CommandSender commandSender, String[] strings) {

        if (commandSender.hasPermission("RegionReset.check")) {
            if (strings.length > 1) {
                String blueprintName = strings[1];
                Blueprint blueprint = RegionManager.getInstance().getBlueprint(blueprintName);
                if (blueprint != null) {
                    int page = 0;
                    if (strings.length > 2 && RegionReset.isNumeric(strings[2])) {
                        page = Integer.parseInt(strings[1]) - 1;
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
                            for (UUID uuid : members) {
                                long lastLogout = 0;
                                if (RegionReset.getInstance().getServer().getOfflinePlayer(uuid) != null) {
                                    lastLogout = RegionReset.getInstance().getServer().getOfflinePlayer(uuid).getLastPlayed();
                                }
                                Long offlineTime = new Date().getTime() - lastLogout;
                                if (offlineTime < RegionReset.getInstance().getConfig().getLong("resetLimit") * MSPERDAY) {
                                    atLeastOneActivePlayer = true;
                                }
                            }

                            //print the results
                            if (!members.isEmpty() && !atLeastOneActivePlayer) {
                                RegionReset.sendMessage(commandSender, RegionResetConfig.getText("info.check.element")
                                        .replace("{0}", blueprint.getRegions().get(i).getId())
                                        .replace("{1}", blueprint.getRegions().get(i).getId()));
                            }
                        }
                    }
                    RegionReset.sendMessage(commandSender, RegionResetConfig.getText("info.check.footer"));
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
