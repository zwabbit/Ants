/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ant;

import akka.actor.ActorRef;
import java.util.HashMap;
import scala.concurrent.stm.TMap;

/**
 *
 * @author Z98
 */
public class GetAnts {
    public TMap.View<Integer, ActorRef> ants;
}
