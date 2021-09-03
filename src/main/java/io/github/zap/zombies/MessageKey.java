package io.github.zap.zombies;

import lombok.Getter;

public enum MessageKey {
    /*
    These are all error message resources that should be displayed to the player when they fail to join an arena.
     */
    GENERIC_ARENA_REJECTION("zombies.arena.reject.generic"), //typically because the game is ongoing or full
    NEW_ARENA_REJECTION("zombies.arena.reject.new"), //new arena was created but still did not accept the player(s)
    OFFLINE_ARENA_REJECTION("zombies.arena.reject.offline"), //one or more of the players joining are offline
    UNKNOWN_ARENA_REJECTION("zombies.arena.reject.unknown"), //nonspecific error message

    WINDOW_REPAIR_FAIL_MOB("zombies.game.window.fail.mob"), //shown when a mob blocks window repair
    WINDOW_REPAIR_FAIL_PLAYER("zombies.game.window.fail.player"), //shown when a player blocks window repair

    GOLD("zombies.game.gold.name"),
    ADD_GOLD("zombies.game.gold.add"), //shown when gold is given to the player
    SUBTRACT_GOLD("zombies.game.gold.subtract"), //shown when gold is given to the player
    CANNOT_AFFORD("zombies.game.gold.unaffordable"), //generic message shown when something is too expensive to purchase

    CHOOSE_SLOT("zombies.game.hotbar.choose"),
    NO_GROUP("zombies.game.hotbar.group.unavailable"),

    MAXED_OUT("zombies.game.shop.max"),
    ACTIVE("zombies.game.shop.active"),
    UNLOCKED("zombies.game.shop.unlocked"),
    REQUIRES_POWER("zombies.game.shop.power"),
    COST("zombies.game.shop.cost"),

    LUCKY_CHEST("zombies.game.shop.chest.name"),
    POWER_SWITCH("zombies.game.shop.power.name"),
    TEAM_MACHINE("zombies.game.shop.team_machine.name"),
    ULTIMATE_MACHINE("zombies.game.shop.ultimate_machine.name"),

    RIGHT_CLICK_TO_OPEN("zombies.game.shop.team_machine.open"),

    LEFT_CLICK("zombies.tutorial.click.left"),
    RIGHT_CLICK("zombies.tutorial.click.right"),
    TO_RELOAD("zombies.tutorial.reload"),
    TO_SHOOT("zombies.tutorial.shoot"),

    NO_AMMO("zombies.game.shop.gun.ammo.empty"),

    BUY_GUN("zombies.game.gun.buy"),
    REFILL_AMMO("zombies.game.gun.ammo"),

    OTHER_PERSON_ROLLING("zombies.game.chest.other"),
    NOT_DONE_ROLLING("zombies.game.chest.unfinished"),
    RIGHT_CLICK_TO_CLAIM("zombies.game.chest.claim"),
    TIME_REMAINING("zombies.game.chest.time"),

    ACTIVATED_POWER_TITLE("zombies.game.power.on.title"),
    ACTIVATED_POWER_SUBTITLE("zombies.game.power.on.subtitle"),
    NO_POWER("zombies.game.power.off"),

    TEAM_MACHINE_PURCHASE("zombies.game.tm.purchase"),

    CORPSE_LINE("zombies.game.corpse_line"),
    DYING_MESSAGE("zombies.game.corpse.dying_message"),
    REVIVING_MESSAGE("zombies.game.corpse.time_until_revive"),
    CORPSE_TIME_REMAINING("zombies.game.corpse.time_remaining"),

    PLACEHOLDER("zombies.game.placeholder");

    @Getter
    private final String key;

    MessageKey(String key) {
        this.key = key;
    }
}
