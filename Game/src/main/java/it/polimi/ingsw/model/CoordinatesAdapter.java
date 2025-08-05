package it.polimi.ingsw.model;

import com.google.gson.*;

import java.lang.reflect.Type;

public class CoordinatesAdapter implements JsonDeserializer<Coordinates>, JsonSerializer<Coordinates> {

    @Override
    public Coordinates deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if(jsonElement.isJsonPrimitive()){
            String s = jsonElement.getAsString();
            return Coordinates.fromString(s);
        }
        else if(jsonElement.isJsonObject()){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int row = jsonObject.get("row").getAsInt();
            int col = jsonObject.get("col").getAsInt();
            return new Coordinates(row, col);
        }
        throw new JsonParseException(jsonElement.toString());
    }

    @Override
    public JsonElement serialize(Coordinates coordinates, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(coordinates.toString());
    }
}
