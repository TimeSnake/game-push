/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.push.server;

import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.game.push.map.EscortManager;
import de.timesnake.game.push.map.PushMap;
import de.timesnake.library.basic.util.TimeCoins;
import org.bukkit.boss.BossBar;

public class PushServer extends LoungeBridgeServer {

    public static final float KILL_COINS_POOL = 16 * TimeCoins.MULTIPLIER;
    public static final float WIN_COINS = 10 * TimeCoins.MULTIPLIER;
    public static final float LAP_COINS = 5 * TimeCoins.MULTIPLIER;

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

    public static EscortManager getEscordManager() {
        return server.getEscortManager();
    }

    public static BossBar getBossBar() {
        return server.getBossBar();
    }
}
