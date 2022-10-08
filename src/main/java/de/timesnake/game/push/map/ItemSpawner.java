/*
 * game-push.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.game.push.map;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.game.push.main.GamePush;
import de.timesnake.game.push.user.PushKit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class ItemSpawner {

    private static final List<ExItemStack> ITEMS = List.of(new ExItemStack(Material.GOLDEN_APPLE), PushKit.TNT,
            PushKit.FOOD.cloneWithId().asQuantity(3), new ExItemStack(Material.ENDER_PEARL));

    private static final int DELAY_BASE = 10; // sec * 2
    private static final int DELAY_RANGE = 10; // sec * 2

    private final ExLocation location;
    private BukkitTask task;

    private Random random = new Random();
    private int delay = 15;

    public ItemSpawner(ExLocation location) {
        this.location = location;
    }

    public void start() {
        this.task = Server.runTaskTimerSynchrony(() -> {
            delay--;

            if (delay <= 0) {
                this.location.getWorld().dropItem(this.location, ITEMS.get(this.random.nextInt(ITEMS.size())));
                this.delay = this.random.nextInt(DELAY_RANGE) + DELAY_BASE;
            }
        }, 20, 40, GamePush.getPlugin());
    }

    public void stop() {
        if (this.task != null) {
            this.task.cancel();
        }
    }

}
