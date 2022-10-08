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

import de.timesnake.basic.bukkit.util.world.ExLocation;

public class PathPoint {

    private final ExLocation location;

    private PathPoint nextToBlue;
    private PathPoint nextToRed;

    public PathPoint(ExLocation location) {
        this.location = location;
    }

    public ExLocation getLocation() {
        return location;
    }

    public boolean hasNextToBlue() {
        return this.nextToBlue != null;
    }

    public boolean hasNextToRed() {
        return this.nextToRed != null;
    }

    public PathPoint getNextToBlue() {
        return nextToBlue;
    }

    public PathPoint setNextToBlue(PathPoint nextToBlue) {
        this.nextToBlue = nextToBlue;
        nextToBlue.nextToRed = this;
        return nextToBlue;
    }

    public PathPoint getNextToRed() {
        return nextToRed;
    }

    public PathPoint setNextToRed(PathPoint nextToRed) {
        this.nextToRed = nextToRed;
        nextToRed.nextToBlue = this;
        return nextToRed;
    }

    public boolean contains(ExLocation location) {
        if (location == null) return false;
        if (this.location.equals(location)) return true;
        return this.containsToBlue(location) || this.containsToRed(location);
    }

    private boolean containsToBlue(ExLocation location) {
        if (location == null) return false;
        if (this.location.equals(location)) return true;
        return this.hasNextToBlue() && this.nextToBlue.containsToBlue(location);
    }

    private boolean containsToRed(ExLocation location) {
        if (location == null) return false;
        if (this.location.equals(location)) return true;
        return this.hasNextToRed() && this.nextToRed.containsToRed(location);
    }
}
