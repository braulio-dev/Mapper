package dev.brauw.mapper.listener.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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