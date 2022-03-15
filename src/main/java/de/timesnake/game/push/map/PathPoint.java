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
