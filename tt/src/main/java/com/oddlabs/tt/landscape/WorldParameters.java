package com.oddlabs.tt.landscape;

import com.oddlabs.matchmaking.Game;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.io.Serializable;

public final class WorldParameters implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final @NonNull String map_code;
    private final int initial_unit_count;
    private final int max_unit_count;
    private final int initial_game_speed;
    private final int map_size;
    private final int max_building_count;
    private final boolean ships;

    public WorldParameters(int initial_game_speed, @NonNull String map_code, int initial_unit_count,
            int max_unit_count) {
        this(initial_game_speed, map_code, initial_unit_count, max_unit_count, Game.SIZE_NONE);
    }

    public WorldParameters(int initial_game_speed, @NonNull String map_code, int initial_unit_count, int max_unit_count,
            int map_size) {
        this(builder().initialGameSpeed(initial_game_speed).mapcode(map_code).initialUnitCount(
                initial_unit_count).maxUnitCount(max_unit_count).mapSize(map_size));
    }

    private WorldParameters(@NonNull Builder b) {
        this.map_code = b.mapcode;
        this.initial_unit_count = b.initial_unit_count;
        this.max_unit_count = b.max_unit_count;
        this.initial_game_speed = b.initial_game_speed;
        this.map_size = b.map_size;
        this.max_building_count = b.max_building_count;
        this.ships = b.ships;
    }

    public static @NonNull Builder builder() {
        return new Builder();
    }

    public @NonNull String getMapcode() {
        return map_code;
    }

    public int getInitialUnitCount() {
        return initial_unit_count;
    }

    public int getMaxUnitCount() {
        return max_unit_count;
    }

    public int getInitialGameSpeed() {
        return initial_game_speed;
    }

    public int getMapSize() {
        return map_size;
    }

    public int getMaxBuildingCount() {
        return max_building_count;
    }

    public boolean isShipsEnabled() {
        return ships;
    }

    public static final class Builder {
        private @NonNull String mapcode = "";
        private int initial_unit_count;
        private int max_unit_count;
        private int initial_game_speed;
        private int map_size = Game.SIZE_NONE;
        private int max_building_count = Game.DEFAULT_MAX_BUILDING_COUNT;
        private boolean ships;

        private Builder() {
        }

        public @NonNull Builder mapcode(@NonNull String mapcode) {
            this.mapcode = mapcode;
            return this;
        }

        public @NonNull Builder initialUnitCount(int v) {
            this.initial_unit_count = v;
            return this;
        }

        public @NonNull Builder maxUnitCount(int v) {
            this.max_unit_count = v;
            return this;
        }

        public @NonNull Builder initialGameSpeed(int v) {
            this.initial_game_speed = v;
            return this;
        }

        public @NonNull Builder mapSize(int v) {
            this.map_size = v;
            return this;
        }

        public @NonNull Builder maxBuildingCount(int v) {
            this.max_building_count = v;
            return this;
        }

        public @NonNull Builder ships(boolean v) {
            this.ships = v;
            return this;
        }

        public @NonNull WorldParameters build() {
            return new WorldParameters(this);
        }
    }
}
