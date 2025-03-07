package dev.brauw.mapper.export.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;

public class LocationDeserializer extends JsonDeserializer<Location> {
    @Override
    public Location deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        double x = node.get("x").asDouble();
        double y = node.get("y").asDouble();
        double z = node.get("z").asDouble();
        World world = Bukkit.getWorld(node.get("world").asText());

        return new Location(world, x, y, z, 0, 0);
    }
}
