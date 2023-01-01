/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.push.main;

import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.game.push.server.PushServerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GamePush extends JavaPlugin {

    private static GamePush plugin;

    public static GamePush getPlugin() {
        return plugin;
    }

    @Override
    public void onLoad() {
        ServerManager.setInstance(new PushServerManager());
    }

    @Override
    public void onEnable() {
        plugin = this;

        PushServerManager.getInstance().onPushServerEnable();
    }
}
