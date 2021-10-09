package io.github.zap.zombies.command.mapeditor.form;

import io.github.zap.regularcommands.commands.CommandForm;
import io.github.zap.regularcommands.commands.Context;
import io.github.zap.regularcommands.commands.RegularCommand;
import io.github.zap.regularcommands.converter.Parameter;
import io.github.zap.regularcommands.util.Permissions;
import io.github.zap.regularcommands.validator.CommandValidator;
import io.github.zap.regularcommands.validator.ValidationResult;
import io.github.zap.zombies.command.mapeditor.EditorContext;
import io.github.zap.zombies.command.mapeditor.MapeditorValidators;
import io.github.zap.zombies.command.mapeditor.Regexes;
import io.github.zap.zombies.command.mapeditor.form.data.RoomSelectionData;
import io.github.zap.zombies.command.mapeditor.form.data.ShopSelectionData;
import io.github.zap.zombies.game.data.map.shop.*;
import io.github.zap.zombies.game.shop.ShopType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class NewShopForm extends CommandForm<ShopSelectionData> {
    private static final Parameter[] parameters = new Parameter[] {
            new Parameter("shop", Component.text("shop")),
            new Parameter("create", Component.text("create")),
            new Parameter(Regexes.OBJECT_NAME, Component.text("[shop-type]"), false)
    };

    private static final CommandValidator<ShopSelectionData, RoomSelectionData> validator = new CommandValidator<>(
            (context, arguments, previousData) -> {
        try {
            return ValidationResult.of(true, null, new ShopSelectionData(previousData.getPlayer(),
                    previousData.getContext(), previousData.getSelection(), previousData.getMap(), previousData.getRoom(),
                            ShopType.valueOf(((String)arguments[2]).toUpperCase())));
        }
        catch (IllegalArgumentException e) {
            return ValidationResult.of(false, Component.text("That is not a valid shop type!"), null);
        }
    }, MapeditorValidators.HAS_ROOM_SELECTION);

    public NewShopForm(@NotNull RegularCommand command) {
        super(command, Component.text("Creates a new shop."), Permissions.OPERATOR, parameters);
    }

    @Override
    public CommandValidator<ShopSelectionData, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public Component execute(Context context, Object[] arguments, ShopSelectionData data) {
        switch (data.getType()) {
            case DOOR:
                return Component.text("Use /mapeditor door create to create a door.");
            case GUN_SHOP:
                data.getMap().getShops().add(new GunShopData(data.getContext().getTarget(),
                        data.getPlayer().getLocation().toVector()));
                break;
            case ARMOR_SHOP:
                data.getMap().getShops().add(new ArmorShopData(data.getContext().getTarget(),
                        data.getPlayer().getLocation().toVector()));
                break;
            case LUCKY_CHEST:
                data.getMap().getShops().add(new LuckyChestData(data.getContext().getTarget()));
                break;
            case PERK_MACHINE:
                data.getMap().getShops().add(new PerkMachineData(data.getContext().getTarget(),
                        data.getPlayer().getLocation().toVector()));
                break;
            case POWER_SWITCH:
                data.getMap().getShops().add(new PowerSwitchData(data.getContext().getTarget(),
                        data.getPlayer().getLocation().toVector()));
                break;
            case TEAM_MACHINE:
                data.getMap().getShops().add(new TeamMachineData(data.getContext().getTarget(),
                        data.getPlayer().getLocation().toVector()));
                break;
            case ULTIMATE_MACHINE:
                data.getMap().getShops().add(new UltimateMachineData(data.getContext().getTarget(),
                        data.getPlayer().getLocation().toVector()));
                break;
            default:
                return Component.text("Unsupported shop type.");
        }

        data.getContext().updateRenderable(EditorContext.Renderables.SHOPS);
        return Component.text("Created new shop.");
    }
}
