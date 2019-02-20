package io.github.apfelcreme.RegionReset;



import io.github.apfelcreme.RegionReset.Commands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
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
public class RegionResetCommand implements CommandExecutor {

    /**
     * onCommand
     *
     * @param sender  the executing Player
     * @param cmd     the command
     * @param label   the label (whatever that is...)
     * @param args    the command line arguments
     * @return false
     */
    public boolean onCommand(final CommandSender sender, Command cmd, String label, final String[] args) {
        SubCommand subCommand = null;
        if (args.length > 0) {
            Operation operation = Operation.getOperation(args[0]);
            if (operation != null) {
                if (operation.isPlayerCommand() && !(sender instanceof Player)) {
                    sender.sendMessage(operation.name().toLowerCase() + " can only be executed by a player!");
                    return true;
                }

                switch (operation) {
                    case ADD:
                        subCommand = new AddCommand();
                        break;
                    case BLUEPRINTS:
                        subCommand = new BlueprintsCommand();
                        break;
                    case CHECK:
                        subCommand = new CheckCommand();
                        break;
                    case CHECKDETAIL:
                        subCommand = new CheckDetailCommand();
                        break;
                    case DEFINE:
                        subCommand = new DefineCommand();
                        break;
                    case DELETE:
                        subCommand = new DeleteCommand();
                        break;
                    case INFO:
                        subCommand = new InfoCommand();
                        break;
                    case LIST:
                        subCommand = new ListCommand();
                        break;
                    case RELOAD:
                        subCommand = new ReloadCommand();
                        break;
                    case RESET:
                        subCommand = new ResetCommand();
                        break;
                    case RESTORE:
                        subCommand = new RestoreCommand();
                        break;
                    case SAVE:
                        subCommand = new SaveCommand();
                        break;
                }
            } else {
                RegionReset.sendMessage(sender, RegionResetConfig.getText("error.unknownCommand")
                        .replace("{0}", args[0]));
            }
        } else {
            subCommand = new HelpCommand();
        }
        if (subCommand != null) {
            subCommand.execute(sender, args);
        }
        return true;
    }

    /**
     * all possible sub-commands for /rr
     *
     * @author Jan
     */
    public enum Operation {
        ADD,
        BLUEPRINTS,
        CHECK,
        CHECKDETAIL,
        DEFINE(true),
        DELETE(true),
        INFO,
        LIST,
        RELOAD,
        RESET,
        RESTORE,
        SAVE;

        private final boolean playerCommand;

        Operation(boolean playerCommand) {
            this.playerCommand = playerCommand;
        }


        Operation() {
            playerCommand = false;
        }

        /**
         * returns the matching operation
         *
         * @param operationString the input
         * @return the matching enum constant or null
         */
        public static Operation getOperation(String operationString) {
            for (Operation operation : Operation.values()) {
                if (operation.name().equalsIgnoreCase(operationString)) {
                    return operation;
                }
            }
            return null;
        }

        public boolean isPlayerCommand() {
            return playerCommand;
        }
    }

}
