package it.polimi.ingsw.model.FileUploaders;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.model.Cargo.*;
import it.polimi.ingsw.model.Deck;
import it.polimi.ingsw.model.Direction;
import it.polimi.ingsw.model.cards.client.ClientCard;
import it.polimi.ingsw.model.cards.client.concrete.*;
import it.polimi.ingsw.model.cards.server.concrete.AbandonedShipServerCard;
import it.polimi.ingsw.model.cards.server.concrete.EnslaversServerCard;
import it.polimi.ingsw.model.cards.server.concrete.SmugglersServerCard;
import it.polimi.ingsw.model.cards.server.ServerCard;
import it.polimi.ingsw.model.cards.server.concrete.*;
import it.polimi.ingsw.model.cards.util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CardsLoader {
    private final Random random = new Random();

    private final Map<Integer, ArrayList<ClientCard>> clientCardsByLevel = new HashMap<>();
    private final Map<Integer, ArrayList<ServerCard>> serverCardsByLevel = new HashMap<>();

    /** Creates the CardLoaders class and uploads all possible cards from a well-formed JSON
     *
     * @param cardsFilePath path of the file containing the well-formed JSON
     * @throws FileNotFoundException if the filePath does not exist
     */
    public CardsLoader(String cardsFilePath) throws FileNotFoundException {
        uploadAllCards(cardsFilePath);
    }

    /** Returns three decks made by random extracted card from the well-formed
     * card's JSON file
     *
     * @param gameLevel level of the game, in order to correctly create decks
     * @return three Deck objects
     */
    public ArrayList<Deck> getRandomServerDecksFromCardsJson(int gameLevel) {
        ArrayList<Deck> decksToReturn = new ArrayList<>();
        ArrayList<ServerCard> deckTemporaryCards = new ArrayList<>();     // temporarily stores cart for a new deck

        // in this project, gameLevel = 1 is the "First Flight" version
        if(gameLevel == 1){
            deckTemporaryCards.addAll(serverCardsByLevel.get(0));
            serverCardsByLevel.get(0).clear();
            decksToReturn.add(new Deck(deckTemporaryCards));
        }
        else{
            // 3 decks generation
            int randomCardIndex;
            ServerCard selectedCard;
            for(int i = 0; i < 3; i++){
                // 1 card of Level 1
                do{
                    // this bound is because first flight cards has the same drawn probability of first level cards
                    int firstLevelCardsNumber = serverCardsByLevel.get(0).size() + serverCardsByLevel.get(1).size();
                    randomCardIndex = random.nextInt(firstLevelCardsNumber);

                    if(randomCardIndex < serverCardsByLevel.get(0).size()){
                        selectedCard = serverCardsByLevel.get(0).remove(randomCardIndex);
                        if(selectedCard == null)
                            serverCardsByLevel.get(0).remove(randomCardIndex);
                    }
                    else{
                        selectedCard = serverCardsByLevel.get(1).get(randomCardIndex - serverCardsByLevel.get(0).size());
                        if(selectedCard == null)
                            serverCardsByLevel.get(1).remove(randomCardIndex);
                    }
                } while (selectedCard == null);
                deckTemporaryCards.add(selectedCard);

                // 1 card of Level 2
                do{
                    randomCardIndex = random.nextInt(serverCardsByLevel.get(2).size());
                    selectedCard = serverCardsByLevel.get(2).remove(randomCardIndex);

                    if(selectedCard == null)
                        serverCardsByLevel.get(2).remove(randomCardIndex);
                } while (selectedCard == null);
                deckTemporaryCards.add(selectedCard);

                // 1 card of Level 2
                do{
                    randomCardIndex = random.nextInt(serverCardsByLevel.get(2).size());
                    selectedCard = serverCardsByLevel.get(2).remove(randomCardIndex);

                    if(selectedCard == null)
                        serverCardsByLevel.get(2).remove(randomCardIndex);
                } while (selectedCard == null);
                deckTemporaryCards.add(selectedCard);

                decksToReturn.add(new Deck(deckTemporaryCards));
                deckTemporaryCards.clear();     // empty the temporary card list, for the next deck to be created
            }
        }

        return decksToReturn;
    }

    /** Returns a ClientCard with the selected fileName
     *
     * @param fileName of the card to be returned
     * @return selected ClientCard, null if not present
     */
    public ClientCard getClientCard(String fileName){
        //FIXME questo array è sempre vuoto (il metodo è statico e la mappa non viene riempita)
        for(ArrayList<ClientCard> clientCard : clientCardsByLevel.values()){
            ClientCard cardToReturn = clientCard.stream().filter((card) -> card.getFileName().equals(fileName)).findFirst().orElse(null);
            if(cardToReturn != null) { return cardToReturn; }
        }
        return null;
    }

    /** Reads all cards from a well formatted JSON file and creates a Card for every card in the file
     *
     * @param cardsFilePath path of the JSON file containing all the cards
     * @throws FileNotFoundException if the selected file doesn't exist
     */
    private void uploadAllCards(String cardsFilePath) throws FileNotFoundException {
        // creates a JsonObject containing all cards (based on the gameLevel)
        JsonArray cardsListByLevel;
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(cardsFilePath)){
            if(inputStream == null){throw new FileNotFoundException("Cards file could not be found");}
            cardsListByLevel = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonArray();
        } catch (IOException e){
            System.err.println("No cards file found on classpath: " + System.getProperty("java.class.path"));
            throw new FileNotFoundException();
        }

        /* level 0 cards */
        JsonArray firstFlightCards = cardsListByLevel.get(0).getAsJsonObject().get("cards").getAsJsonArray();

        /* level 1 cards */
        JsonArray firstLevelCards = cardsListByLevel.get(1).getAsJsonObject().get("cards").getAsJsonArray();

        /* level 2 cards */
        JsonArray secondLevelCards = cardsListByLevel.get(2).getAsJsonObject().get("cards").getAsJsonArray();

        /* inserts all Server and Client cards into the maps, by level */

        // first flight cards
        ArrayList<ClientCard> zeroLevelClientCards = new ArrayList<>();
        ArrayList<ServerCard> zeroLevelServerCards = new ArrayList<>();
        for(JsonElement jsonCard : firstFlightCards){
            zeroLevelClientCards.add(createNewClientCard(jsonCard.getAsJsonObject(), 0));
            zeroLevelServerCards.add(createNewServerCard(jsonCard.getAsJsonObject(), 0));
        }
        clientCardsByLevel.put(0, zeroLevelClientCards);
        serverCardsByLevel.put(0, zeroLevelServerCards);

        // first level cards
        ArrayList<ClientCard> firstLevelClientCards = new ArrayList<>();
        ArrayList<ServerCard> firstLevelServerCards = new ArrayList<>();
        for(JsonElement jsonCard : firstLevelCards){
            firstLevelClientCards.add(createNewClientCard(jsonCard.getAsJsonObject(), 1));
            firstLevelServerCards.add(createNewServerCard(jsonCard.getAsJsonObject(), 1));
        }
        clientCardsByLevel.put(1, firstLevelClientCards);
        serverCardsByLevel.put(1, firstLevelServerCards);

        // second level cards
        ArrayList<ClientCard> secondLevelClientCards = new ArrayList<>();
        ArrayList<ServerCard> secondLevelServerCards = new ArrayList<>();
        for(JsonElement jsonCard : secondLevelCards){
            secondLevelClientCards.add(createNewClientCard(jsonCard.getAsJsonObject(), 2));
            secondLevelServerCards.add(createNewServerCard(jsonCard.getAsJsonObject(), 2));
        }
        clientCardsByLevel.put(2, secondLevelClientCards);
        serverCardsByLevel.put(2, secondLevelServerCards);
}

    /** Creates a ClientCard object from a JsonObject representing a card
     *
     * @param cardObject of the card to be created
     * @param cardLevel level of the card
     * @return Card represented by the cardObject
     */
    private ClientCard createNewClientCard(JsonObject cardObject, int cardLevel){
        switch (cardObject.get("cardType").getAsString()){
            case "Smugglers" :
                return new SmugglersClientCard(
                        cardObject.get("cargoPenalty").getAsInt(),
                        createCargoPrize(cardObject.get("cargoPrize").getAsJsonArray()),
                        cardObject.get("flightDayLoss").getAsInt(),
                        cardObject.get("requiredFirePower").getAsInt(),
                        cardLevel,
                        cardObject.get("fileName").getAsString()
                );

            case "MeteorSwarm" :
                return new MeteorSwarmClientCard(
                        createMeteorList(cardObject.get("meteorList").getAsJsonArray()),
                        cardLevel,
                        cardObject.get("fileName").getAsString()
                );

            case "Stardust" :
                return new StardustClientCard(
                        cardLevel,
                        cardObject.get("fileName").getAsString()
                );

            case "OpenSpace" :
                return new OpenSpaceClientCard(
                        cardLevel,
                        cardObject.get("fileName").getAsString()
                );

            case "Planet" :
                return new PlanetClientCard(
                        createPlanetList(cardObject.get("planets").getAsJsonArray()),
                        cardObject.get("flightDayLoss").getAsInt(),
                        cardLevel,
                        cardObject.get("fileName").getAsString()
                );

            case "WarZone" :
                return new WarZoneClientCard(
                        cardObject.get("cargoLoss").getAsInt(),
                        cardObject.get("crewLoss").getAsInt(),
                        cardObject.get("flightDayLoss").getAsInt(),
                        createFireShotList(cardObject.get("shotsList").getAsJsonArray()),
                        cardLevel,
                        cardObject.get("fileName").getAsString()
                );

            case "AbandonedShip" :
                return new AbandonedShipClientCard(
                        cardObject.get("creditReward").getAsInt(),
                        cardObject.get("flightDayLoss").getAsInt(),
                        cardObject.get("crewLoss").getAsInt(),
                        cardLevel,
                        cardObject.get("fileName").getAsString()
                );

            case "AbandonedStation" :
                return new AbandonedStationClientCard(
                        cardObject.get("flightDayLoss").getAsInt(),
                        cardObject.get("requiredCrew").getAsInt(),
                        createCargoPrize(cardObject.get("cargoPrize").getAsJsonArray()),
                        cardObject.get("fileName").getAsString(),
                        cardLevel
                );

            case "Enslavers" :
                return new EnslaversClientCard(
                        cardObject.get("creditPrize").getAsInt(),
                        cardObject.get("crewPenalty").getAsInt(),
                        cardObject.get("flightDayLoss").getAsInt(),
                        cardObject.get("requiredFirePower").getAsInt(),
                        cardLevel,
                        cardObject.get("fileName").getAsString()
                );

            case "Pirates" :
                return new PiratesClientCard(
                        createFireShotList(cardObject.get("shotPenalty").getAsJsonArray()),
                        cardObject.get("creditPrize").getAsInt(),
                        cardObject.get("flightDayLoss").getAsInt(),
                        cardObject.get("requiredFirePower").getAsInt(),
                        cardLevel,
                        cardObject.get("fileName").getAsString()
                );

            case "Epidemic" :
                return new EpidemicClientCard(
                        cardLevel,
                        cardObject.get("fileName").getAsString()
                );

            default:
                System.out.print("This card does not exists in the game");
                System.out.println(cardObject);
                return null;
        }
    }

    /** Creates a ServerCard object from a JsonObject representing a card
     *
     * @param cardObject of the card to be created
     * @param cardLevel level of the card
     * @return Card represented by the cardObject
     */
    private ServerCard createNewServerCard(JsonObject cardObject, int cardLevel){
        switch (cardObject.get("cardType").getAsString()){
            case "Smugglers" :
                return new SmugglersServerCard(
                        cardObject.get("fileName").getAsString(),
                        cardObject.get("cargoPenalty").getAsInt(),
                        createCargoPrize(cardObject.get("cargoPrize").getAsJsonArray()),
                        cardObject.get("flightDayLoss").getAsInt(),
                        cardObject.get("requiredFirePower").getAsInt(),
                        cardLevel
                );

            case "MeteorSwarm" :
                return new MeteorSwarmServerCard(
                        cardObject.get("fileName").getAsString(),
                        cardLevel,
                        createMeteorList(cardObject.get("meteorList").getAsJsonArray())
                );

            case "Stardust" :
                return new StardustServerCard(
                        cardLevel,
                        cardObject.get("fileName").getAsString()
                );

            case "OpenSpace" :
                return new OpenSpaceServerCard(
                        cardObject.get("fileName").getAsString(),
                        cardLevel
                );

            case "Planet" :
                return new PlanetServerCard(
                        cardObject.get("fileName").getAsString(),
                        cardLevel,
                        createPlanetList(cardObject.get("planets").getAsJsonArray()),
                        cardObject.get("flightDayLoss").getAsInt()
                );

            case "WarZone" :
                return new WarZoneServerCard(
                        cardObject.get("cargoLoss").getAsInt(),
                        cardObject.get("crewLoss").getAsInt(),
                        cardObject.get("flightDayLoss").getAsInt(),
                        createFireShotList(cardObject.get("shotsList").getAsJsonArray()),
                        cardLevel,
                        cardObject.get("fileName").getAsString()
                );

            case "AbandonedShip" :
                return new AbandonedShipServerCard(
                        cardObject.get("fileName").getAsString(),
                        cardLevel,
                        cardObject.get("creditReward").getAsInt(),
                        cardObject.get("flightDayLoss").getAsInt(),
                        cardObject.get("crewLoss").getAsInt()
                );

            case "AbandonedStation" :
                return new AbandonedStationServerCard(
                        createCargoPrize(cardObject.get("cargoPrize").getAsJsonArray()),
                        cardObject.get("flightDayLoss").getAsInt(),
                        cardObject.get("requiredCrew").getAsInt(),
                        cardLevel,
                        cardObject.get("fileName").getAsString()
                );

            case "Enslavers" :
                return new EnslaversServerCard(
                        cardObject.get("fileName").getAsString(),
                        cardLevel,
                        cardObject.get("crewPenalty").getAsInt(),
                        cardObject.get("creditPrize").getAsInt(),
                        cardObject.get("requiredFirePower").getAsInt(),
                        cardObject.get("flightDayLoss").getAsInt()
                );

            case "Pirates" :
                return new PiratesServerCard(
                        cardObject.get("fileName").getAsString(),
                        cardLevel,
                        createFireShotList(cardObject.get("shotPenalty").getAsJsonArray()),
                        cardObject.get("creditPrize").getAsInt(),
                        cardObject.get("flightDayLoss").getAsInt(),
                        cardObject.get("requiredFirePower").getAsInt()
                );

            case "Epidemic" :
                return new EpidemicServerCard(
                        cardObject.get("fileName").getAsString(),
                        cardLevel
                );

            default:
                System.out.print("This card does not exists in the game");
                System.out.println(cardObject);
                return null;
        }
    }

    /** Produces a list of CargoType elements, based on the input JsonArray.
     *
     * @param cargoPrizeArray JsonArray of strings representing the color of each cargo.
     *                        "y" -> YellowCargo,
     *                        "g" -> GreenCargo,
     *                        "r" -> RedCargo,
     *                        "b" -> BlueCargo
     * @return ArrayList of correspondent CargoTypes
     */
    private ArrayList<CargoType> createCargoPrize(JsonArray cargoPrizeArray){
        ArrayList<CargoType> cargosToReturn = new ArrayList<>();

        for(JsonElement cargo : cargoPrizeArray){
            switch (cargo.getAsString()){
                case "y":
                    cargosToReturn.add(new YellowCargo());
                    break;

                case "g":
                    cargosToReturn.add(new GreenCargo());
                    break;

                case "b":
                    cargosToReturn.add(new BlueCargo());
                    break;

                case "r":
                    cargosToReturn.add(new RedCargo());
                    break;
            }
        }

        return cargosToReturn;
    }

    /** Produces a list of MeteorShot elements, based on the input JsonArray.
     *
     * @param meteorShotsArray JsonArray of objects that contains strings of Size and direction of the meteor.
     *                        The possible sizes are "SMALL" and "BIG"
     *                        The possible directions are "NORTH", "EAST", "SOUTH", "WEST"
     * @return ArrayList of correspondent MeteorShots
     */
    private ArrayList<MeteorShot> createMeteorList(JsonArray meteorShotsArray){
        ArrayList<MeteorShot> meteorShotsToReturn = new ArrayList<>();

        for(JsonElement meteorShot : meteorShotsArray){
            Pair<ShotSize, Direction> shotSizeAndDirection = getShotSizeAndDirection(meteorShot);

            meteorShotsToReturn.add(
                    new MeteorShot(
                            shotSizeAndDirection.getFirst(),
                            shotSizeAndDirection.getSecond()
                    )
            );
        }

        return meteorShotsToReturn;
    }

    /** Produces a list of Planet elements, based on the input JsonArray.
     *
     * @param planetArray JsonArray of arrays representing the list of cargos in that planet.
     *                        "y" -> YellowCargo,
     *                        "g" -> GreenCargo,
     *                        "r" -> RedCargo,
     *                        "b" -> BlueCargo
     * @return ArrayList of correspondent Planets
     */
    private ArrayList<Planet> createPlanetList(JsonArray planetArray){
        ArrayList<Planet> planetsToReturn = new ArrayList<>();

        for(JsonElement planet : planetArray){
            Planet newPlanet = new Planet(createCargoPrize(planet.getAsJsonArray()));
            planetsToReturn.add(newPlanet);
        }

        return planetsToReturn;
    }

    /** Produces a list of FireShot elements, based on the input JsonArray.
     *
     * @param fireShotsArray JsonArray of objects that contains strings of Size and direction of the fire shot.
     *                        The possible sizes are "SMALL" and "BIG"
     *                        The possible directions are "NORTH", "EAST", "SOUTH", "WEST"
     * @return ArrayList of correspondent FireShots
     */
    private ArrayList<FireShot> createFireShotList(JsonArray fireShotsArray){
        ArrayList<FireShot> fireShotsToReturn = new ArrayList<>();

        for(JsonElement fireShot : fireShotsArray){
            Pair<ShotSize, Direction> shotSizeAndDirection = getShotSizeAndDirection(fireShot);

            fireShotsToReturn.add(
                    new FireShot(
                            shotSizeAndDirection.getFirst(),
                            shotSizeAndDirection.getSecond()
                    )
            );
        }

        return fireShotsToReturn;
    }

    /** Extracts the size and direction from a Json of a FireShot
     *
     * @param fireShot of which extract the size and direction
     * @return ShotSize and Direction enum objects of the fireShot
     */
    private Pair<ShotSize, Direction> getShotSizeAndDirection(JsonElement fireShot){
        ShotSize size;
        Direction direction;

        if(fireShot.getAsJsonObject().get("size").getAsString().equals("SMALL"))
            size = ShotSize.SMALL;
        else
            size = ShotSize.BIG;

        String meteorDirection = fireShot.getAsJsonObject().get("direction").getAsString();
        direction = switch (meteorDirection) {
            case ("NORTH") -> Direction.NORTH;
            case ("EAST") -> Direction.EAST;
            case ("SOUTH") -> Direction.SOUTH;
            default -> Direction.WEST;
        };

        return new Pair<>(size, direction);
    }
}
