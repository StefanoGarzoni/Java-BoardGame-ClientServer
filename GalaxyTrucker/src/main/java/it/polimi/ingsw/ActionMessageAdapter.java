package it.polimi.ingsw;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.model.cards.util.Pair;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ActionMessageAdapter implements JsonSerializer<ActionMessage>, JsonDeserializer<ActionMessage> {

    private JsonObject buildTypeDescriptor(Object sample){
        JsonObject json = new JsonObject();
        if(sample == null){
            json.addProperty("_class", Object.class.getName()); //FIXME
            return json;
        }
        Class<?> currClass = sample.getClass();
        json.addProperty("_class", currClass.getName());

        if(sample instanceof List<?> list && !list.isEmpty()){
            json.add("_generic", buildTypeDescriptor(list.getFirst()));
        } else if(sample instanceof Map<?,?> map && !map.isEmpty()){
            Map.Entry<?,?> entry = map.entrySet().iterator().next();
            json.add("_genericKey", buildTypeDescriptor(entry.getKey()));
            json.add("_genericValue", buildTypeDescriptor(entry.getValue()));
        }
        else if(sample instanceof Pair<?,?> pair){
            json.addProperty("_class", Pair.class.getName());
            json.add("_genericFirst", buildTypeDescriptor(pair.getFirst()));
            json.add("_genericSecond", buildTypeDescriptor(pair.getSecond()));
        }
        return json;
    }

    /**
     * Recursive function to parse through nested generic objects I.E. {@code List<List<Card>>}
     * @param object the object to parse
     * @param ctx {@link JsonDeserializationContext}, manages basic serialization
     * @return the complete description of the generic as a json object
     */
    private JsonObject addGenericType(Object object, JsonSerializationContext ctx){
        JsonObject json = buildTypeDescriptor(object); // solo metadati
        json.add("value", ctx.serialize(object));      // il contenuto vero lo metti una sola volta
        return json;
    }

    /**
     * Resolves the json of a nested object with generics
     * @param json the object to parse
     * @return the type of the class as described per json
     * @throws ClassNotFoundException if there is no class with a given name inside the tree
     */
    private Type findType(JsonObject json) throws ClassNotFoundException {
        Class<?> topClass = Class.forName(json.get("_class").getAsString());

        //Takes a single generic
        if(json.has("_generic")){
            JsonElement generic = json.get("_generic");
            //If the generic is a JSON object, we keep going
            Type genericType = findType(generic.getAsJsonObject());
            return TypeToken.getParameterized(topClass, genericType).getType();
        }
        else if(json.has("_genericKey") && json.has("_genericValue")){
            JsonObject keyInfo = json.getAsJsonObject("_genericKey");
            JsonObject valueInfo = json.getAsJsonObject("_genericValue");
            Type keyType = findType(keyInfo);
            Type valueType = findType(valueInfo);
            return TypeToken.getParameterized(topClass, keyType, valueType).getType();
        }

        return topClass;
    }

    @Override
    public JsonElement serialize(ActionMessage am, Type t, JsonSerializationContext ctx){
        JsonObject root = new JsonObject();
        root.addProperty("actionName", am.getActionName());
        root.addProperty("sender", am.getSender());
        root.addProperty("receiver", am.getReceiver());

        JsonObject typeDescriptor = new JsonObject();
        for(String key : am.getKeysParams()) {
            Object val = am.getData(key);
            typeDescriptor.add(key, addGenericType(val, ctx));
        }
        root.add("params", typeDescriptor);
        return root;
    }

    @Override
    public ActionMessage deserialize(JsonElement json, Type t, JsonDeserializationContext ctx) throws JsonParseException{
        JsonObject root = json.getAsJsonObject();
        ActionMessage msg = new ActionMessage(root.get("actionName").getAsString(),
                root.get("sender").getAsString());
        msg.setReceiver(root.get("receiver").getAsString());

        for(Map.Entry<String, JsonElement> e : root.getAsJsonObject("params").entrySet()){
            JsonObject wrapper = e.getValue().getAsJsonObject();
            try {
                Type type = findType(wrapper);
                Object object = ctx.deserialize(wrapper.get("value"), type);
                msg.setData(e.getKey(), object);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
        return msg;
    }
}
