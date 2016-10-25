package io.github.apfelcreme.RegionReset.Commands;

import com.sk89q.worldedit.bukkit.selections.Selection;
import io.github.apfelcreme.RegionReset.Blueprint;
import io.github.apfelcreme.RegionReset.Exceptions.NonCuboidSelectionException;
import io.github.apfelcreme.RegionReset.Exceptions.UnknownException;
import io.github.apfelcreme.RegionReset.RegionManager;
import io.github.apfelcreme.RegionReset.RegionReset;
import io.github.apfelcreme.RegionReset.RegionResetConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
public class DeleteCommand implements SubCommand {

    /**
     * executes the command
     *
     * @param commandSender the sender
     * @param strings       the command args
     */
    @Override
    public void execute(CommandSender commandSender, String[] strings) {

        Player sender = (Player) commandSender;
        if (sender.hasPermission("RegionReset.remove")) {
            if (strings.length > 1) {
                String blueprintName = strings[1];
                Blueprint blueprint = RegionManager.getInstance().getBlueprint(blueprintName);
                if (blueprint != null) {
                    RegionManager.getInstance().deleteBlueprint(sender, blueprint);
                    RegionReset.sendMessage(sender, RegionResetConfig.getText("info.delete.deleted")
                            .replace("{0}", blueprintName));

                } else {
                    RegionReset.sendMessage(sender, RegionResetConfig.getText("error.unknownBlueprint")
                            .replace("{0}", blueprintName));
                }
            } else {
                RegionReset.sendMessage(sender, RegionResetConfig.getText("error.wrongUsage")
                        .replace("{0}", "/rr define <Blueprint>"));
            }
        } else {
            RegionReset.sendMessage(sender, RegionResetConfig.getText("error.noPermission"));
        }
    }
}
