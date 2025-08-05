package it.polimi.ingsw.client.TUI;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class MessageList {
    ArrayList<String> messages;
    ArrayList<String> errorMessages;

    public MessageList(){
        messages = new ArrayList<>();
        errorMessages = new ArrayList<>();
    }

    public MessageList(String message){
        messages = new ArrayList<>();
        messages.add(message);
    }

    public void addMessage(String message){
        messages.addFirst(message);
    }

    public void addErrorMessage(String message){
        errorMessages.addFirst(message);
    }

    /**
     * Gets the last n elements of a list
     * @param messagesToQuery the amount of messages to retrieve, starting from the most recent
     * @return a {@code List} of n elements (or less if there are not enough messages)
     */
    public List<String> getMessages(int messagesToQuery) {

        List<String> list = new ArrayList<>();
        for(int index = 0; index < messagesToQuery && index < messages.size(); index++){
            list.add(messages.get(index));
        }
        return list;
    }

    /**
     * Gets the last n elements of a list
     * @param messagesToQuery the amount of error messages to retrieve, starting from the most recent
     * @return a {@code List} of n elements (or less if there are not enough messages)
     */
    public List<String> getErrorMessages(int messagesToQuery) {
        List<String> list = new ArrayList<>();
        for(int index = 0; index < messagesToQuery; index++){
            list.add(errorMessages.get(index));
        }
        return list;
    }
}