package de.timesnake.game.push.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.game.util.Game;
import de.timesnake.basic.game.util.Team;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.basic.loungebridge.util.user.Kit;
import de.timesnake.basic.loungebridge.util.user.KitNotDefinedException;
import de.timesnake.database.util.game.DbGame;
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
import de.timesnake.library.extension.util.chat.Chat;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.time.Duration;

public class PushServerManager extends LoungeBridgeServerManager {

    private boolean isRunning;
    private int lap = 0;
    private EscortManager escortManager;
    private UserManager userManager;
    private Sideboard sideboard;
    private int blueWins = 0;
    private int redWins = 0;

    private SpecialItemManager specialItemManager;

    public static PushServerManager getInstance() {
        return (PushServerManager) LoungeBridgeServerManager.getInstance();
    }

    public void onPushServerEnable() {
        super.onLoungeBridgeEnable();

        this.escortManager = new EscortManager();
        this.userManager = new UserManager();

        this.setTeamMateDamage(false);

        this.sideboard = Server.getScoreboardManager().registerNewSideboard("push", "§6§lPush");
        this.sideboard.setScore(4, "§c§lLap");
        // lap
        this.sideboard.setScore(2, "§r§f-----------");
        this.sideboard.setScore(1, "§9§lMap");
        // map

        this.specialItemManager = new SpecialItemManager();

    }


    @Override
    protected Game loadGame(DbGame dbGame, boolean loadWorlds) {
        return new PushGame(dbGame);
    }

    @Override
    public PushMap getMap() {
        return (PushMap) super.getMap();
    }

    @Override
    public PushGame getGame() {
        return (PushGame) super.getGame();
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
    public void broadcastGameMessage(String message) {
        Server.broadcastMessage(Plugin.PUSH, ChatColor.PUBLIC + message);
    }


    @Override
    public void loadMap() {
        super.loadMap();
        for (LivingEntity entity : this.getMap().getWorld().getLivingEntities()) {
            entity.remove();
        }

        this.sideboard.setScore(0, this.getMap().getDisplayName());
    }

    @Override
    public void prepareGame() {
        this.lap = 0;
        this.updateSideboardLap();
    }

    @Override
    public void startGame() {
        int delta = this.getGame().getBlueTeam().getUsers().size() - this.getGame().getRedTeam().getUsers().size();
        if (delta < 0) {
            for (User user : this.getGame().getBlueTeam().getUsers()) {
                user.setMaxHealth(((double) this.getGame().getRedTeam().getUsers().size()) / this.getGame().getBlueTeam().getUsers().size() * 20);
                user.setHealth(((double) this.getGame().getRedTeam().getUsers().size()) / this.getGame().getBlueTeam().getUsers().size() * 20);
            }

            for (User user : this.getGame().getRedTeam().getUsers()) {
                user.setMaxHealth(20);
                user.setHealth(20);
            }
        } else if (delta > 0) {
            for (User user : this.getGame().getRedTeam().getUsers()) {
                user.setMaxHealth(((double) this.getGame().getBlueTeam().getUsers().size()) / this.getGame().getRedTeam().getUsers().size() * 20);
                user.setHealth(((double) this.getGame().getBlueTeam().getUsers().size()) / this.getGame().getRedTeam().getUsers().size() * 20);
            }

            for (User user : this.getGame().getBlueTeam().getUsers()) {
                user.setMaxHealth(20);
                user.setHealth(20);
            }
        }

        for (User user : Server.getInGameUsers()) {
            user.setMaxHealth(20);
            user.setHealth(20);
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

        if (this.lap >= this.getMap().getLaps()) {
            this.stopGame();
            return;
        }

        Team blueTeam = this.getGame().getBlueTeam();
        Team redTeam = this.getGame().getRedTeam();

        if (blue) {
            Server.broadcastTitle(blueTeam.getChatColor() + blueTeam.getDisplayName() + " scored", "", Duration.ofSeconds(3));
        } else {
            Server.broadcastTitle(redTeam.getChatColor() + redTeam.getDisplayName() + " scored", "", Duration.ofSeconds(3));
        }

        this.broadcastGameMessage(ChatColor.GOLD + "§lScores: ");
        this.broadcastGameMessage(Chat.getLongLineSeparator());
        this.broadcastGameMessage("    " + this.blueWins + " " + blueTeam.getChatColor() + blueTeam.getDisplayName());
        this.broadcastGameMessage("    " + this.redWins + " " + redTeam.getChatColor() + redTeam.getDisplayName());
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
                this.broadcastGameMessage(ChatColor.PUBLIC + "The Game starts " + ChatColor.VALUE + "now");
                Server.broadcastNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
                this.nextLap();
            } else {
                Server.broadcastTitle(ChatColor.RED + "" + time, "", Duration.ofSeconds(1));
                this.broadcastGameMessage(ChatColor.PUBLIC + "Next lap starts in " + ChatColor.VALUE + time + ChatColor.PUBLIC + " seconds");
                Server.broadcastNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
            }
        }, 3, true, 3 * 20, 20, GamePush.getPlugin());

    }

    public void stopGame() {
        Team blueTeam = this.getGame().getBlueTeam();
        Team redTeam = this.getGame().getRedTeam();

        this.broadcastGameMessage(ChatColor.GOLD + "§lResult:");
        this.broadcastGameMessage(Chat.getLongLineSeparator());
        this.broadcastGameMessage("    " + this.blueWins + " " + blueTeam.getChatColor() + blueTeam.getDisplayName());
        this.broadcastGameMessage("    " + this.redWins + " " + redTeam.getChatColor() + redTeam.getDisplayName());
        this.broadcastGameMessage(Chat.getLongLineSeparator());

        if (this.blueWins > this.redWins) {
            Server.broadcastTitle(blueTeam.getChatColor() + blueTeam.getDisplayName() + " wins", "", Duration.ofSeconds(5));
        } else if (this.redWins > this.blueWins) {
            Server.broadcastTitle(redTeam.getChatColor() + redTeam.getDisplayName() + " wins", "",
                    Duration.ofSeconds(5));
        } else {
            Server.broadcastTitle("Draw", "", Duration.ofSeconds(5));
        }

        super.closeGame();

    }

    public void updateSideboardLap() {
        this.sideboard.setScore(3, this.lap + " §7/ §f" + this.getMap().getLaps());
    }

    public Sideboard getGameSideboard() {
        return this.sideboard;
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
    public void resetGame() {
        this.blueWins = 0;
        this.redWins = 0;
        this.lap = 0;

        if (this.getMap() != null) {
            Server.getWorldManager().reloadWorld(this.getMap().getWorld());
        }
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
    public Location getSpectatorSpawn() {
        return this.getMap().getZombieSpawn();
    }

}
