/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.push.server;

import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.loungebridge.util.game.TmpGame;
import de.timesnake.basic.loungebridge.util.user.KitManager;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.game.push.map.PushMap;
import de.timesnake.game.push.user.PushKitManager;

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
  public PushTeam loadTeam(DbTeam team) {
    return new PushTeam(team);
  }

  @Override
  public KitManager<?> loadKitManager() {
    return new PushKitManager();
  }

  public Team getBlueTeam() {
    return super.getTeam(BLUE_NAME);
  }

  public Team getRedTeam() {
    return super.getTeam(RED_NAME);
  }
}