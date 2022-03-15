package de.timesnake.game.push.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserDeathEvent;
import de.timesnake.basic.bukkit.util.user.event.UserRespawnEvent;
import de.timesnake.game.push.main.GamePush;
import de.timesnake.game.push.server.PushServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
}
