package io.github.apfelcreme.RegionReset.Commands;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.apfelcreme.RegionReset.Blueprint;
import io.github.apfelcreme.RegionReset.RegionManager;
import io.github.apfelcreme.RegionReset.RegionReset;
import io.github.apfelcreme.RegionReset.RegionResetConfig;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

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
                        page = Integer.parseInt(strings[2]) - 1;
                    }

                    Integer pageSize = RegionReset.getInstance().getConfig().getInt("pageSize");
                    Map<ProtectedRegion, Long> inactiveRegions = new LinkedHashMap<>();
                    for (int i = 0; i < blueprint.getRegions().size()
                            && inactiveRegions.size() < (pageSize * page) + pageSize; i++) {
                        ProtectedRegion region = blueprint.getRegions().get(i);
                        long shortestOfflineTime = Long.MAX_VALUE;
                        boolean atLeastOneActivePlayer = false;
                        List<UUID> members = new ArrayList<>(region.getOwners().getUniqueIds());
                        members.addAll(new ArrayList<>(region.getMembers().getUniqueIds()));
                        for (UUID uuid : members) {
                            long lastLogout = 0;
                            OfflinePlayer player = RegionReset.getInstance().getServer().getOfflinePlayer(uuid);
                            if (player != null) {
                                lastLogout = player.getLastPlayed();
                            }
                            long offlineTime = new Date().getTime() - lastLogout;
                            if (offlineTime < RegionReset.getInstance().getConfig().getLong("resetLimit") * MSPERDAY) {
                                atLeastOneActivePlayer = true;
                                break;
                            } else if (offlineTime < shortestOfflineTime) {
                                shortestOfflineTime = offlineTime;
                            }
                        }
    
                        //add to inactive map
                        if (!members.isEmpty() && !atLeastOneActivePlayer) {
                            inactiveRegions.put(region, shortestOfflineTime);
                        }
                    }
                    
                    Integer maxPages = (int) Math.ceil((float) inactiveRegions.size() / pageSize);
                    if (page >= maxPages - 1) {
                        page = maxPages - 1;
                    }
                    RegionReset.sendConfigMessage(commandSender, "info.check.header",
                            "blueprint", blueprint.getName(),
                            "reset-limit", Integer.toString(RegionReset.getInstance().getConfig().getInt("resetLimit")),
                            "page", Integer.toString(page + 1),
                            "last-page", maxPages.toString());
                    inactiveRegions.entrySet().stream().skip(page * pageSize).limit(pageSize).forEachOrdered(e -> {
                        ProtectedRegion region = e.getKey();
                        RegionReset.sendConfigMessage(commandSender, "info.check.element",
                                "region", region.getId(),
                                "region", region.getId(),
                                "offlinetime", RegionReset.formatTimeDifference(e.getValue()),
                                "owners", region.getOwners().toUserFriendlyString(RegionReset.getInstance().getWorldGuard().getProfileCache()),
                                "members", region.getMembers().toUserFriendlyString(RegionReset.getInstance().getWorldGuard().getProfileCache())
                        );
                    });
                    RegionReset.sendConfigMessage(commandSender, "info.check.footer", "blueprint", blueprint.getName());
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
