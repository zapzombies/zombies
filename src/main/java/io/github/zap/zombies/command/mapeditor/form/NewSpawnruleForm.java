package io.github.zap.zombies.command.mapeditor.form;

import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.ConversionResult;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Converters;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.Zombies;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.MapContextData;
import io.github.zap.zombies.command.mapeditor.form.data.SpawnRuleContext;
import io.github.zap.zombies.game.data.map.SpawnRule;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class NewSpawnruleForm extends CommandForm<SpawnRuleContext> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("spawnrule", Component.text("spawnrule")),
            new Parameter("create", Component.text("create")),
            new Parameter(Regexes.OBJECT_NAME, Component.text("[spawnrule-name]")),
            new Parameter(Regexes.BOOLEAN, Component.text("[is-blacklist]"), Converters.BOOLEAN_CONVERTER),
            new Parameter(Regexes.STRING_LIST, Component.text("[mob-names]"), Converters.newArrayConverter(
                    (form, argument) -> ConversionResult.of(true, argument, null), ",",
                    String.class))
    };

    private static final CommandValidator<SpawnRuleContext, MapContextData> validator = new CommandValidator<>(
            (context, objects, mapSelectionData) -> {
        String spawnruleName = (String)objects[2];

        if(mapSelectionData.getMap().getSpawnRules().containsKey(spawnruleName)) {
            return ValidationResult.of(false, Component.text("A spawnrule with that name already exists!"), null);
        }

        String[] names = (String[]) objects[4];
        Set<String> namesSet = new HashSet<>();
        for(String mobName : names) {
            if(Zombies.getInstance().getMythicMobs().getAPIHelper().getMythicMob(mobName) == null) {
                return ValidationResult.of(false, Component.text("Mob '" + mobName + "' does not exist!"), null);
            }

            namesSet.add(mobName);
        }

        return ValidationResult.of(true, null, new SpawnRuleContext(mapSelectionData.getPlayer(),
                mapSelectionData.getContext(), mapSelectionData.getMap(), new SpawnRule(spawnruleName,
                (boolean)objects[3], namesSet)));
    }, MapeditorValidators.HAS_ACTIVE_MAP);

    public NewSpawnruleForm(@NotNull RegularCommand command) {
        super(command, Component.text("Creates a new spawnrule."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<SpawnRuleContext, ?> getValidator(Context context, Object[] objects) {
        return validator;
    }

    @Override
    public Component execute(Context context, Object[] objects, SpawnRuleContext rule) {
        SpawnRule spawnRule = rule.getRule();
        rule.getMap().getSpawnRules().put(spawnRule.getName(), spawnRule);
        return Component.text("Created new spawnrule.");
    }
}
