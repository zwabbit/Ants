/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ant;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.transactor.Coordinated;
import akka.util.Timeout;

import java.awt.Point;
import java.util.concurrent.TimeUnit;

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
    ActorRef worldActor;
    
    boolean stalking = false;
    EatAnt eatAnt;
    
    public WolfSpider()
    {
        stalking = World.spiderRandom.nextBoolean();
        x = World.spiderRandom.nextInt(World.xdim);
        y = World.spiderRandom.nextInt(World.ydim);
        currentPatch = World.patchMap.get(new Point(x,y));
        eatAnt = new EatAnt();
    }

    @Override
    public void onReceive(Object o) throws Exception {
    	
        if(o instanceof AntMove)
        {
            if(stalking)
            {
            	System.out.println("stalk");
                int newX = -1, newY = -1;
                while(newX < 0 || newX >= World.xdim)
                {
                    int xStep = World.spiderRandom.nextInt(3);
                    --xStep;
                    newX = x + xStep;
                    System.out.println("Stuck getting new X");
                }
                
                while(newY < 0 || newY >= World.ydim)
                {
                    int yStep = World.spiderRandom.nextInt(3);
                    --yStep;
                    newY = y + yStep;
                    System.out.println("Stuck getting new Y");
                }
                
                currentPatch.tell(new GetPatchInfo(), worldActor);
                getSelf().tell(new Point(newX, newY));
                
                /*
                Enter enter = new Enter();
                enter.startX = x;
                enter.startY = y;
                enter.endX = newX;
                enter.endY = newY;
                enter.ant = this.getSelf();
                enter.isAnt = false;
                boolean succ = true;
                try {
                	currentPatch.tell(new Coordinated(enter, new Timeout(10000, TimeUnit.MICROSECONDS)), getSelf());	
                }
                catch(Exception e){
                	succ = false;
                	throw e;
                }
                if (succ)*/
                	
            }
            else
            {
                getSelf().tell(eatAnt);
            }
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
                System.out.println("kill!");
                counter = 10;
                this.getSelf().tell(new AntMove());
                stalking = false;
            }
            else
            {
            	System.out.println("noants");
                if(counter == 0)
                {
                	stalking = true;
                    this.getSelf().tell(new AntMove());
                }
                else
                {
                    --counter;
                    getSelf().tell(new EatAnt(), this.getSelf());
                }
            }
        }
        if (o instanceof Point) {
            Point loc = (Point) o;
            x = loc.x;
            y = loc.y;
            currentPatch = World.patchMap.get(new Point(x,y));
            //System.out.println("at " + o.toString());
            this.getSelf().tell(eatAnt);
            worldActor.tell(new SpiderGUIUpdate(new Point(x,y)));
        }
        if (o instanceof SpiderGUIUpdate){
        	worldActor = getSender();
        
        	worldActor.tell(new SpiderGUIUpdate(new Point(x,y)));
        	return;
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
