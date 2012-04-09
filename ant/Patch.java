/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ant;

import akka.actor.UntypedActor;
import java.util.HashMap;

/**
 *
 * @author Z98
 */
public class Patch extends UntypedActor {
    int x, y;
    int food = 0;
    float pher = 0;
    /*
     * Pretty sure I just hosed myself due to the boxing/
     * unboxing that will take place with the key value.
     */
    HashMap<Integer, Ant> ants;
    HashMap<Integer, Ant> antsCopy;
    
    public Patch(int x, int y)
    {
        this.x = x;
        this.y = y;
        ants = new HashMap<>();
        antsCopy = ants;
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof Eat)
        {
            Eat eat = (Eat)o;
            if(this.food >= eat.food)
            {
                this.food -= eat.food;
                eat.ate = true;
            }
            
            getSender().tell(eat);
            
            return;
        }
        if(o instanceof Enter)
        {
            Enter enter = (Enter)o;
            if(enter.ant == null)
            {
                ants.remove(enter.id);
            }
            else
            {
                ants.put(enter.id, enter.ant);
            }
            
            return;
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
