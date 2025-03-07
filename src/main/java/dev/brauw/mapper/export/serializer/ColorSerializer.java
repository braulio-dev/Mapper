package dev.brauw.mapper.export.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bukkit.Color;

import java.io.IOException;

public class ColorSerializer extends JsonSerializer<Color> {
    @Override
    public void serialize(Color color, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("red", color.getRed());
        jsonGenerator.writeNumberField("green", color.getGreen());
        jsonGenerator.writeNumberField("blue", color.getBlue());
        jsonGenerator.writeEndObject();
    }
}
