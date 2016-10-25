package io.github.apfelcreme.RegionReset;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
public class Blueprint {

    private String name = null;
    private World world = null;
    private File blueprintFile = null;
    private List<ProtectedRegion> regions = null;

    public Blueprint(String name, World world, File blueprintFile, List<ProtectedRegion> regions) {
        this.name = name;
        this.world = world;
        this.blueprintFile = blueprintFile;
        this.regions = regions;
    }

    public Blueprint(String name, World world, File blueprintFile) {
        this.name = name;
        this.world = world;
        this.blueprintFile = blueprintFile;
        this.regions = new ArrayList<>();
    }

    /**
     * returns the blueprint name
     *
     * @return the blueprint name
     */
    public String getName() {
        return name;
    }

    /**
     * returns the world the blueprint is in
     *
     * @return the world the blueprint is in
     */
    public World getWorld() {
        return world;
    }

    /**
     * returns the .schematic file object
     *
     * @return the .schematic file object
     */
    public File getBlueprintFile() {
        return blueprintFile;
    }

    /**
     * returns the list of regions which are assigned to the blueprint
     *
     * @return the list of regions which are assigned to the blueprint
     */
    public List<ProtectedRegion> getRegions() {
        return regions;
    }
}
