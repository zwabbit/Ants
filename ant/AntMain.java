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
import ant.gui.GUIActor;
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
    public static ActorSystem system = null;
    static ActorRef world = null;
    
    public static void main(String[] args) {
        int temp;
        System.out.println("Test");
        system = ActorSystem.create();
        world = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create()
            {
                return new World(100,100);
            }
        }), "world");
        ActorRef gui = system.actorOf(new Props(GUIActor.class), "gui");
        gui.tell(world);
        world.tell(gui);
       // world.tell("ants move");
       /* try {
            System.in.read();
            //ActorRef world = system.actorOf(new Props(new World(100,100)));
        } catch (IOException ex) {
            Logger.getLogger(AntMain.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
    
    public static ActorRef GetWorldRef()
    {
        return world;
    }
}
