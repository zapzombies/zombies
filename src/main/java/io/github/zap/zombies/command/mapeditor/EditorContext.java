package io.github.zap.zombies.command.mapeditor;

import io.github.zap.arenaapi.Disposable;
import io.github.zap.arenaapi.ObjectDisposedException;
import io.github.zap.arenaapi.particle.*;
import io.github.zap.zombies.game.data.map.MapData;
import io.github.zap.zombies.game.data.map.RoomData;
import io.github.zap.zombies.game.data.map.SpawnpointData;
import io.github.zap.zombies.game.data.map.WindowData;
import io.github.zap.zombies.game.data.map.shop.*;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class EditorContext implements Disposable {
    private static final Vector UNIT = new Vector(1, 1, 1);
    private static final VectorProvider[] EMPTY_VECTOR_PROVIDER_ARRAY = new VectorProvider[0];

    public enum Renderables {
        SELECTION(0),
        MAP(1),
        ROOMS(2),
        WINDOWS(3),
        WINDOW_BOUNDS(4),
        SPAWNPOINTS(5),
        DOORS(6),
        DOOR_SIDES(7),
        SHOPS(8);

        @Getter
        private final int index;

        Renderables(int index) {
            this.index = index;
        }
    }

    private static final Shader SELECTION_SHADER = new SolidShader(Particle.REDSTONE, 1,
            new Particle.DustOptions(Color.GREEN, 1));

    private static final Shader MAP_BOUNDS_SHADER = new SolidShader(Particle.REDSTONE, 1,
            new Particle.DustOptions(Color.RED, 2));

    private static final Shader ROOM_SHADER = new SolidShader(Particle.REDSTONE, 1,
            new Particle.DustOptions(Color.WHITE, 1));

    private static final Shader WINDOW_SHADER = new SolidShader(Particle.REDSTONE, 1,
            new Particle.DustOptions(Color.BLUE, 1));

    private static final Shader WINDOW_BOUNDS_SHADER = new SolidShader(Particle.REDSTONE, 1,
            new Particle.DustOptions(Color.PURPLE, 2));

    private static final Shader SPAWNPOINT_SHADER = new SolidShader(Particle.REDSTONE, 3,
            new Particle.DustOptions(Color.YELLOW, 1));

    private static final Shader DOOR_SHADER = new SolidShader(Particle.REDSTONE, 1,
            new Particle.DustOptions(Color.BLACK, 1));

    private static final Shader DOOR_TRIGGER_SHADER = new SolidShader(Particle.REDSTONE, 1,
            new Particle.DustOptions(Color.GRAY, 1));

    private static final Shader SHOP_SHADER = new SolidShader(Particle.REDSTONE, 1,
            new Particle.DustOptions(Color.AQUA, 1));

    private boolean disposed = false;

    private class SelectionRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return SELECTION_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            if (firstClicked != null) {
                Vector target = getTarget();

                return new CompositeProvider(new RectangularPrism(getSelection(), 1),
                        new RectangularPrism(BoundingBox.of(target, target.clone().add(UNIT)), 2));
            }

            return null;
        }
    }

    private class MapRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return MAP_BOUNDS_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            return map == null ? null : new RectangularPrism(map.getMapBounds(), 0.25);
        }
    }

    private class RoomRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return ROOM_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            if(map != null) {
                List<VectorProvider> vectorProviders = new ArrayList<>();

                for(RoomData room : map.getRooms()) {
                    for(BoundingBox boundingBox : room.getBounds()) {
                        vectorProviders.add(new RectangularPrism(boundingBox, 0.5));
                    }
                }

                return new CompositeProvider(vectorProviders.toArray(EMPTY_VECTOR_PROVIDER_ARRAY));
            }

            return null;
        }
    }

    private class WindowRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return WINDOW_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            if(map != null) {
                List<VectorProvider> vectorProviders = new ArrayList<>();

                for(RoomData room : map.getRooms()) {
                    for(WindowData window : room.getWindows()) {
                        vectorProviders.add(new RectangularPrism(window.getFaceBounds(), 2));
                    }
                }

                return new CompositeProvider(vectorProviders.toArray(EMPTY_VECTOR_PROVIDER_ARRAY));
            }

            return null;
        }
    }

    private class WindowBoundsRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return WINDOW_BOUNDS_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            if(map != null) {
                List<VectorProvider> vectorProviders = new ArrayList<>();

                for(RoomData room : map.getRooms()) {
                    for(WindowData window : room.getWindows()) {
                        for(BoundingBox bounds : window.getInteriorBounds()) {
                            vectorProviders.add(new RectangularPrism(bounds, 1));
                        }
                    }
                }

                return new CompositeProvider(vectorProviders.toArray(EMPTY_VECTOR_PROVIDER_ARRAY));
            }

            return null;
        }
    }

    private class SpawnpointRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return SPAWNPOINT_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            if(map != null) {
                List<VectorProvider> vectorProviders = new ArrayList<>();

                for(RoomData room : map.getRooms()) {
                    for(SpawnpointData spawnpoint : room.getSpawnpoints()) {
                        Vector spawn = spawnpoint.getSpawn();

                        if(spawn != null) {
                            vectorProviders.add(new RectangularPrism(BoundingBox.of(spawn, spawn.clone().add(UNIT)), 3));
                        }
                    }

                    for(WindowData windowData : room.getWindows()) {
                        for(SpawnpointData spawnpoint : windowData.getSpawnpoints()) {
                            Vector spawn = spawnpoint.getSpawn();

                            if(spawn != null) {
                                vectorProviders.add(new RectangularPrism(BoundingBox.of(spawn, spawn.clone().add(UNIT)), 3));
                            }
                        }
                    }
                }

                return new CompositeProvider(vectorProviders.toArray(EMPTY_VECTOR_PROVIDER_ARRAY));
            }

            return null;
        }
    }

    private class DoorRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return DOOR_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            if(map != null) {
                List<VectorProvider> vectorProviders = new ArrayList<>();

                for(DoorData door : map.getDoors()) {
                    for(BoundingBox bounds : door.getDoorBounds()) {
                        vectorProviders.add(new RectangularPrism(bounds, 2));
                    }
                }

                return new CompositeProvider(vectorProviders.toArray(EMPTY_VECTOR_PROVIDER_ARRAY));
            }

            return null;
        }
    }

    private class DoorSideRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return DOOR_TRIGGER_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            if(map != null) {
                List<VectorProvider> vectorProviders = new ArrayList<>();

                for(DoorData door : map.getDoors()) {
                    for(DoorSide side : door.getDoorSides()) {
                        vectorProviders.add(new RectangularPrism(side.getTriggerBounds(), 2));
                    }
                }

                return new CompositeProvider(vectorProviders.toArray(EMPTY_VECTOR_PROVIDER_ARRAY));
            }

            return null;
        }
    }

    private class ShopRenderable extends ShadedRenderable {
        @Override
        public Shader getShader() {
            return SHOP_SHADER;
        }

        @Override
        public VectorProvider vectorProvider() {
            if(map != null && map.getShops().size() > 0) {
                List<VectorProvider> vectorProviders = new ArrayList<>();

                for(ShopData shop : map.getShops()) {
                    if (shop instanceof BlockShopData blockShopData) {
                        vectorProviders.add(new RectangularPrism(BoundingBox.of(blockShopData.getBlockLocation(),
                                blockShopData.getBlockLocation().clone().add(UNIT)), 1));
                    }
                    if (shop instanceof ArmorStandShopData armorShopData) {
                        vectorProviders.add(new RectangularPrism(BoundingBox.of(armorShopData.getRootLocation(),
                                armorShopData.getRootLocation().clone().add(UNIT)), 1));
                    }
                }

                return new CompositeProvider(vectorProviders.toArray(EMPTY_VECTOR_PROVIDER_ARRAY));
            }

            return null;
        }
    }

    private final List<Renderable> renderables = new ArrayList<>();

    @Getter
    private final Player player;

    @Getter
    private MapData map;

    private Vector firstClicked = null;
    private Vector secondClicked = null;

    private final Renderer renderer;

    public EditorContext(Player player) {
        this.player = player;

        renderer = new SimpleRenderer(player.getWorld(), 0, 10);
        addRenderable(new SelectionRenderable());
        addRenderable(new MapRenderable());
        addRenderable(new RoomRenderable());
        addRenderable(new WindowRenderable());
        addRenderable(new WindowBoundsRenderable());
        addRenderable(new SpawnpointRenderable());
        addRenderable(new DoorRenderable());
        addRenderable(new DoorSideRenderable());
        addRenderable(new ShopRenderable());
        renderer.start();
    }

    private void addRenderable(Renderable renderable) {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        renderer.add(renderable);
        renderables.add(renderable);
    }

    public void updateRenderable(Renderables renderable) {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        renderables.get(renderable.index).update();
    }

    public void updateAllRenderables() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        for(Renderable renderable : renderables) {
            renderable.update();
        }
    }

    public void setMap(MapData map) {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        if(this.map != map) {
            this.map = map;

            for(Renderable renderable : renderables) {
                renderable.update();
            }
        }
    }

    public void handleClicked(Block at) {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        Vector clickedVector = at.getLocation().toVector();

        if(firstClicked == null && secondClicked == null) {
            firstClicked = clickedVector;
        }
        else if(firstClicked != null && secondClicked == null) {
            secondClicked = clickedVector;
        }
        else if(firstClicked != null) {
            firstClicked = secondClicked;
            secondClicked = clickedVector;
        }

        renderables.get(Renderables.SELECTION.index).update();
    }

    /**
     * Returns a new BoundingBox representing the current bounds selection made by the player.
     * @return A new BoundingBox representing the selection the player currently has
     */
    public BoundingBox getSelection() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        if(firstClicked != null && secondClicked != null) {
            return BoundingBox.of(firstClicked, secondClicked).expandDirectional(UNIT);
        }
        else if(firstClicked != null) {
            return BoundingBox.of(firstClicked, firstClicked).expandDirectional(UNIT);
        }

        return null;
    }

    public Vector getTarget() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        if(firstClicked != null && secondClicked == null) {
            return firstClicked.clone();
        }
        else if(firstClicked != null) {
            return secondClicked.clone();
        }

        return null;
    }

    public Vector getFirst() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        return firstClicked == null ? null : firstClicked.clone();
    }

    public Vector getSecond() {
        if(disposed) {
            throw new ObjectDisposedException();
        }

        return secondClicked == null ? getFirst() : secondClicked.clone();
    }

    @Override
    public void dispose() {
        if(disposed) {
            return;
        }

        renderer.stop();
        disposed = true;
    }
}
