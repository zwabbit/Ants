/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ant;

import java.awt.Point;
import java.util.HashMap;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 *
 * @author Z98
 */
public class Ant extends UntypedActor {
	public int id;
	boolean alive = true;
	int energy = 0;
	int knownNeighbors = 0;
	Point loc;
	ActorRef world;
	HashMap<ActorRef, GetPatchInfo> neighborhood = new HashMap<ActorRef, GetPatchInfo>();

	public static int FOOD_NEED = 10;

	@Override
	public void onReceive(Object o) throws Exception {
		if(o instanceof Eat)
		{
			Eat eat = (Eat)o;
			if(eat.ate == true)
				energy += FOOD_NEED;
			return;
		}
		if(o instanceof ActorRef){
			world = (ActorRef)o;
			return;
		}
		if(o instanceof Integer){
			id = (Integer)o;
			return;
		}
		if(o instanceof AntMove){
			if(knownNeighbors < 9){
				VisionRequest rq = new VisionRequest();
				rq.id = id;
				world.tell(rq, getSelf());
			}
			else{
				if(energy > 0){
					world.tell(new Scent(), getSelf());

					//go home
				}
				else if(neighborhood.get(World.patchMap.get(loc)).food > 0){
					world.tell(new Eat(), getSelf());
				}
				else{
					ActorRef move = null;
					int maxfood = 0;
					ActorRef bestPherPatch = null;
					float maxpher = 0;
					for(ActorRef r:neighborhood.keySet()){
						if(neighborhood.get(r).food > maxfood){
							maxfood = neighborhood.get(r).food;
							move = r;
						}
						if(neighborhood.get(r).pher > maxpher){
							maxpher = neighborhood.get(r).pher;
							bestPherPatch = r;
						}
					}
					Enter en = new Enter();
					if (move != null){
						GetPatchInfo nfo = neighborhood.get(move);

						en.endX = nfo.x;
						en.endY = nfo.y;
						en.id = id;


						//world.tell(m, getSelf());
					}
					else if (bestPherPatch != null){
						GetPatchInfo nfo = neighborhood.get(bestPherPatch);
						en.endX = nfo.x;
						en.endY = nfo.y;
						en.id = id;
						//world.tell(m, getSelf());
					}
					else {
						GetPatchInfo nfo = neighborhood.get((ActorRef)neighborhood.keySet().toArray()[0]);
						en.endX = nfo.x;
						en.endY = nfo.y;
						en.id = id;
						//world.tell(m, getSelf());
					}
					world.tell(en, getSelf());
				}
				neighborhood.clear();
			}
			return;
		}
		if(o instanceof VisionRequest){
			VisionRequest rq = (VisionRequest)o;
			loc = rq.center;
			for(ActorRef p : rq.patches){
				if(p != null){
					p.tell(new GetPatchInfo(), getSelf());
				}
			}
			return;
		}
		if(o instanceof GetPatchInfo){
			GetPatchInfo info = (GetPatchInfo)o;
			neighborhood.put(getSender(), info);
			knownNeighbors ++;
			if (knownNeighbors == 9){
				getSelf().tell(new AntMove());
			}
			return;
		}
		throw new UnsupportedOperationException(o.toString() + " Not supported yet.");
	}
}

//ant movement
/*if(foodCarrying != 0){
//go home
}
else {

/*
 * if (trailStrength > 0) {
 * 	face patch with highest pher amt
 * 	move
 * } 
 *	
 * else if (foodInVision) {
 * 	face food
 * 	move to food
 * }
 * 
 * else {
 * 	face a random direction, move forward
 * }
 * 
 * 
 *
}*/
//if has food, go home and lay trail
//if no food, move
//if trail, roll downhill
//if no trail, check for food in vision range
//if no food visible, random walk
