package dev.brauw.mapper.export.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.bukkit.Location;

import java.io.IOException;

public class LocationDeserializer extends JsonDeserializer<Location> {
    @Override
    public Location deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        double x = node.get("x").asDouble();
        double y = node.get("y").asDouble();
        double z = node.get("z").asDouble();
        // Absent in files written before rotation was serialized, and omitted when zero.
        float yaw = node.has("yaw") ? (float) node.get("yaw").asDouble() : 0f;
        float pitch = node.has("pitch") ? (float) node.get("pitch").asDouble() : 0f;
        return new Location(null, x, y, z, yaw, pitch);
    }
}
