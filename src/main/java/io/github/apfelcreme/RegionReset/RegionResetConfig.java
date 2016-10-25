package io.github.apfelcreme.RegionReset;

import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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
public class RegionResetConfig {

    private static YamlConfiguration languageConfig;

    private static RegionReset plugin;

    /**
     * loads the config
     */
    public static void load() {
        plugin = RegionReset.getInstance();
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        plugin.saveDefaultConfig();
        plugin.saveResource("lang.de.yml", false);

        languageConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + "/lang.de.yml"));
    }

    /**
     * returns the config
     *
     * @return the config
     */
    public static YamlConfiguration getLanguageConfig() {
        return languageConfig;
    }


    /**
     * returns a texty string
     *
     * @param key the config path
     * @return the text
     */
    public static String getText(String key) {
        String ret = (String) languageConfig.get("texts." + key);
        if (ret != null && !ret.isEmpty()) {
            ret = ChatColor.translateAlternateColorCodes('&', ret);
            return ChatColor.translateAlternateColorCodes('§', ret);
        } else {
            return "Missing text node: " + key;
        }
    }

    /**
     * returns the url that is used to call the mojang api to get a players name
     *
     * @return the url that is used to call the mojang api to get a players name
     */
    public static String getUUIDToNameUrl() {
        return plugin.getConfig().getString("uuidToNameUrl");
    }
}
