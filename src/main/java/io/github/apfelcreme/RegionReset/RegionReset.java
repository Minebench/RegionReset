package io.github.apfelcreme.RegionReset;

import com.griefcraft.lwc.LWC;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.util.profile.Profile;
import de.minebench.plotsigns.PlotSigns;
import de.themoep.minedown.MineDown;
import io.github.apfelcreme.RegionReset.Listener.ItemRightclickListener;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.zaiyers.UUIDDB.core.UUIDDBPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

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
public class RegionReset extends JavaPlugin {

    /**
     * a cache for uuid -> uuid
     */
    private Map<UUID, String> uuidCache = null;

    /**
     * directly store reference to UUIDDB plugin instead of always getting the instance
     */
    private UUIDDBPlugin uuidDb = null;

    /**
     * the plugin instance
     */
    private static RegionReset instance = null;

    /**
     * PlotSigns plugin instance
     */
    private PlotSigns plotSigns = null;

    /**
     * LWC plugin instance
     */
    private LWC lwc;

    /**
     * onEnable
     */
    public void onEnable() {

        instance = this;

        // initialize the uuid cache
        uuidCache = new HashMap<>();
        if (getServer().getPluginManager().isPluginEnabled("UUIDDB")) {
            uuidDb = (UUIDDBPlugin) getServer().getPluginManager()
                    .getPlugin("UUIDDB");
        }

        // create the data folder
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
            new File(getDataFolder(), "backups").mkdirs();
            new File(getDataFolder(), "blueprints").mkdirs();
            saveDefaultConfig();
        }

        if (getServer().getPluginManager().isPluginEnabled("PlotSigns")) {
            plotSigns = (PlotSigns) getServer().getPluginManager().getPlugin("PlotSigns");
            getLogger().info("Plugin PlotSigns found!");
        }
        if (getServer().getPluginManager().isPluginEnabled("LWC")) {
            lwc = LWC.getInstance();
            getLogger().info("Plugin LWC found!");
        }

        // initialize the rightclick listener
        getServer().getPluginManager().registerEvents(new ItemRightclickListener(), this);

        // initialize the language config
        RegionResetConfig.load();

        // command
        getServer().getPluginCommand("rr").setExecutor(
                new RegionResetCommand());
    }

    /**
     * onDisable
     */
    public void onDisable() {
    }

    /**
     * sends a message to a player
     *
     * @param player  the player the message shall be sent to
     * @param message the message
     */
    public static void sendMessage(CommandSender player, String message) {
        sendMessage(player, MineDown.parse(message));
    }
    
    /**
     * sends a message to a player
     *
     * @param player  the player the message shall be sent to
     * @param message the message
     */
    public static void sendMessage(CommandSender player, BaseComponent[] message) {
        player.spigot().sendMessage(
                new ComponentBuilder("")
                        .append(RegionResetConfig.getComponents("prefix"))
                        .append(message).create());
    }
    
    /**
     * sends a message to a player
     *
     * @param player  the player the message shall be sent to
     * @param key     the messages's config key
     */
    public static void sendConfigMessage(CommandSender player, String key, String... replacements) {
        sendMessage(player, RegionResetConfig.getComponents(key, replacements));
    }

    /**
     * returns the plugin instance
     *
     * @return the plugin instance
     */
    public static RegionReset getInstance() {
        return instance;
    }

    /**
     * returns the PlotSigns plugin instance
     * @return the PlotSigns plugin instance
     */
    public PlotSigns getPlotSigns() {
        return plotSigns;
    }

    /**
     * returns the LWC  plugin instance
     * @return the LWC plugin instance
     */
    public LWC getLWC() {
        return lwc;
    }


    /**
     * formats a given unix-timestamp into Days, Hours, Minutes and Seconds
     *
     * @param time unix timestamp
     * @return a formatted String
     */
    public static String formatTimeDifference(long time) {
        long diffSeconds = time / 1000 % 60;
        long diffMinutes = time / (60 * 1000) % 60;
        long diffHours = time / (60 * 60 * 1000) % 24;
        long diffDays = time / (24 * 60 * 60 * 1000);
        return diffDays + "Tage " + diffHours + "Std " + diffMinutes + "Min " + diffSeconds + "Sek";
    }

    /**
     * returns the username of a player with the given uuid
     *
     * @param uuid a players uuid
     * @return his name
     */
    public String getNameByUUID(UUID uuid) {
        if (uuid.equals(new UUID(0, 0))) {
            return "[Console]";
        }
        String name = null;
        if (uuidDb != null) {
            name = uuidDb.getStorage().getNameByUUID(uuid);
        }
        if (name == null) {
            Profile profile = WorldGuard.getInstance().getProfileCache().getIfPresent(uuid);
            if (profile != null) {
                name = profile.getName();
            }
        }
        if (name == null) {
            name = uuidCache.get(uuid);
        }

        if (name == null) {
            //this should only occur if the player has never joined
            try {
                URL url = new URL(RegionResetConfig.getUUIDToNameUrl().replace("{0}", uuid.toString().replace("-", "")));
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder json = new StringBuilder();
                int read;
                while ((read = in.read()) != -1) {
                    json.append((char) read);
                }
                Object obj = new JSONParser().parse(json.toString());
                JSONArray jsonArray = (JSONArray) obj;
                name = (String) ((JSONObject) jsonArray.get(jsonArray.size() - 1)).get("name");
                if (uuidDb != null) {
                    uuidDb.getStorage().insert(uuid, name);
                } else {
                    uuidCache.put(uuid, name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return name != null ? name : "Unknown Player";
    }

    /**
     * checks if a String contains only numbers
     *
     * @param string a string
     * @return true or false
     */
    public static boolean isNumeric(String string) {
        return Pattern.matches("([0-9])*", string);
    }
}
