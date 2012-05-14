/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ant;

import akka.actor.ActorRef;

/**
 *
 * @author Z98
 */
public class Enter {
    public int id;
    public ActorRef ant;
    public int startX, startY, endX, endY;
    public boolean relayed = false;
    public boolean isAnt = true;
}
