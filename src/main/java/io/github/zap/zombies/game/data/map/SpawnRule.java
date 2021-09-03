package io.github.zap.zombies.game.data.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class SpawnRule {
    private String name;
    private boolean blacklist;
    private Set<String> mobSet;

    private SpawnRule() {}
}
