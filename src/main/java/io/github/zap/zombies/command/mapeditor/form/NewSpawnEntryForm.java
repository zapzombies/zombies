package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Converters;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.MapContextData;
import io.github.zap.zombies.command.mapeditor.form.data.SpawnEntryContextData;
import io.github.zap.zombies.game.data.map.RoundData;
import io.github.zap.zombies.game.data.map.SpawnEntryData;

import java.util.List;

public class NewSpawnEntryForm extends CommandForm<SpawnEntryContextData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("spawnentry"),
            new Parameter("create"),
            new Parameter(Regexes.NON_NEGATIVE_INTEGER, "[round-index]", Converters.INTEGER_CONVERTER),
            new Parameter(Regexes.NON_NEGATIVE_INTEGER, "[wave-index]", Converters.INTEGER_CONVERTER),
            new Parameter(Regexes.NON_NEGATIVE_INTEGER, "[count]", Converters.INTEGER_CONVERTER),
            new Parameter(Regexes.OBJECT_NAME, "[mob-name]")
    };

    private static final CommandValidator<SpawnEntryContextData, MapContextData> validator = new CommandValidator<>((context, objects, mapContextData) -> {
        String mobName = (String)objects[5];
        if(Zombies.getInstance().getMythicMobs().getAPIHelper().getMythicMob(mobName) == null) {
            return ValidationResult.of(false, "A mob with that name does not exist!", null);
        }

        int roundIndex = (int)objects[2];
        int waveIndex = (int)objects[3];

        List<RoundData> rounds = mapContextData.getMap().getRounds();
        if(roundIndex >= rounds.size()) {
            return ValidationResult.of(false, "Round index out of bounds!", null);
        }

        RoundData round = rounds.get(roundIndex);
        if(waveIndex >= round.getWaves().size()) {
            return ValidationResult.of(false, "Wave index out of bounds!", null);
        }

        return ValidationResult.of(true, null, new SpawnEntryContextData(mapContextData.getPlayer(),
                mapContextData.getContext(), mapContextData.getMap(), round.getWaves().get(waveIndex),
                new SpawnEntryData(mobName, (int)objects[4])));
    }, MapeditorValidators.HAS_ACTIVE_MAP);

    public NewSpawnEntryForm() {
        super("Creates a new spawn entry.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<SpawnEntryContextData, ?> getValidator(Context context, Object[] objects) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] objects, SpawnEntryContextData mapContextData) {
        mapContextData.getWave().getSpawnEntries().add(mapContextData.getSpawnEntry());
        return "Added new spawn entry.";
    }
}
