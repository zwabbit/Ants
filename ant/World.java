/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ant;

import akka.actor.*;
import akka.routing.BroadcastRouter;
import ant.point.PointQuadTree;
import akka.actor.Props;

import java.awt.Point;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Random;
import scala.actors.threadpool.Arrays;
import java.util.TreeSet;

//import scala.collection.immutable.TreeSet;
//import scala.math.Ordering.StringOrdering;

/**
 *
 * @author Z98
 */
public class World extends UntypedActor {
    int xdim;
    int ydim;
    
    public static PointQuadTree<ActorRef> foodPatches = null;
    public static HashMap<Point, ActorRef> patchMap = null;
    
    private ActorRef bRouter;
    //TreeSet<ActorRef> patches;
    TreeSet<String> patchRoutes;
    TreeSet<ActorRef> patches;
    
    public static Random foodRandom = null;
    
    public World(int xDim, int yDim)
    {
        if(foodRandom == null)
            foodRandom = new Random(System.currentTimeMillis());
        if(patchMap == null)
            patchMap = new HashMap<>();
        xdim = xDim;
        ydim = yDim;
        if(foodPatches == null)
            foodPatches = new PointQuadTree<>(new Point(0,0), new Dimension(xdim, ydim));
        patches = new TreeSet<>();
        //Ordering order = new Ordering();
        for(int x = 0; x < xDim; x++)
        {
            for(int y = 0; y < yDim; y++)
            {
                final int fx = x;
                final int fy = y;
                //Patch patch = new Patch(x,y);
                ActorRef patch = AntMain.system.actorOf(new Props(new UntypedActorFactory() {
                    public UntypedActor create()
                    {
                        return new Patch(fx, fy);
                    }
                }));
                if(patch == null)
                    System.err.println("Failed to create patch.");
                patchMap.put(new Point(x,y), patch);
                patches.add(patch);
                //patchRoutes.add(patch.self().path().address().toString());
            }
        }
        
        //bRouter = getContext().actorOf(new Props(Patch.class).withRouter(BroadcastRouter.apply(patches)));
        bRouter = AntMain.system.actorOf(new Props(Patch.class).withRouter(BroadcastRouter.create(patches)));
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof Tick)
        {
            foodPatches.clear();
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
