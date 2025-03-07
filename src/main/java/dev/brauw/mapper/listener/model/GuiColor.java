package dev.brauw.mapper.listener.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.bukkit.Color;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public enum GuiColor {

    RED(Color.RED, Material.RED_CONCRETE, "Red"),
    ORANGE(Color.ORANGE, Material.ORANGE_CONCRETE, "Orange"),
    YELLOW(Color.YELLOW, Material.YELLOW_CONCRETE, "Yellow"),
    GREEN(Color.GREEN, Material.GREEN_CONCRETE, "Green"),
    BLUE(Color.BLUE, Material.BLUE_CONCRETE, "Blue"),
    PURPLE(Color.PURPLE, Material.PURPLE_CONCRETE, "Purple"),
    WHITE(Color.WHITE, Material.WHITE_CONCRETE, "White");

    private final Color color;
    private final Material material;
    private final String name;

    public static GuiColor fromColor(Color color) {
        for (GuiColor guiColor : values()) {
            if (guiColor.getColor().equals(color)) {
                return guiColor;
            }
        }
        throw new IllegalArgumentException("No GuiColor found for color " + color);
    }

}
