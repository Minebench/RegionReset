package io.github.apfelcreme.RegionReset.Commands;

import com.sk89q.worldedit.bukkit.selections.Selection;
import io.github.apfelcreme.RegionReset.Exceptions.NonCuboidSelectionException;
import io.github.apfelcreme.RegionReset.Exceptions.UnknownException;
import io.github.apfelcreme.RegionReset.RegionManager;
import io.github.apfelcreme.RegionReset.RegionReset;
import io.github.apfelcreme.RegionReset.RegionResetConfig;
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
public class DefineCommand implements SubCommand {

    /**
     * executes the command
     *
     * @param commandSender the sender
     * @param strings       the command args
     */
    public void execute(CommandSender commandSender, String[] strings) {
        Player sender = (Player) commandSender;
        if (sender.hasPermission("RegionReset.define")) {
            Selection selection = RegionReset.getInstance().getWorldEdit().getSelection(sender);
            if (selection != null) {
                if (strings.length > 1) {
                    String blueprintName = strings[1];
                    if (RegionManager.getInstance().getBlueprint(blueprintName) == null) {
                        try {
                            RegionManager.getInstance().defineBlueprint(sender, selection, blueprintName);
                            RegionReset.sendMessage(sender, RegionResetConfig.getText("info.define.defined")
                                    .replace("{0}", blueprintName));
                        } catch (UnknownException e) {
                            RegionReset.sendMessage(sender, RegionResetConfig.getText("error.unknownException")
                                    .replace("{0}", e.getException().getClass().getName()));
                            e.printStackTrace();
                        } catch (NonCuboidSelectionException e) {
                            RegionReset.sendMessage(sender, RegionResetConfig.getText("error.noCuboidSelection"));
                            e.printStackTrace();
                        }
                    } else {
                        RegionReset.sendMessage(sender, RegionResetConfig.getText("error.duplicateBlueprintName")
                                .replace("{0}", blueprintName));
                    }
                } else {
                    RegionReset.sendMessage(sender, RegionResetConfig.getText("error.wrongUsage")
                            .replace("{0}", "/rr define <Blueprint>"));
                }
            } else {
                RegionReset.sendMessage(sender, RegionResetConfig.getText("error.noSelection"));
            }
        } else {
            RegionReset.sendMessage(sender, RegionResetConfig.getText("error.noPermission"));
        }
    }
}
