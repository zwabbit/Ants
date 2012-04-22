/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ant;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.transactor.*;
import java.awt.Point;
import java.util.HashMap;
import scala.concurrent.stm.Ref;
import scala.concurrent.stm.japi.STM;
import scala.concurrent.stm.japi.STM.Transformer;

/**
 *
 * @author Z98
 */
/*
public class Patch
{
    int x, y;
    private Ref.View<Integer> food = STM.newRef(0);
    float pher = 0;
    HashMap<Integer, Ant> ants;
    
    public Patch(int x, int y)
    {
        food = new Ref<Integer>(10);
        this.y = y;
        ants = new HashMap<>();
    }
}
*/

public class Patch extends UntypedActor {
    static int MAX_FOOD = 50;
    final int x, y;
    private Ref.View<Integer> food = STM.newRef(0);
    float pher = 0;
    /*
     * Pretty sure I just hosed myself due to the boxing/
     * unboxing that will take place with the key value.
     */
    HashMap<Integer, ActorRef> ants;
    HashMap<Integer, ActorRef> antsCopy;
    
    public Patch(int x, int y)
    {
        this.x = x;
        this.y = y;
        ants = new HashMap<>();
        antsCopy = ants;
        food.set(World.foodRandom.nextInt(MAX_FOOD));
        if(food.get() > 10)
            World.foodPatches.insert(this.x, this.y, this.getSelf());
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof Coordinated)
        {
            Coordinated coordinated = (Coordinated)o;
            
            Object message = coordinated.getMessage();
            if(message instanceof Enter)
            {
                Enter enter = (Enter)message;
                final int enterX = enter.startX;
                final int enterY = enter.startY;
                final int leaveX = enter.endX;
                final int leaveY = enter.endY;
                final int antID = enter.id;
                if (enter.relayed == false) {
                    enter.relayed = true;
                    ActorRef otherPatch = null;
                    if (this.x == enterX && this.y == enterY) {
                        otherPatch = World.patchMap.get(new Point(leaveX, leaveY));
                    } else {
                        otherPatch = World.patchMap.get(new Point(enterX, enterY));
                    }
                    otherPatch.tell(coordinated.coordinate(message));
                }
                final ActorRef ant = getSender();
                coordinated.atomic(new Runnable()
                        {
                           @Override
                           public void run()
                           {
                               if(enterX == x && enterY == y)
                               {
                                   ants.put(antID, ant);
                               }
                               if(leaveX == x && leaveY == y)
                               {
                                   ants.remove(antID);
                               }
                           }
                        });
            }
            else
            {
                unhandled(coordinated);
            }
            /*
            if(message instanceof Eat)
            {
                Eat eat = (Eat)message;
                final int amount = -eat.food;
                coordinated.atomic(new Runnable()
                        {
                           @Override
                           public void run()
                           {
                               if(food.get() >= amount)
                                   STM.increment(food, amount);
                           }
                        });
            }
            */
        }
        if(o instanceof Eat)
        {
            Eat eat = (Eat)o;
            final int amount = eat.food;
            if(amount <= food.get())
            {
                Integer newFood = STM.getAndTransform(food, new Transformer<Integer>()
                {
                    @Override
                    public Integer apply(Integer a) {
                        if(a >= amount)
                        {
                            a -= amount;
                        }
                        
                        return a;
                    }
                });
                eat.ate = true;
                getSender().tell(eat);
            }
        }
        if(o instanceof Scent)
        {
            Scent sent = (Scent)o;
            pher += sent.smell;
            
            return;
        }
        if(o instanceof GetAnts)
        {
            GetAnts gAnts = (GetAnts)o;
            gAnts.ants = antsCopy;
            getSender().tell(gAnts);
            return;
        }
        if(o instanceof Tick)
        {
            antsCopy = new HashMap<>(ants);
            return;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
