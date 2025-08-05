package it.polimi.ingsw.model.ComponentTile;

import com.google.gson.*;
import it.polimi.ingsw.model.cards.util.ServerCardAdapter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ComponentTileAdapter implements JsonDeserializer<ComponentTile>, JsonSerializer<ComponentTile> {
    private final Map<String, Class<? extends ComponentTile>> nameToClass;
    private final Map<Class<? extends ComponentTile>, String> classToName;

    public ComponentTileAdapter() {
        nameToClass = new HashMap<>();
        classToName = new HashMap<>();

        nameToClass.put("AlienSupportSystem", AlienSupportSystemTile.class);
        nameToClass.put("Batteries", BatteriesTile.class);
        nameToClass.put("cabin", CabinTile.class);
        nameToClass.put("Cannon", CannonTile.class);
        nameToClass.put("Cargo", CargoTile.class);
        nameToClass.put("Engine", EngineTile.class);
        nameToClass.put("Shield",  Shield.class);
        nameToClass.put("Structural", StructuralTile.class);

        nameToClass.forEach((key, val) -> {
            classToName.put(val, key);
        });
    }

    @Override
    public ComponentTile deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        String tileType =  object.get("type").getAsString();
        Class<? extends ComponentTile> tileClass = nameToClass.get(tileType);
        if(tileClass == null) throw new JsonParseException("Unknown tile type: " + tileType);
        return jsonDeserializationContext.deserialize(object, tileClass);
    }

    @Override
    public JsonElement serialize(ComponentTile componentTile, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = jsonSerializationContext.serialize(componentTile, componentTile.getClass()).getAsJsonObject();
        object.addProperty("type", classToName.get(componentTile.getClass()));
        return object;
    }
}
