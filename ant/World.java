/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ant;

import akka.actor.*;
import akka.routing.BroadcastRouter;
import akka.transactor.Coordinated;
import akka.util.Timeout;
import ant.gui.GUIRequest;
import ant.gui.GUIUpdate;
import ant.gui.WaitForGUI;
import ant.point.PointQuadTree;
import akka.actor.Props;

import java.awt.Point;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import scala.actors.threadpool.Arrays;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

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
    public static HashMap<ActorRef, Point> antMap = null;
    public static boolean keepGoing = true;
    public static boolean waitForGUI = false;
    //public static HashMap<ActorRef, Enter> moveList = null;
    static ActorRef gui;
    
    private ActorRef bRouter;
    //TreeSet<ActorRef> patches;
    TreeSet<String> patchRoutes;
    TreeSet<ActorRef> patches;
    
    private static World worldInstance = null;
    
    public static Random foodRandom = null;
    public static Random antRandom = null;
    
    public World(int xDim, int yDim)
    {
        if(foodRandom == null)
            foodRandom = new Random(System.currentTimeMillis());
        if(patchMap == null)
            patchMap = new HashMap<>();
        if(antMap == null)
        	antMap = new HashMap<>();
        //if(moveList == null)
        	//moveList = new HashMap<>();
        if(antRandom == null)
            antRandom = new Random(System.currentTimeMillis());
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
        
        //***change initial ants here
        for(int i = 0;i<10;i++){
        	int antX = antRandom.nextInt(xDim);
        	int antY = antRandom.nextInt(yDim);
        	ActorRef newAnt = AntMain.system.actorOf(new Props(new UntypedActorFactory() {
        		public UntypedActor create()
        		{
        			return new Ant();
        		}
        	}));
        	patchMap.get(new Point(antX,antY)).tell(newAnt);
        	antMap.put(newAnt, new Point(antX,antY));
        	newAnt.tell(new Point(antX,antY));
        	newAnt.tell((Integer)antMap.size());
        	newAnt.tell(getSelf());
        	        	
        }
        
        //bRouter = getContext().actorOf(new Props(Patch.class).withRouter(BroadcastRouter.apply(patches)));
        bRouter = AntMain.system.actorOf(new Props(Patch.class).withRouter(BroadcastRouter.create(patches)));
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof Tick)
        {
            foodPatches.clear();
            return;
        }
        if(o instanceof WaitForGUI){
        	waitForGUI = ((WaitForGUI)o).isWait;
        	return;
        }
        if(o instanceof Pause){
    		keepGoing = false;
    		return;
    	}
    	if(o instanceof Play){
    		keepGoing = true;
    		return;
    	}
        if(o instanceof String){
        	if (o.equals("getDetails")){
        		getSender().tell(new GUIRequest(xdim, ydim, patches));
        		return;
        	}
        	/*if (o.equals("ants move")){
        		for(ActorRef p:patchMap.values()){
        			p.tell(new AntMove(), getSelf());
        		}
        		return;
        	}*/
        }
        if(o instanceof AntMove){
        	for(ActorRef p:patchMap.values()){
    			p.tell(o, getSelf());
    		}
        	return;
        }
        if(o instanceof ActorRef){
        	gui = (ActorRef)o;
        	return;
        }
        if(o instanceof GUIUpdate){
        	GetPatchInfo info = ((GUIUpdate)o).info;
        	//System.out.println("gui update");
        	gui.tell(info, getSender());
        	return;
        }
        if(o instanceof VisionRequest){
        	VisionRequest rq = (VisionRequest)o;
        	Point center = rq.center;
        	ArrayList<ActorRef> patches = new ArrayList<ActorRef>();
        	
        	int cx = center.x, cy = center.y;
        	patches.add(patchMap.get(new Point(cx-1, cy-1)));
        	patches.add(patchMap.get(new Point(cx, cy-1)));
        	patches.add(patchMap.get(new Point(cx+1, cy-1)));
        	patches.add(patchMap.get(new Point(cx-1, cy)));
        	patches.add(patchMap.get(new Point(cx, cy)));
        	patches.add(patchMap.get(new Point(cx+1, cy)));
        	patches.add(patchMap.get(new Point(cx-1, cy+1)));
        	patches.add(patchMap.get(new Point(cx, cy+1)));
        	patches.add(patchMap.get(new Point(cx+1, cy+1)));
        	
        	VisionRequest reply = new VisionRequest();
        	reply.center = center;
        	reply.patches = patches;
        	getSender().tell(reply);
        	return;
        	
        }
        if(o instanceof Enter){
          	/*System.out.println("ant " + antMap.get(getSender()).toString() + " moved to (" + ((Enter)o).endX + "," + ((Enter) o).endY + ")");
        	((Enter) o).startX = antMap.get(getSender()).x;
        	((Enter) o).startY = antMap.get(getSender()).y;
        	//moveList.put(getSender(), (Enter)o);
        	Enter ent = ((Enter) o);
  			patchMap.get(new Point(ent.endX, ent.endY)).tell(new Coordinated(ent, new Timeout(5, TimeUnit.SECONDS)));
			antMap.put(getSender(), new Point(ent.endX, ent.endY));
        	patchMap.get(new Point(ent.endX, ent.endY)).tell(new GetPatchInfo(), gui);
        	patchMap.get(new Point(ent.startX, ent.startY)).tell(new GetPatchInfo(), gui);
        	getSender().tell(new Point(ent.endX, ent.endY));
			getSender().tell(new AntMove(), getSelf());
			//System.out.println("move");
        	
        	/*if(moveList.size() == antMap.size()){
        		for(Enter en:moveList.values()){
        			Coordinated coord = new Coordinated(en, new Timeout(5, TimeUnit.SECONDS));
        			patchMap.get(new Point(en.endX, en.endY)).tell(coord);
        			antMap.put(, value)
        		}
        		moveList.clear();
        	}*/
        	return;
        }
        if(o instanceof Eat){
        	//System.out.println("ate");
        	Eat ea =new Eat();
        	ea.food=1;
        	patchMap.get(antMap.get(getSender())).tell(ea, getSender());
        	patchMap.get(antMap.get(getSender())).tell(new GetPatchInfo(), gui);
        	getSender().tell(new AntMove(), getSelf());
        	return;
        }
        if(o instanceof Scent){
        	Scent sce = (Scent)o;
        	//System.out.println("scent");
        	((Scent) o).smell = 1;
        	patchMap.get(antMap.get(getSender())).tell(sce, getSender());
        	patchMap.get(antMap.get(getSender())).tell(new GetPatchInfo(), gui);
        	getSender().tell(new AntMove(), getSelf());
        	return;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
