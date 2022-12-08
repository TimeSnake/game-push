/*
 * workspace.game-push.main
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

import de.timesnake.basic.bukkit.util.exception.UnsupportedGroupRankException;
import de.timesnake.basic.game.util.game.Kit;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.game.util.game.TmpGame;
import de.timesnake.database.util.game.DbKit;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.game.push.map.PushMap;
import de.timesnake.game.push.user.PushKit;

public class PushGame extends TmpGame {

    private static final String BLUE_NAME = "blue";
    private static final String RED_NAME = "red";

    public PushGame(DbTmpGame game) {
        super(game, true);
    }

    @Override
    public PushMap loadMap(DbMap dbMap, boolean loadWorld) {
        return new PushMap(dbMap, loadWorld);
    }

    @Override
    public PushTeam loadTeam(DbTeam team) throws UnsupportedGroupRankException {
        return new PushTeam(team);
    }

    @Override
    public Kit loadKit(DbKit dbKit) {
        int dbId = dbKit.getId();
        for (PushKit kit : PushKit.KITS) {
            if (kit.getId().equals(dbId)) {
                return kit;
            }
        }
        return null;
    }

    public Team getBlueTeam() {
        return super.getTeam(BLUE_NAME);
    }

    public Team getRedTeam() {
        return super.getTeam(RED_NAME);
    }
}