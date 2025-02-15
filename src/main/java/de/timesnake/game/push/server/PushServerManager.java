/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.push.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.KeyedSideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.KeyedSideboard.LineId;
import de.timesnake.basic.bukkit.util.user.scoreboard.KeyedSideboardBuilder;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.loungebridge.util.server.EndMessage;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager;
import de.timesnake.basic.loungebridge.util.tool.listener.GameUserDeathListener;
import de.timesnake.basic.loungebridge.util.tool.listener.GameUserRespawnListener;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.game.push.main.GamePush;
import de.timesnake.game.push.map.EscortManager;
import de.timesnake.game.push.map.ItemSpawner;
import de.timesnake.game.push.map.PushMap;
import de.timesnake.game.push.user.PushUser;
import de.timesnake.game.push.user.SpecialItemManager;
import de.timesnake.game.push.user.UserManager;
import de.timesnake.library.chat.Chat;
import de.timesnake.library.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.time.Duration;

public class PushServerManager extends LoungeBridgeServerManager<PushGame> {

  public static final LineId<String> LAP_LINE = LineId.of("lap", "§c§lLap", false,
      Object::toString);

  public static PushServerManager getInstance() {
    return (PushServerManager) LoungeBridgeServerManager.getInstance();
  }

  private int lap = 0;
  private EscortManager escortManager;
  private UserManager userManager;
  private KeyedSideboard sideboard;
  private int blueWins = 0;
  private int redWins = 0;
  private SpecialItemManager specialItemManager;
  private BossBar bossBar;

  public void onPushServerEnable() {
    super.onLoungeBridgeEnable();

    this.escortManager = new EscortManager();
    this.userManager = new UserManager();

    this.setTeamMateDamage(false);

    this.sideboard = Server.getScoreboardManager().registerExSideboard(new KeyedSideboardBuilder()
        .name("push")
        .title("§6§lPush")
        .lineSpacer()
        .addLine(LAP_LINE)
        .addLine(LineId.MAP));

    this.specialItemManager = new SpecialItemManager();

    this.bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);

    this.getToolManager().add((GameUserDeathListener) (e, user) -> {
      e.setAutoRespawn(true);
      e.getDrops().clear();
      e.setKeepInventory(false);
    });

