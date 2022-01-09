package io.github.apfelcreme.RegionReset.Commands;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.apfelcreme.RegionReset.Blueprint;
import io.github.apfelcreme.RegionReset.RegionManager;
import io.github.apfelcreme.RegionReset.RegionReset;
import io.github.apfelcreme.RegionReset.RegionResetConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

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
    
    static final long MSPERDAY = 86400000;
    
    /**
     * executes the command
     *
     * @param commandSender the sender
     * @param strings       the command args
     */
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender.hasPermission("RegionReset.checkDetail")) {
            if (strings.length > 1) {
                String blueprintName = strings[1];
                Blueprint blueprint = RegionManager.getInstance().getBlueprint(blueprintName);
                if (blueprint != null) {
                    int page = 0;
                    if (strings.length > 2 && RegionReset.isNumeric(strings[2])) {
                        page = Integer.parseInt(strings[2]) - 1;
                    }

                    int pageSize = RegionReset.getInstance().getConfig().getInt("pageSize");
                    int maxPages = (int) Math.ceil((float) blueprint.getRegions().size() / pageSize);
                    if (page >= maxPages - 1) {
                        page = maxPages - 1;
                    }
                    RegionReset.sendConfigMessage(commandSender, "info.checkDetail.header",
                            "blueprint", blueprint.getName(),
                            "reset-limit", String.valueOf(RegionReset.getInstance().getConfig().getInt("resetLimit")),
                            "page", String.valueOf(page + 1),
                            "last-page", String.valueOf(maxPages));
                    int finalPage = page;
                    Bukkit.getScheduler().runTaskAsynchronously(RegionReset.getInstance(), () -> {
                        int resetLimit = RegionReset.getInstance().getConfig().getInt("resetLimit");
                        for (int i = finalPage * pageSize; i < (finalPage * pageSize) + pageSize; i++) {
                            if (i < blueprint.getRegions().size() && blueprint.getRegions().size() > 0) {
                                boolean atLeastOneActivePlayer = false;
                                ProtectedRegion region = blueprint.getRegions().get(i);
                                List<UUID> members = new ArrayList<>(region.getOwners().getUniqueIds());
                                members.addAll(new ArrayList<>(region.getMembers().getUniqueIds()));
                                Map<UUID, Long> offlineTimes = new HashMap<>();
                                for (UUID uuid : members) {
                                    long lastLogout = RegionReset.getInstance().getLastSeen(uuid);
                                    long offlineTime = new Date().getTime() - lastLogout;
                                    offlineTimes.put(uuid, offlineTime);
                                    if (offlineTime < resetLimit * MSPERDAY) {
                                        atLeastOneActivePlayer = true;
                                    }
                                }

                                //print the results
                                String messageKey = "info.checkDetail.region2";
                                if (members.size() == 0) {
                                    messageKey = "info.checkDetail.region3";
                                } else if (!atLeastOneActivePlayer) {
                                    messageKey = "info.checkDetail.region1";
                                }
                                RegionReset.sendConfigMessage(commandSender, messageKey,
                                        "region", region.getId());
                                for (Map.Entry<UUID, Long> entry : offlineTimes.entrySet()) {
                                    String name = RegionReset.getInstance().getNameByUUID(entry.getKey());
                                    String offlineTime = RegionReset.formatTimeDifference(entry.getValue());
                                    RegionReset.sendConfigMessage(commandSender, "info.checkDetail.member",
                                            "player", name,
                                            "offline-time", offlineTime);
                                }
                            }
                        }
                        RegionReset.sendConfigMessage(commandSender, "info.checkDetail.footer", "blueprint", blueprint.getName());
                    });
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
