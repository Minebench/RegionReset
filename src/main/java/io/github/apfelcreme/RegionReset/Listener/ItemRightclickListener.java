package io.github.apfelcreme.RegionReset.Listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitWorldConfiguration;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.apfelcreme.RegionReset.Blueprint;
import io.github.apfelcreme.RegionReset.RegionManager;
import io.github.apfelcreme.RegionReset.RegionResetConfig;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;

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
public class ItemRightclickListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private void onItemRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        World world = new BukkitWorld(event.getClickedBlock().getWorld());
        BukkitWorldConfiguration wConf = (BukkitWorldConfiguration) WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world);
        if (event.getItem() != null && event.getItem().getType().getKey().toString().equals(wConf.regionWand)) {
            com.sk89q.worldguard.protection.managers.RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
            if (rm != null) {
                Location l = BukkitAdapter.adapt(event.getClickedBlock().getLocation());
                Set<ProtectedRegion> regions = rm.getApplicableRegions(l.toVector().toBlockPoint()).getRegions();
                for (ProtectedRegion region : regions) {
                    Blueprint blueprint = RegionManager.getInstance().getBlueprint(event.getPlayer().getWorld(), region);
                    if (blueprint != null) {
                        event.getPlayer().sendMessage(RegionResetConfig.getText("info.rightClick")
                                .replace("{0}", region.getId())
                                .replace("{1}", blueprint.getName()));
                    }
                }
            }
        }
    }
}
