package de.timesnake.game.push.map;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.entities.EntityManager;
import de.timesnake.basic.entities.entity.bukkit.ExZombie;
import de.timesnake.basic.entities.pathfinder.ExPathfinderGoalUpdatedLocation;
import de.timesnake.basic.entities.pathfinder.PathfinderGoalUpdatedLocation;
import de.timesnake.game.push.main.GamePush;
import de.timesnake.game.push.server.PushServer;
import de.timesnake.game.push.user.PushUser;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.reflection.wrapper.ExEnumItemSlot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class EscortManager {

    private static final double RADIUS = 5;

    private ExZombie zombie;
    private PathPoint currentPathPoint;
    private boolean lastDirectionBlue;

    private BukkitTask moveTask;

    public EscortManager() {

    }

    public void start() {
        for (Villager villager : PushServer.getMap().getWorld().getEntitiesByClass(Villager.class)) {
            villager.remove();
        }

        this.spawn();
        this.runMoveTask();
    }

    public void stop() {
        if (this.moveTask != null) {
            this.moveTask.cancel();
        }

        if (this.zombie != null) {
            this.zombie.remove();
        }
    }

    public void spawn() {
        PushMap map = PushServer.getMap();

        this.currentPathPoint = map.getSpawnPoint();

        if (this.zombie != null) {
            this.zombie.remove();
        }

        this.zombie = new ExZombie(map.getWorld().getBukkitWorld(), false);

        this.zombie.setInvulnerable(true);
        this.zombie.setPersistent(true);

        this.zombie.setSlot(ExEnumItemSlot.HEAD, new ItemStack(Material.GOLDEN_HELMET));

        this.zombie.addPathfinderGoal(1, new ExPathfinderGoalUpdatedLocation(new PathfinderGoalUpdatedLocation(null, 0.4, 32, 0.2) {
            @Override
            public Location getNextLocation(Location entityLoc) {
                return EscortManager.this.currentPathPoint.getLocation().toBlockLocation();
            }
        }));


        ExLocation spawn = map.getZombieSpawn();
        this.zombie.setPosition(spawn.getX(), spawn.getY(), spawn.getZ());

        EntityManager.spawnEntity(map.getWorld().getBukkitWorld(), this.zombie);
    }

    public void runMoveTask() {
        this.moveTask = Server.runTaskTimerSynchrony(() -> {
            int countBlue = 0;
            int countRed = 0;

            for (Player player : this.zombie.getLocation().getNearbyPlayers(RADIUS)) {
                PushUser user = (PushUser) Server.getUser(player);
                if (user.getTeam() == null || !user.getStatus().equals(Status.User.IN_GAME)) {
                    continue;
                }

                if (user.getTeam().equals(PushServer.getGame().getBlueTeam())) {
                    countBlue++;
                } else if (user.getTeam().equals(PushServer.getGame().getRedTeam())) {
                    countRed++;
                }
            }

            if (countBlue > countRed) {
                this.moveTo(true);
            } else if (countRed > countBlue) {
                this.moveTo(false);
            }

        }, 0, 10, GamePush.getPlugin());
    }

    private void moveTo(boolean blue) {
        double distance = this.zombie.getLocation().distance(this.currentPathPoint.getLocation().toBlockLocation());

        if (distance > 2 && this.lastDirectionBlue == blue) {
            return;
        }

        this.lastDirectionBlue = blue;

        PathPoint next;
        if (blue) {
            next = this.currentPathPoint.getNextToBlue();
        } else {
            next = this.currentPathPoint.getNextToRed();
        }

        if (next == null) {
            if (distance < 1.2) {
                PushServer.onFinishReached(blue);
                return;
            }

            return;
        }

        this.currentPathPoint = next;
    }

}
