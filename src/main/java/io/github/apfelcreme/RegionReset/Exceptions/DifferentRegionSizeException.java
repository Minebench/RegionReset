package io.github.apfelcreme.RegionReset.Exceptions;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.util.BlockVector;

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
public class DifferentRegionSizeException extends Throwable {

    private final String regionName;
    private final String blueprintName;
    private final BlockVector3 regionSize;
    private final BlockVector3 blueprintSize;

    public DifferentRegionSizeException(String regionName, String blueprintName, BlockVector3 regionSize, BlockVector3 blueprintSize) {
        this.regionName = regionName;
        this.blueprintName = blueprintName;
        this.regionSize = regionSize;
        this.blueprintSize = blueprintSize;
    }

    /**
     * returns the region name
     *
     * @return the region name
     */
    public String getRegionName() {
        return regionName;
    }

    /**
     * returns the blueprint name
     *
     * @return the blueprint name
     */
    public String getBlueprintName() {
        return blueprintName;
    }

    /**
     * returns the region size in the width, height and depth order
     *
     * @return the region size
     */
    public BlockVector3 getRegionSize() {
        return regionSize;
    }

    /**
     * returns the blueprint size in the width, height and depth order
     *
     * @return the blueprint size
     */
    public BlockVector3 getBlueprintSize() {
        return blueprintSize;
    }
}
