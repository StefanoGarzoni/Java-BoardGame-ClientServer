package it.polimi.ingsw.model.ComponentTile;

import java.io.Serializable;

public class Connector implements Serializable {
   private final int connections;

   public Connector(int connection){
      this.connections = connection;
   }

   public Connector(Connector connector){
      this.connections = connector.getNumberOfConnections();
   }
   public int getNumberOfConnections() {
      return this.connections;
   }


   public boolean matchWith(Connector connector){
      if(
              (connector.getNumberOfConnections() == 0 && this.getNumberOfConnections()!=0)
                      || (connector.getNumberOfConnections()!= 0 && this.getNumberOfConnections()==0)
      )
         return false;  // not smooth connector with not smooth connector

      return this.getNumberOfConnections() == connector.getNumberOfConnections()
              || this.getNumberOfConnections() == 3
              || connector.getNumberOfConnections() == 3;
   }

   public Boolean equals(Connector connector){
      if(this.matchWith(connector)){
         return this.getNumberOfConnections() == connector.getNumberOfConnections();
      }
      return false;
   }

   public String toString(){
      return getNumberOfConnections() + " connections";
   }
}
