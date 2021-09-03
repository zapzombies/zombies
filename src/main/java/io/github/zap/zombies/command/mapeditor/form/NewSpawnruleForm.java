package io.github.zap.zombies.command.mapeditor.form;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.converter.ConversionResult;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Converters;
import io.github.regularcommands.util.Permissions;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.MapContextData;
import io.github.zap.zombies.command.mapeditor.form.data.SpawnRuleContext;
import io.github.zap.zombies.game.data.map.SpawnRule;

import java.util.HashSet;
import java.util.Set;

public class NewSpawnruleForm extends CommandForm<SpawnRuleContext> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("spawnrule"),
            new Parameter("create"),
            new Parameter(Regexes.OBJECT_NAME, "[spawnrule-name]"),
            new Parameter(Regexes.BOOLEAN, "[is-blacklist]", Converters.BOOLEAN_CONVERTER),
            new Parameter(Regexes.STRING_LIST, "[mob-names]", Converters.newArrayConverter(argument ->
                    ConversionResult.of(true, argument, null), ",", String.class))
    };

    private static final CommandValidator<SpawnRuleContext, MapContextData> validator = new CommandValidator<>((context, objects, mapSelectionData) -> {
        String spawnruleName = (String)objects[2];

        if(mapSelectionData.getMap().getSpawnRules().containsKey(spawnruleName)) {
            return ValidationResult.of(false, "A spawnrule with that name already exists!", null);
        }

        String[] names = (String[]) objects[4];
        Set<String> namesSet = new HashSet<>();
        for(String mobName : names) {
            if(Zombies.getInstance().getMythicMobs().getAPIHelper().getMythicMob(mobName) == null) {
                return ValidationResult.of(false, "Mob '" + mobName + "' does not exist!", null);
            }

            namesSet.add(mobName);
        }

        return ValidationResult.of(true, null, new SpawnRuleContext(mapSelectionData.getPlayer(),
                mapSelectionData.getContext(), mapSelectionData.getMap(), new SpawnRule(spawnruleName,
                (boolean)objects[3], namesSet)));
    }, MapeditorValidators.HAS_ACTIVE_MAP);

    public NewSpawnruleForm() {
        super("Creates a new spawnrule.", Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<SpawnRuleContext, ?> getValidator(Context context, Object[] objects) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] objects, SpawnRuleContext rule) {
        SpawnRule spawnRule = rule.getRule();
        rule.getMap().getSpawnRules().put(spawnRule.getName(), spawnRule);
        return "Created new spawnrule.";
    }
}
