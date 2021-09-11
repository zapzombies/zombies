package io.github.zap.zombies.game.mob.mechanic;

import io.github.zap.arenaapi.util.MetadataHelper;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.game.ZombiesArena;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.INoTargetSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class ZombiesArenaSkill extends SkillMechanic implements INoTargetSkill {
    public ZombiesArenaSkill(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        setAsyncSafe(false);
    }

    @Override
    public boolean cast(SkillMetadata skillMetadata) {
       Optional<MetadataValue> metadataOptional = MetadataHelper.getMetadataValue(skillMetadata.getCaster().getEntity()
               .getBukkitEntity(), Zombies.getInstance(), Zombies.ARENA_METADATA_NAME);

        if(metadataOptional.isPresent()) {
            ZombiesArena arena = (ZombiesArena)metadataOptional.get().value();

            if(arena != null) {
                return cast(skillMetadata, arena);
            }
        }

        return false;
    }

    public abstract boolean cast(@NotNull SkillMetadata metadata, @NotNull ZombiesArena arena);
}
