package it.polimi.ingsw.model.FileUploaders;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.model.ComponentTile.*;

import java.io.*;
import java.util.ArrayList;

public class TilesLoader {

    /** Returns all tiles present as Json object in the selected file
     *
     * @param tilesFilePath path of the file containing the Json objects of the tiles
     * @return list of loaded tiles
     * @throws FileNotFoundException if the file does not exist
     */
    public static ArrayList<ComponentTile> uploadAllTiles(String tilesFilePath) throws FileNotFoundException {
        ArrayList<ComponentTile> componentTiles = new ArrayList<>();

        JsonArray allTilesList;
        try(InputStream inputStream = TilesLoader.class.getClassLoader().getResourceAsStream(tilesFilePath)){
            if(inputStream == null){throw new FileNotFoundException("Tiles file could not be found");}
            allTilesList = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonArray();
        } catch (IOException e) {
            System.err.println("No tiles file found on classpath");
            throw new RuntimeException(e);
        }

        for(JsonElement tile : allTilesList){
            componentTiles.add(createComponentTile(tile.getAsJsonObject()));
        }

        return componentTiles;
    }

    /** Creates a ComponentTile object from a JsonObject representing a well-formed ComponentTile
     *
     * @param jsonTile object of the tile to be created
     * @return ComponentTile represented by the jsonTile
     */
    private static ComponentTile createComponentTile(JsonObject jsonTile){
        String tileType = jsonTile.get("tileType").getAsString();

        Connector[] connectors = getConnectorArray(jsonTile.get("connectorsList").getAsJsonArray());

        switch (tileType){
            case "Cargo" :
                return new CargoTile(
                        jsonTile.get("fileName").getAsString(),
                        connectors[0],
                        connectors[1],
                        connectors[2],
                        connectors[3],
                        jsonTile.get("isSpecial").getAsBoolean(),
                        jsonTile.get("capacity").getAsInt()
                );

            case "Cannon" :
                return new CannonTile(
                        jsonTile.get("fileName").getAsString(),
                        connectors[0],
                        connectors[1],
                        connectors[2],
                        connectors[3],
                        jsonTile.get("doubleCannon").getAsBoolean()
                );

            case "Batteries" :
                return new BatteriesTile(
                        jsonTile.get("fileName").getAsString(),
                        connectors[0],
                        connectors[1],
                        connectors[2],
                        connectors[3],
                        jsonTile.get("capacity").getAsInt()
                );

            case "Shield" :
                return new Shield(
                        jsonTile.get("fileName").getAsString(),
                        connectors[0],
                        connectors[1],
                        connectors[2],
                        connectors[3]
                );

            case "Structural" :
                return new StructuralTile(
                        jsonTile.get("fileName").getAsString(),
                        connectors[0],
                        connectors[1],
                        connectors[2],
                        connectors[3]
                );

            case "Engine":
                return new EngineTile(
                        jsonTile.get("fileName").getAsString(),
                        connectors[0],
                        connectors[1],
                        connectors[2],
                        connectors[3],
                        jsonTile.get("doubleEngine").getAsBoolean()
                );

            case "AlienSupportSystem" :
                return new AlienSupportSystemTile(
                        jsonTile.get("fileName").getAsString(),
                        connectors[0],
                        connectors[1],
                        connectors[2],
                        connectors[3],
                        jsonTile.get("brownAlienSupport").getAsBoolean()
                );

            case "Cabin" :
                return new CabinTile(
                        jsonTile.get("fileName").getAsString(),
                        connectors[0],
                        connectors[1],
                        connectors[2],
                        connectors[3]
                );

            default:
                System.out.print("This tiles does not exists in the game");
                System.out.println(jsonTile);
                return  null;
        }

    }

    /** Creates an array of Connectors, from a well-formed array of connectors
     *
     * @param connectorsArray array containing integer values, representing number of connections of a connector
     * @return array of Connectors
     */
    private static Connector[] getConnectorArray(JsonArray connectorsArray){
        Connector[] connectors = new Connector[4];

        for(int i = 0; i < 4; i++){
            connectors[i] = new Connector(connectorsArray.get(i).getAsInt());
        }

        return connectors;
    }
}
