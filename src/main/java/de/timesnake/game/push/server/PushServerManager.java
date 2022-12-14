/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.push.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.basic.loungebridge.util.user.Kit;
import de.timesnake.basic.loungebridge.util.user.KitNotDefinedException;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.game.push.main.GamePush;
import de.timesnake.game.push.main.Plugin;
import de.timesnake.game.push.map.EscortManager;
import de.timesnake.game.push.map.ItemSpawner;
import de.timesnake.game.push.map.PushMap;
import de.timesnake.game.push.user.PushKit;
import de.timesnake.game.push.user.PushUser;
import de.timesnake.game.push.user.SpecialItemManager;
import de.timesnake.game.push.user.UserManager;
import de.timesnake.library.basic.util.chat.ChatColor;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Chat;
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

    public static PushServerManager getInstance() {
        return (PushServerManager) LoungeBridgeServerManager.getInstance();
    }

    private boolean isRunning;
    private int lap = 0;
    private EscortManager escortManager;
    private UserManager userManager;
    private Sideboard sideboard;
    private int blueWins = 0;
    private int redWins = 0;
    private SpecialItemManager specialItemManager;
    private BossBar bossBar;

    public void onPushServerEnable() {
        super.onLoungeBridgeEnable();

        this.escortManager = new EscortManager();
        this.userManager = new UserManager();

        this.setTeamMateDamage(false);

        this.sideboard = Server.getScoreboardManager().registerSideboard("push", "??6??lPush");
        this.sideboard.setScore(4, "??c??lLap");
        // lap
        this.sideboard.setScore(2, "??r??f-----------");
        this.sideboard.setScore(1, "??9??lMap");
        // map

        this.specialItemManager = new SpecialItemManager();

        this.bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
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

    @Override
    public Plugin getGamePlugin() {
        return Plugin.PUSH;
    }

    public EscortManager getEscortManager() {
        return escortManager;
    }

    public SpecialItemManager getSpecialItemManager() {
        return specialItemManager;
    }

    @Override
    public boolean isGameRunning() {
        return this.isRunning;
    }

    @Override
    @Deprecated
    public void broadcastGameMessage(String message) {
        Server.broadcastMessage(Plugin.PUSH, ChatColor.PUBLIC + message);
    }

    @Override
    public void broadcastGameMessage(Component message) {
        Server.broadcastMessage(Plugin.PUSH, message.color(ExTextColor.PUBLIC));
    }


    @Override
    public void onMapLoad() {
        for (LivingEntity entity : this.getMap().getWorld().getLivingEntities()) {
            entity.remove();
        }

        this.sideboard.setScore(0, this.getMap().getDisplayName());
    }

    @Override
    public void onGamePrepare() {
        this.lap = 0;
        this.updateSideboardLap();

        this.updateBossBar(0, 0);
    }

    @Override
    public void onGameStart() {
        int delta = this.getGame().getBlueTeam().getUsers().size() - this.getGame().getRedTeam().getUsers().size();
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
        this.isRunning = true;

        this.updateSideboardLap();

        for (User user : Server.getInGameUsers()) {
            user.lockLocation(false);
            user.setGravity(true);
            user.setInvulnerable(false);
        }

        this.escortManager.start();
        for (ItemSpawner spawner : this.getMap().getItemSpawners()) {
            spawner.start();
        }
    }

    public void onFinishReached(boolean blue) {
        this.isRunning = false;

        this.escortManager.stop();
        for (ItemSpawner spawner : this.getMap().getItemSpawners()) {
            spawner.stop();
        }

        if (blue) {
            this.blueWins++;
        } else {
            this.redWins++;
        }

        this.updateBossBar(this.blueWins, this.redWins);

        if (this.blueWins > this.getMap().getLaps() / 2 || this.redWins > this.getMap().getLaps() / 2) {
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
                user.lockLocation(true);
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
                Server.broadcastTitle(Component.text(time, ExTextColor.WARNING), Component.empty(), Duration.ofSeconds(1));
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

        this.broadcastGameMessage(Component.text("Result:", ExTextColor.GOLD, TextDecoration.BOLD));
        this.broadcastGameMessage(Chat.getLongLineSeparator());
        this.broadcastGameMessage(Component.text("    " + this.blueWins + " ", ExTextColor.PUBLIC)
                .append(Component.text(blueTeam.getDisplayName(), blueTeam.getTextColor())));
        this.broadcastGameMessage(Component.text("    " + this.redWins + " ", ExTextColor.PUBLIC)
                .append(Component.text(redTeam.getDisplayName(), redTeam.getTextColor())));
        this.broadcastGameMessage(Chat.getLongLineSeparator());

        if (this.blueWins > this.redWins) {
            Server.broadcastTitle(Component.text(blueTeam.getDisplayName(), blueTeam.getTextColor())
                            .append(Component.text(" wins", ExTextColor.WHITE)), Component.empty(),
                    Duration.ofSeconds(5));
        } else if (this.redWins > this.blueWins) {
            Server.broadcastTitle(Component.text(redTeam.getDisplayName(), redTeam.getTextColor())
                            .append(Component.text(" wins", ExTextColor.WHITE)), Component.empty(),
                    Duration.ofSeconds(5));
        } else {
            Server.broadcastTitle(Component.text("Draw", ExTextColor.WHITE), Component.empty(), Duration.ofSeconds(5));
        }
    }

    public void updateSideboardLap() {
        this.sideboard.setScore(3, this.lap + " ??7/ ??f" + this.getMap().getLaps());
    }

    public void updateBossBar(int blueWins, int redWins) {
        Team blue = this.getGame().getBlueTeam();
        Team red = this.getGame().getRedTeam();

        this.bossBar.setTitle(blue.getChatColor() + blue.getDisplayName() + "??f - ??6" + blueWins + "??f | " +
                "??6" + redWins + "??f - " + red.getChatColor() + red.getDisplayName());

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
    public void onGameUserQuit(GameUser user) {

    }

    @Override
    public void onGameUserQuitBeforeStart(GameUser user) {

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
    public Kit getKit(int index) throws KitNotDefinedException {
        for (PushKit kit : PushKit.KITS) {
            if (kit.getId().equals(index)) {
                return kit;
            }
        }
        throw new KitNotDefinedException(index);
    }

    @Override
    public Kit[] getKits() {
        return PushKit.KITS;
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
