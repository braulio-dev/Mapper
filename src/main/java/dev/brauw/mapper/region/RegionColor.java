package dev.brauw.mapper.region;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public enum RegionColor {

    RED(Color.RED, Material.RED_CONCRETE, "Red"),
    ORANGE(Color.ORANGE, Material.ORANGE_CONCRETE, "Orange"),
    YELLOW(Color.YELLOW, Material.YELLOW_CONCRETE, "Yellow"),
    GREEN(Color.GREEN, Material.GREEN_CONCRETE, "Green"),
    BLUE(Color.BLUE, Material.BLUE_CONCRETE, "Blue"),
    PURPLE(Color.PURPLE, Material.PURPLE_CONCRETE, "Purple"),
    WHITE(Color.WHITE, Material.WHITE_CONCRETE, "White");

    @JsonIgnore
    private final Color bukkitColor;
    @JsonIgnore
    private final Material material;
    @JsonIgnore
    private final String name;

    public static RegionColor fromColor(Color color) {
        for (RegionColor regionCOlor : values()) {
            if (regionCOlor.getBukkitColor().equals(color)) {
                return regionCOlor;
            }
        }
        throw new IllegalArgumentException("No GuiColor found for color " + color);
    }

}
