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
import scala.collection.Iterator;
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
    ActorRef lastVictim = null;
    
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
                while(newX < 0 || newX >= World.xdim || newX == x)
                {
                    int xStep = World.spiderRandom.nextInt(3);
                    --xStep;
                    newX = x + xStep;
                    System.out.println("Stuck getting new X");
                }
                
                while(newY < 0 || newY >= World.ydim || newY == y)
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
                boolean ate = false;
                Iterator<Tuple2<Integer, ActorRef>> antsItr = ants.iterator();
                while(antsItr.hasNext())
                {
                    Tuple2<Integer, ActorRef> ant = antsItr.next();
                    if(ant._2() != lastVictim)
                    {
                        ant._2().tell("kill");
                        lastVictim = ant._2();
                        ate = true;
                        break;
                    }
                }
                
                if(ate)
                {
                    counter = 10;
                    stalking = false;
                    this.getSelf().tell(eatAnt);
                }
                else
                {
                    if (counter == 0) {
                        stalking = true;
                        this.getSelf().tell(new AntMove());
                    } else {
                        --counter;
                        getSelf().tell(new EatAnt());
                    }
                }
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
                    getSelf().tell(new EatAnt());
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
