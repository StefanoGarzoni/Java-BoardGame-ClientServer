package it.polimi.ingsw.model.Cargo.util;

import com.google.gson.*;
import it.polimi.ingsw.model.Cargo.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CargoTypeAdapter implements JsonSerializer<CargoType>, JsonDeserializer<CargoType> {
    private final Map<String, Class<? extends CargoType>> nameToClass;
    private final Map<Class<? extends CargoType>, String> classToName;

    public CargoTypeAdapter() {
        nameToClass = new HashMap<>();
        classToName = new HashMap<>();

        nameToClass.put("RedCargo", RedCargo.class);
        nameToClass.put("GreenCargo", GreenCargo.class);
        nameToClass.put("YellowCargo", YellowCargo.class);
        nameToClass.put("BlueCargo", BlueCargo.class);

        nameToClass.forEach((key, val) -> {
            classToName.put(val, key);
        });
    }

    @Override
    public CargoType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Class<? extends CargoType> cargoTypeClass = nameToClass.get(jsonObject.get("cargoType").getAsString());
        if(cargoTypeClass == null) throw new JsonParseException("Cargo Type not found" + jsonObject.get("cargoType").getAsString());
        return jsonDeserializationContext.deserialize(jsonObject, cargoTypeClass);
    }

    @Override
    public JsonElement serialize(CargoType cargoType, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = jsonSerializationContext.serialize(cargoType, cargoType.getClass()).getAsJsonObject();
        String cargoTypeName = classToName.get(cargoType.getClass());
        if(cargoTypeName == null) throw new JsonParseException("Cargo type not found" + cargoType.getClass());
        jsonObject.addProperty("cargoType", cargoTypeName);
        return jsonObject;
    }
}
