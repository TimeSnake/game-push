/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.push.server;

import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.game.push.map.EscortManager;
import de.timesnake.game.push.map.PushMap;
import org.bukkit.boss.BossBar;

public class PushServer extends LoungeBridgeServer {

    private static final PushServerManager server = PushServerManager.getInstance();

    public static PushGame getGame() {
        return server.getGame();
    }

    public static PushMap getMap() {
        return server.getMap();
    }

    public static void onFinishReached(boolean blue) {
        server.onFinishReached(blue);
    }

    public static Sideboard getGameSideboard() {
        return server.getGameSideboard();
    }

    public static EscortManager getEscordManager() {
        return server.getEscortManager();
    }

    public static BossBar getBossBar() {
        return server.getBossBar();
    }
}
