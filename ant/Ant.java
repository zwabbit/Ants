/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ant;

import akka.actor.UntypedActor;

/**
 *
 * @author Z98
 */
public class Ant extends UntypedActor {
    public int id;
    boolean alive = true;
    int energy = 0;

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof Eat)
        {
            Eat eat = (Eat)o;
            if(eat.ate == true)
                energy += 10;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
