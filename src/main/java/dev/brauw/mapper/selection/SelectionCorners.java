package dev.brauw.mapper.selection;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;

@Data
@NoArgsConstructor
public class SelectionCorners {
    private Location firstCorner;
    private Location secondCorner;
    
    public SelectionCorners(Location firstCorner) {
        this.firstCorner = firstCorner;
    }
    
    public boolean isComplete() {
        return firstCorner != null && secondCorner != null;
    }
}