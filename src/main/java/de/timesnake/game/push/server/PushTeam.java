/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.push.server;

import de.timesnake.basic.game.util.game.Team;
import de.timesnake.database.util.game.DbTeam;

public class PushTeam extends Team {

  private PushTeam opposite;

  public PushTeam(DbTeam team) {
    super(team);
  }

  public PushTeam getOpposite() {
    return opposite;
  }

  public void setOpposite(PushTeam opposite) {
    this.opposite = opposite;
  }

}
