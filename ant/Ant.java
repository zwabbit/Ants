/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ant;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.transactor.Coordinated;
import akka.util.Timeout;

/**
 *
 * @author Z98
 */
public class Ant extends UntypedActor {
	public int id;
	boolean alive = true;
	int energy = 0;
	int knownNeighbors = 0;
	Random moveRand = new Random();
	Point loc;
	Point home = new Point(50,50);
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
			getSelf().tell(new AntMove());
			return;
		}
		if(o instanceof ActorRef){
			world = (ActorRef)o;
			return;
		}
		if(o instanceof Point){
			loc = (Point)o;
			//System.out.println("at " + o.toString());
			return;
		}
		if(o instanceof Integer){
			id = (Integer)o;
			return;
		}
		if(o instanceof AntMove){
			int expNeighbors = 9;
			if((loc.x == 0 || loc.x == 99) && (loc.y == 0 || loc.y == 99)){
				expNeighbors = 4;
			}
			else if(loc.x == 0 || loc.x == 99 || loc.y == 0 || loc.y == 99){
				expNeighbors = 6;
			}
			if(knownNeighbors < expNeighbors){
				VisionRequest rq = new VisionRequest();
				rq.id = id;
				rq.center = loc;
				//System.out.println("rq");
				world.tell(rq, getSelf());
			}
			else{
				System.out.println("moving");
				if(energy > 0){
					World.patchMap.get(loc).tell(new Scent(), getSelf());
					//world.tell(new Scent(), getSelf());
					
					//getSelf().tell(new AntMove());
					int xDist = Math.abs(home.x - loc.x);
					int yDist = Math.abs(home.y - loc.y);
					if(xDist < 5 && yDist < 5){
						//drop food
						energy = 0;
						System.out.println("home!");
						getSelf().tell(new AntMove());
						knownNeighbors = 0;
						neighborhood.clear();
					}
					else{
						int goX = loc.x;
						int goY = loc.y;
						if(loc.x > home.x){
							goX --;
						}
						else if(loc.x < home.x){
							goX ++;
						}
						if(loc.y > home.y){
							goY --;
						}
						else if(loc.y < home.y){
							goY ++;
						}
						Enter ent = new Enter();
						ent.endX = goX;
						ent.endY = goY;
						ent.startX = loc.x;
						ent.startY = loc.y;
						ent.id = id;
						ent.ant = getSelf();
						knownNeighbors = 0;
						neighborhood.clear();
						World.patchMap.get(new Point(goX, goY)).tell(new Coordinated(ent, new Timeout(1, TimeUnit.SECONDS)), getSelf());
					}
					//go home
				}
				else{
					ActorRef ref = World.patchMap.get(loc);
					GetPatchInfo gp = neighborhood.get(ref);
					if (gp == null){
						//System.out.println("ant at " + loc.toString() + " couldn't find actor " + ref);
					}
					else{
						if(neighborhood.get(World.patchMap.get(loc)).food > 0){
							neighborhood.clear();
							knownNeighbors = 0;
							World.patchMap.get(loc).tell(new Eat(), getSelf());
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
							GetPatchInfo nfo;
							if (move != null){
								nfo = neighborhood.get(move);
								//System.out.println("foodmove");
							}
							else if (bestPherPatch != null){
								nfo = neighborhood.get(bestPherPatch);
								//System.out.println("phermove");
							}
							else {
								ArrayList<ActorRef> ar = new ArrayList<ActorRef>();
								for(ActorRef act:neighborhood.keySet()){
									ar.add(act);
								}
								
								
								nfo = neighborhood.get(ar.get(moveRand.nextInt(ar.size())));
								//System.out.println("randmove");
							}
							en.endX = nfo.x;
							en.endY = nfo.y;
							en.startX = loc.x;
							en.startY = loc.y;
							en.id = id;
							en.ant = getSelf();
							neighborhood.clear();
							knownNeighbors = 0;
							//world.tell(en, getSelf());
							if(en.endX == en.startX && en.endY == en.startY){
								//System.out.println("tried to move to self");
								getSelf().tell(new AntMove());
							}
							else{
								World.patchMap.get(new Point(en.endX, en.endY)).tell(new Coordinated(en, new Timeout(1, TimeUnit.SECONDS)), getSelf());
							}
						}
					}
					neighborhood.clear();
					knownNeighbors = 0;
				}
			}
			return;
		}
		if(o instanceof VisionRequest){
			VisionRequest rq = (VisionRequest)o;
			//loc = rq.center;
			for(ActorRef p : rq.patches){
				if(p != null){
					//System.out.println("asked " + p.toString());
					p.tell(new GetPatchInfo(), getSelf());
				}
			}
			return;
		}
		if(o instanceof GetPatchInfo){
			GetPatchInfo info = (GetPatchInfo)o;
			neighborhood.put(getSender(), info);
			
			knownNeighbors ++;
			int expNeighbors = 9;
			if((loc.x == 0 || loc.x == 99) && (loc.y == 0 || loc.y == 99)){
				expNeighbors = 4;
			}
			else if(loc.x == 0 || loc.x == 99 || loc.y == 0 || loc.y == 99){
				expNeighbors = 6;
			}
			System.out.println("upd neighbor " + knownNeighbors + " / " + expNeighbors);
			if (knownNeighbors >= expNeighbors){
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
