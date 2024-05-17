/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.push.main;

public class Plugin extends de.timesnake.basic.game.util.user.Plugin {

  public static final Plugin PUSH = new Plugin("Push", "GPU");

  protected Plugin(String name, String code) {
    super(name, code);
  }
}
