package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.zombies.game.ZombiesArena;
import io.github.zap.zombies.game.player.ZombiesPlayer;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.jetbrains.annotations.NotNull;

public abstract class ZombiesPlayerSkill extends ZombiesArenaSkill {
    public ZombiesPlayerSkill(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
    }

    @Override
    public boolean cast(@NotNull SkillMetadata metadata, @NotNull ZombiesArena arena) {
        AbstractEntity target = metadata.getCaster().getEntity().getTarget();

        if(target != null) {
            ZombiesPlayer targetPlayer = arena.getPlayerMap().get(target.getUniqueId());

            if(targetPlayer != null && targetPlayer.isAlive()) {
                return castAtPlayer(metadata, arena, targetPlayer);
            }
        }

        return false;
    }

    public abstract boolean castAtPlayer(@NotNull SkillMetadata skillMetadata, @NotNull ZombiesArena arena,
                                         @NotNull ZombiesPlayer target);
}
