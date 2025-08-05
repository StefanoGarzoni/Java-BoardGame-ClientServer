package it.polimi.ingsw;

import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;

public class ActionMessage {
    private final String actionName;
    private final Map<String, Object> params;
    private String sender;
    private String receiver;

    public ActionMessage(String actionName, String sender){
        this.actionName = actionName;
        params = new HashMap<>();
        this.sender = sender;
        receiver = "all";
    }

    public void setReceiver(String receiver){
        if(this.receiver.equals("all"))
            this.receiver = receiver;
    }
    public String getSender(){
        return sender;
    }
    public String getReceiver(){
        return receiver;
    }
    public void setData(String key, Object value){ params.put(key, value); }
    public Object getData(String key){ return params.get(key); }
    public String getActionName() { return actionName; }
    public Set<String> getKeysParams(){return new HashSet<>(params.keySet()); }
    public int getInt(String key){
        return (Integer)params.get(key);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(actionName);
        sb.append("(");
        sb.append(params.toString());
        sb.append(")");
        return sb.toString();
    }
}