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
