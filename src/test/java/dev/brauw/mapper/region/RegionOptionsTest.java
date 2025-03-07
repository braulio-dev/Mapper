package dev.brauw.mapper.region;

import org.junit.jupiter.api.Test;
import org.bukkit.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RegionOptionsTest {

    @Test
    void createsRegionOptionsWithSpecificColor() {
        RegionColor color = RegionColor.RED;
        RegionOptions options = RegionOptions.builder()
                .color(color)
                .build();

        assertEquals(color, options.getColor());
    }

    @Test
    void usesWhiteColorByDefault() {
        RegionOptions options = RegionOptions.builder().build();

        assertEquals(RegionColor.WHITE, options.getColor());
    }

    @Test
    void throwsExceptionWhenColorIsNull() {
        assertThrows(NullPointerException.class, () ->
                RegionOptions.builder()
                        .color(null)
                        .build()
        );
    }

    @Test
    void builderCreatesEqualObjectsWithSameColor() {
        RegionColor color = RegionColor.BLUE;
        RegionOptions options1 = RegionOptions.builder().color(color).build();
        RegionOptions options2 = RegionOptions.builder().color(color).build();

        assertEquals(options1, options2);
        assertEquals(options1.hashCode(), options2.hashCode());
    }

}
