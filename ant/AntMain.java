/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ant;

import akka.actor.ActorRef;
import akka.actor.TypedProps;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.routing.RoundRobinRouter;
import akka.util.Duration;
import akka.agent.Agent;
import java.io.IOException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Z98
 */
public class AntMain {

    /**
     * @param args the command line arguments
     */
    static ActorSystem system = null;
    
    public static void main(String[] args) {
        int temp;
        System.out.println("Test");
        system = ActorSystem.create();
        ActorRef world = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create()
            {
                return new World(100,100);
            }
        }), "world");
        try {
            System.in.read();
            //ActorRef world = system.actorOf(new Props(new World(100,100)));
        } catch (IOException ex) {
            Logger.getLogger(AntMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
