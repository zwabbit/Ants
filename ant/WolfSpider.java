/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ant;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import java.awt.Point;
import scala.Tuple2;
import scala.concurrent.stm.TMap;

/**
 *
 * @author Z98
 */
public class WolfSpider extends UntypedActor {
    
    ActorRef currentPatch;
    
    int x, y;

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof AntMove)
        {
            currentPatch = World.patchMap.get(new Point(x,y));
            currentPatch.tell(new GetAnts(), this.getSelf());
        }
        if(o instanceof GetAnts)
        {
            GetAnts getAnts = (GetAnts)o;
            final TMap.View<Integer, ActorRef> ants = getAnts.ants;
            Tuple2<Integer, ActorRef> ant = ants.head();
            ant._2().tell("kill");
            
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
