package de.timesnake.game.push.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserBlockPlaceEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDeathEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDropItemEvent;
import de.timesnake.basic.bukkit.util.user.event.UserRespawnEvent;
import de.timesnake.game.push.main.GamePush;
import de.timesnake.game.push.server.PushServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class UserManager implements Listener {

    public UserManager() {
        Server.registerListener(this, GamePush.getPlugin());
    }

    @EventHandler
    public void onUserDeath(UserDeathEvent e) {
        e.setAutoRespawn(true);
        e.getDrops().clear();
        e.setKeepInventory(false);
    }

    @EventHandler
    public void onUserRespawn(UserRespawnEvent e) {
        PushUser user = (PushUser) e.getUser();

        if (user.getTeam().equals(PushServer.getGame().getBlueTeam())) {
            e.setRespawnLocation(PushServer.getMap().getRandomBlueSpawn());
        } else {
            e.setRespawnLocation(PushServer.getMap().getRandomRedSpawn());
        }

        user.respawn();
    }

    @EventHandler
    public void onBlockPlace(UserBlockPlaceEvent e) {
        if (!e.getBlockPlaced().getType().equals(Material.TNT)) {
            return;
        }

        Location loc = e.getBlock().getLocation();

        loc.add(0.5, 0, 0.5);

        TNTPrimed tnt = loc.getWorld().spawn(loc, TNTPrimed.class);

        tnt.setFuseTicks(20 * 2);

        e.getUser().removeCertainItemStack(PushKit.TNT.asOne());
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent e) {
        e.blockList().clear();
        e.setYield(0);
    }

    @EventHandler
    public void onUserDropItem(UserDropItemEvent e) {
        Material material = e.getItemStack().getType();

        if (material.equals(Material.GLASS_BOTTLE)) {
            e.setCancelled(true);
            Server.runTaskLaterSynchrony(() -> e.getUser().getInventory().remove(material), 1, GamePush.getPlugin());
        }
    }

}
