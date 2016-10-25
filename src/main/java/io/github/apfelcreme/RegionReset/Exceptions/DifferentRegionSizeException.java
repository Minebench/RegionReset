package io.github.apfelcreme.RegionReset.Exceptions;

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

    private String regionName;
    private String blueprintName;

    public DifferentRegionSizeException(String regionName, String blueprintName) {
        this.regionName = regionName;
        this.blueprintName = blueprintName;
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


}
