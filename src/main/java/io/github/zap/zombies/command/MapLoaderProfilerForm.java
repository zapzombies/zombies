package io.github.zap.zombies.command;

import io.github.regularcommands.commands.CommandForm;
import io.github.regularcommands.commands.CommandManager;
import io.github.regularcommands.commands.Context;
import io.github.regularcommands.commands.PermissionData;
import io.github.regularcommands.converter.Parameter;
import io.github.regularcommands.util.Converters;
import io.github.regularcommands.util.Validators;
import io.github.regularcommands.validator.CommandValidator;
import io.github.regularcommands.validator.ValidationResult;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.Range;
import io.github.zap.arenaapi.shadow.org.apache.commons.lang3.time.StopWatch;
import io.github.zap.arenaapi.world.WorldLoader;
import io.github.zap.zombies.Zombies;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class MapLoaderProfilerForm extends CommandForm<Object> {
    private static final Parameter[] parameters = {
            new Parameter("profile"),
            new Parameter("maploader"),
            new Parameter("^(\\d+)$", "[iterations]", Converters.INTEGER_CONVERTER),
            new Parameter("^([a-zA-Z0-9_ ]+)$", "[world]")
    };

    private static final Range<Integer> values = Range.between(1, 50);
    private static final CommandValidator<Object, ?> validator = new CommandValidator<>((context, arguments, previousData) -> {
        String worldName = (String)arguments[3];
        if(Zombies.getInstance().getWorldLoader().worldExists(worldName)) {
            return ValidationResult.of(true, null, null);
        }

        return ValidationResult.of(false, String.format("World '%s' doesn't exist.", worldName), null);
    }, new CommandValidator<>((context, arguments, previousData) -> {
        int value = (int)arguments[2];

        if(values.contains(value)) {
            return ValidationResult.of(false, String.format("Value '%s' is out of range for this command.", value), null);
        }

        return ValidationResult.of(true, null, null);
    }, Validators.PLAYER_EXECUTOR));

    private static final Semaphore profilerSemaphore = new Semaphore(1);
    private static final StopWatch profiler = new StopWatch();
    private static final ExecutorService service = Executors.newSingleThreadExecutor();

    public MapLoaderProfilerForm() {
        super("Debug command for profiling map loading.", new PermissionData(true), parameters);
    }

    @Override
    public boolean canStylize() {
        return true;
    }

    @Override
    public CommandValidator<Object, ?> getValidator(Context context, Object[] arguments) {
        return validator;
    }

    @Override
    public String execute(Context context, Object[] arguments, Object data) {
        if(profilerSemaphore.tryAcquire()) { //only one instance of the profiler can run at a time
            Player player = (Player)context.getSender(); //validation ensures that this will never throw ClassCastException
            int iterations = (int)arguments[2];
            String worldName = (String)arguments[3];

            Zombies instance = Zombies.getInstance();
            CommandManager commandManager = context.getManager();
            WorldLoader loader = instance.getWorldLoader();
            BukkitScheduler scheduler = Bukkit.getScheduler();
            List<World> loadedWorlds = new ArrayList<>();

            Semaphore semaphore = new Semaphore(-(iterations - 1));
            service.submit(() -> { //use executorservice instead of bukkit async so we start immediately
                scheduler.runTask(instance, () -> commandManager.sendStylizedMessage(player, ">green{===Start" +
                        " maploader profiling session===}"));

                profiler.start();
                semaphore.acquireUninterruptibly();
                profiler.stop();

                scheduler.runTask(instance, () -> {
                    commandManager.sendStylizedMessage(player, String.format("Loaded >green{%s} copies of world " +
                            ">green{%s} in >green{~%sms}", iterations, worldName, profiler.getTime()));
                    profiler.reset();

                    commandManager.sendStylizedMessage(player, ">gray{Cleaning up worlds.}");

                    profiler.start();
                    for(World world : loadedWorlds) {
                        loader.unloadWorld(world);
                    }
                    profiler.stop();

                    commandManager.sendStylizedMessage(player, String.format(">gray{Done unloading worlds; " +
                            "~%sms elapsed}", profiler.getTime()));
                    commandManager.sendStylizedMessage(player, ">red{===End maploader profiling session===}");

                    profiler.reset();
                    profilerSemaphore.release();
                });
            });

            for(int i = 0; i < iterations; i++) {
                loader.loadWorld(worldName, world -> {
                    loadedWorlds.add(world);
                    semaphore.release();
                });
            }
        }
        else {
            return ">red{The profiler is already running.}";
        }

        return null;
    }
}
