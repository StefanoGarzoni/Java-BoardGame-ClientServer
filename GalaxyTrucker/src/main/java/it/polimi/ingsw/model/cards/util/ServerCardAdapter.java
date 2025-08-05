package it.polimi.ingsw.model.cards.util;

import com.google.gson.*;
import it.polimi.ingsw.model.cards.generic.AbandonedShipCard;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.cards.server.concrete.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ServerCardAdapter implements JsonSerializer<ServerCard>, JsonDeserializer<ServerCard> {
    private final Map<String, Class<? extends ServerCard>> nameToClass;
    private final Map<Class<? extends ServerCard>, String> classToName;

    public ServerCardAdapter(){
        nameToClass = new HashMap<>();
        classToName = new HashMap<>();

        nameToClass.put("AbandonedShip", AbandonedShipServerCard.class);
        nameToClass.put("AbandonedStation", AbandonedStationServerCard.class);
        nameToClass.put("Enslavers", EnslaversServerCard.class);
        nameToClass.put("Epidemic", EpidemicServerCard.class);
        nameToClass.put("MeteorSwarm", MeteorSwarmServerCard.class);
        nameToClass.put("OpenSpace", OpenSpaceServerCard.class);
        nameToClass.put("Pirates", PiratesServerCard.class);
        nameToClass.put("Planet", PlanetServerCard.class);
        nameToClass.put("Smugglers",  SmugglersServerCard.class);
        nameToClass.put("Stardust",  StardustServerCard.class);
        nameToClass.put("WarZone", WarZoneServerCard.class);

        nameToClass.forEach((key, val) -> {
            classToName.put(val, key);
        });
    }

    @Override
    public ServerCard deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        String cardType = object.get("type").getAsString();
        Class<? extends ServerCard> serverCardClass = nameToClass.get(cardType);
        if(serverCardClass == null) throw new JsonParseException("Unknown server card type: " + cardType);
        return jsonDeserializationContext.deserialize(object, serverCardClass);
    }

    @Override
    public JsonElement serialize(ServerCard serverCard, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = jsonSerializationContext.serialize(serverCard, serverCard.getClass()).getAsJsonObject();
        object.addProperty("type", classToName.get(serverCard.getClass()));
        return object;
    }

}
