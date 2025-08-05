package it.polimi.ingsw.model;

import java.io.Serializable;
import java.util.Random;

public class Die implements Serializable {
    Random rand;
    int facesNumber;

    public Die (int facesNumber){
        rand = new Random();
        this.facesNumber = facesNumber;
    }

    public int roll(){ return rand.nextInt(1, facesNumber+1); }

}
