package dev.brauw.mapper.export.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.bukkit.Color;

import java.io.IOException;

public class ColorDeserializer extends JsonDeserializer<Color> {
    @Override
    public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        // Extract RGB values with null checks
        int red = node.has("red") ? node.get("red").asInt() : 255;
        int green = node.has("green") ? node.get("green").asInt() : 255;
        int blue = node.has("blue") ? node.get("blue").asInt() : 255;

        return Color.fromRGB(red, green, blue);
    }
}