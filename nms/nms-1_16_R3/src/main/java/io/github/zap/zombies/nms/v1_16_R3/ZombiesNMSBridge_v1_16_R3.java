package io.github.zap.zombies.nms.v1_16_R3;

import io.github.zap.zombies.nms.common.ZombiesNMSBridge;
import io.github.zap.zombies.nms.common.entity.EntityBridge;
import io.github.zap.zombies.nms.v1_16_R3.entity.EntityBridge_v1_16_R3;
import org.jetbrains.annotations.NotNull;

public class ZombiesNMSBridge_v1_16_R3 implements ZombiesNMSBridge {

    public static final ZombiesNMSBridge_v1_16_R3 INSTANCE = new ZombiesNMSBridge_v1_16_R3();
    private static final String VERSION = "v1_16_R3";

    @Override
    public @NotNull String version() {
        return VERSION;
    }

    @Override
    public @NotNull EntityBridge entityBridge() {
        return EntityBridge_v1_16_R3.INSTANCE;
    }

}
