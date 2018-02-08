package io.github.apfelcreme.RegionReset.Commands;

import io.github.apfelcreme.RegionReset.Blueprint;
import io.github.apfelcreme.RegionReset.RegionManager;
import io.github.apfelcreme.RegionReset.RegionReset;
import io.github.apfelcreme.RegionReset.RegionResetConfig;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

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
public class BlueprintsCommand implements SubCommand {

    /**
     * executes the command
     *
     * @param commandSender the sender
     * @param strings       the command args
     */
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender.hasPermission("RegionReset.blueprints")) {
            RegionReset.sendMessage(commandSender, RegionResetConfig.getText("info.blueprints.header"));
            for (World world : RegionReset.getInstance().getServer().getWorlds()) {
                List<Blueprint> worldBlueprints = RegionManager.getInstance().getBlueprints(world);
                if (!worldBlueprints.isEmpty()) {
                    RegionReset.sendMessage(commandSender, RegionResetConfig.getText("info.blueprints.worldHeader")
                            .replace("{0}", world.getName()));
                    for (Blueprint blueprint : worldBlueprints) {
                        RegionReset.sendMessage(commandSender, RegionResetConfig.getText("info.blueprints.element")
                                .replace("{0}", blueprint.getName())
                                .replace("{1}", Integer.toString(blueprint.getRegions().size())));
                    }
                }
            }
        } else {
            RegionReset.sendMessage(commandSender, RegionResetConfig.getText("error.noPermission"));
        }
    }
}