    this.getToolManager().add((GameUserRespawnListener) user -> {
      user.respawnDelayed(3);

      if (user.getTeam().equals(PushServer.getGame().getBlueTeam())) {
        return PushServer.getMap().getRandomBlueSpawn();
      } else {
        return PushServer.getMap().getRandomRedSpawn();
      }
    });
  }

  @Override
  protected PushGame loadGame(DbGame dbGame, boolean loadWorlds) {
    return new PushGame((DbTmpGame) dbGame);
  }

  @Override
  public PushMap getMap() {
    return (PushMap) super.getMap();
  }

  @Override
  public PushGame getGame() {
    return super.getGame();
  }

  @Override
  public GameUser loadUser(Player player) {
    return new PushUser(player);
  }

  public EscortManager getEscortManager() {
    return escortManager;
  }

  public SpecialItemManager getSpecialItemManager() {
    return specialItemManager;
  }

  @Override
  public void onMapLoad() {
    for (LivingEntity entity : this.getMap().getWorld().getLivingEntities()) {
      entity.remove();
    }

    this.sideboard.updateScore(LineId.MAP, this.getMap().getDisplayName());

    this.lap = 0;
    this.updateSideboardLap();

    this.updateBossBar(0, 0);
  }

  @Override
  public void onGameStart() {
    int delta = this.getGame().getBlueTeam().getUsers().size() - this.getGame().getRedTeam()
        .getUsers().size();
    if (delta < 0) {
      double health = ((double) this.getGame().getRedTeam().getUsers().size()) /
          this.getGame().getBlueTeam().getUsers().size() * 20;

      for (User user : this.getGame().getBlueTeam().getUsers()) {
        user.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        user.setHealth(health);
      }

      for (User user : this.getGame().getRedTeam().getUsers()) {
        user.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        user.setHealth(20);
      }
    } else if (delta > 0) {
      double health = ((double) this.getGame().getBlueTeam().getUsers().size()) /
          this.getGame().getRedTeam().getUsers().size() * 20;

      for (User user : this.getGame().getRedTeam().getUsers()) {
        user.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        user.setHealth(health);
      }

      for (User user : this.getGame().getBlueTeam().getUsers()) {
        user.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        user.setHealth(20);
      }
    } else {
      for (User user : Server.getInGameUsers()) {
        user.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        user.setHealth(20);
      }
    }

    this.nextLap();
  }

  private void nextLap() {
    this.lap++;

    this.updateSideboardLap();

    for (User user : Server.getInGameUsers()) {
      user.unlockLocation();
      user.setGravity(true);
      user.setInvulnerable(false);
    }

    this.escortManager.start();
    for (ItemSpawner spawner : this.getMap().getItemSpawners()) {
      spawner.start();
    }
  }

  public void onFinishReached(boolean blue) {
    this.escortManager.stop();
    for (ItemSpawner spawner : this.getMap().getItemSpawners()) {
      spawner.stop();
    }

    if (blue) {
      this.blueWins++;
      this.getGame().getBlueTeam().getUsers()
          .forEach(u -> u.addCoins(PushServer.LAP_COINS, true));
    } else {
      this.redWins++;
      this.getGame().getRedTeam().getUsers()
          .forEach(u -> u.addCoins(PushServer.LAP_COINS, true));
    }

    this.updateBossBar(this.blueWins, this.redWins);

    if (this.blueWins > this.getMap().getLaps() / 2
        || this.redWins > this.getMap().getLaps() / 2) {
      this.stopGame();
      return;
    }

    Team blueTeam = this.getGame().getBlueTeam();
    Team redTeam = this.getGame().getRedTeam();

    if (blue) {
      Server.broadcastTitle(Component.text(blueTeam.getDisplayName(), blueTeam.getTextColor())
              .append(Component.text(" scored", ExTextColor.WHITE)), Component.empty(),
          Duration.ofSeconds(3));
    } else {
      Server.broadcastTitle(Component.text(redTeam.getDisplayName(), redTeam.getTextColor())
              .append(Component.text(" scored", ExTextColor.WHITE)), Component.empty(),
          Duration.ofSeconds(3));
    }

    this.broadcastGameMessage(Component.text("Scores: ", ExTextColor.GOLD, TextDecoration.BOLD));
    this.broadcastGameMessage(Chat.getLongLineSeparator());
    this.broadcastGameMessage(Component.text("    " + this.blueWins + " ", ExTextColor.PUBLIC)
        .append(Component.text(blueTeam.getDisplayName(), blueTeam.getTextColor())));
    this.broadcastGameMessage(Component.text("    " + this.redWins + " ", ExTextColor.PUBLIC)
        .append(Component.text(redTeam.getDisplayName(), redTeam.getTextColor())));
    this.broadcastGameMessage(Chat.getLongLineSeparator());

    Server.runTaskLaterSynchrony(() -> {
      for (User user : Server.getInGameUsers()) {
        if (((PushUser) user).getTeam().equals(PushServer.getGame().getBlueTeam())) {
          user.teleport(PushServer.getMap().getRandomBlueSpawn());
        } else {
          user.teleport(PushServer.getMap().getRandomRedSpawn());
        }
        user.lockLocation();
        user.setGravity(false);
        user.setInvulnerable(true);
        ((PushUser) user).respawn();
      }
    }, 20 * 3, GamePush.getPlugin());

    Server.runTaskTimerSynchrony((time) -> {
      if (time == 0) {
        this.broadcastGameMessage(Component.text("The Game starts ", ExTextColor.PUBLIC)
            .append(Component.text("now", ExTextColor.VALUE)));
        Server.broadcastNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
        this.nextLap();
      } else {
        Server.broadcastTitle(Component.text(time, ExTextColor.WARNING), Component.empty(),
            Duration.ofSeconds(1));
        this.broadcastGameMessage(Component.text("Next lap starts in ", ExTextColor.PUBLIC)
            .append(Component.text(time, ExTextColor.VALUE))
            .append(Component.text(" seconds", ExTextColor.PUBLIC)));
        Server.broadcastNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
      }
    }, 3, true, 3 * 20, 20, GamePush.getPlugin());

  }

  @Override
  public void onGameStop() {
    Team blueTeam = this.getGame().getBlueTeam();
    Team redTeam = this.getGame().getRedTeam();

    Team winnerTeam;

    if (this.blueWins > this.redWins) {
      winnerTeam = blueTeam;
    } else if (this.redWins > this.blueWins) {
      winnerTeam = redTeam;
    } else {
      winnerTeam = null;
    }

    new EndMessage()
        .winner(winnerTeam)
        .addExtra("§h§lResult:")
        .addExtraLineSeparator()
        .addExtra("    §p" + this.blueWins + " " + blueTeam.getTDColor() + blueTeam.getDisplayName())
        .addExtra("    " + this.redWins + " " + redTeam.getTDColor() + redTeam.getDisplayName())
        .send();

    int kills = blueTeam.getKills() + redTeam.getKills();

    Server.getInGameUsers().forEach(u ->
        u.addCoins((float) ((GameUser) u).getKills() / kills * PushServer.KILL_COINS_POOL, true));
  }

  public void updateSideboardLap() {
    this.sideboard.updateScore(LAP_LINE, this.lap + " §7/ §f" + this.getMap().getLaps());
  }

  public void updateBossBar(int blueWins, int redWins) {
    Team blue = this.getGame().getBlueTeam();
    Team red = this.getGame().getRedTeam();

    this.bossBar.setTitle(blue.getTDColor() + blue.getDisplayName() + "§f - §6" + blueWins + "§f | " +
        "§6" + redWins + "§f - " + red.getTDColor() + red.getDisplayName());

    if (blueWins > redWins) {
      this.bossBar.setColor(BarColor.BLUE);
    } else if (redWins > blueWins) {
      this.bossBar.setColor(BarColor.RED);
    } else {
      this.bossBar.setColor(BarColor.WHITE);
    }
  }

  public Sideboard getGameSideboard() {
    return this.sideboard;
  }

  public BossBar getBossBar() {
    return bossBar;
  }

  @Override
  public boolean checkGameEnd() {
    return this.getGame().getBlueTeam().isEmpty() || this.getGame().getRedTeam().isEmpty();
  }

  @Override
  public boolean isRejoiningAllowed() {
    return false;
  }

  @Override
  public void onGameReset() {
    this.blueWins = 0;
    this.redWins = 0;
    this.lap = 0;
  }

  @Override
  public Sideboard getSpectatorSideboard() {
    return this.sideboard;
  }

  @Override
  public ExLocation getSpectatorSpawn() {
    return this.getMap().getZombieSpawn();
  }

}
