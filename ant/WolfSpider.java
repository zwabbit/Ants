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
    int counter = 10;
    
    boolean stalking = false;
    EatAnt eatAnt;
    
    public WolfSpider()
    {
        stalking = World.spiderRandom.nextBoolean();
        x = World.spiderRandom.nextInt(World.xdim);
        y = World.spiderRandom.nextInt(World.ydim);
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof AntMove)
        {
            if(stalking)
            {
                int newX = -1, newY = -1;
                while(newX < 0 || newX >= World.xdim)
                {
                    int xStep = World.spiderRandom.nextInt(3);
                    --xStep;
                    newX = x + xStep;
                }
                
                while(newY < 0 || newY >= World.ydim)
                {
                    int yStep = World.spiderRandom.nextInt(3);
                    --yStep;
                    newY = y + yStep;
                }
                
                Enter enter = new Enter();
                enter.startX = x;
                enter.startY = y;
                enter.endX = newX;
                enter.endY = newY;
                enter.ant = this.getSelf();
                enter.isAnt = false;
            }
            this.getSelf().tell(eatAnt);
        }
        if(o instanceof EatAnt)
        {
            currentPatch = World.patchMap.get(new Point(x,y));
            currentPatch.tell(new GetAnts(), this.getSelf());
        }
        if(o instanceof GetAnts)
        {
            GetAnts getAnts = (GetAnts)o;
            final TMap.View<Integer, ActorRef> ants = getAnts.ants;
            if(ants.size() > 0)
            {
                Tuple2<Integer, ActorRef> ant = ants.head();
                ant._2().tell("kill");
                counter = 10;
                this.getSelf().tell(new AntMove());
            }
            else
            {
                if(counter == 0)
                {
                    this.getSelf().tell(new AntMove());
                }
                else
                {
                    --counter;
                    currentPatch.tell(new GetAnts(), this.getSelf());
                }
            }
        }
        if (o instanceof Point) {
            Point loc = (Point) o;
            x = loc.x;
            y = loc.y;
            currentPatch = World.patchMap.get(new Point(x,y));
            //System.out.println("at " + o.toString());
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
