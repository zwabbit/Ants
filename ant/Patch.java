/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ant;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.transactor.Coordinated;
import ant.gui.GUIUpdate;

import java.awt.Point;
import java.util.HashMap;
import scala.collection.Iterator;

import scala.concurrent.stm.Ref;
import scala.concurrent.stm.TMap;
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
	boolean keepGoing = false;
	ActorRef world = null;
	/*
	 * Pretty sure I just hosed myself due to the boxing/
	 * unboxing that will take place with the key value.
	 */
	final TMap.View<Integer, ActorRef> ants;
	HashMap<Integer, ActorRef> antsCopy;

	public Patch(int x, int y)
	{
		this.x = x;
		this.y = y;
		ants = STM.newTMap();
		food.set(World.foodRandom.nextInt(MAX_FOOD));
		if(food.get() > 10)
                {
                    synchronized(World.foodPatches) {
                        World.foodPatches.insert(this.x, this.y, this.getSelf());
                    }
                }
	}

	@Override
	public void onReceive(Object o) throws Exception {
            world = AntMain.GetWorldRef();
		if(o instanceof Coordinated)
		{
			Coordinated coordinated = (Coordinated)o;

			Object message = coordinated.getMessage();
			if(message instanceof Enter)
			{
				Enter enter = (Enter)message;
				final int enterX = enter.endX;
				final int enterY = enter.endY;
				final int leaveX = enter.startX;
				final int leaveY = enter.startY;
				final int antID = enter.id;
				final boolean rly = enter.relayed;
				boolean succ = true;
				if (enterX != leaveX || enterY != leaveY){


					if (enter.relayed == false) {
						//System.out.println("ant " + enter.ant + " moved from (" + ((Enter)message).startX + "," + ((Enter)message).startY + ") to (" + ((Enter)message).endX + "," + ((Enter) message).endY + ") " + food.get());
						enter.relayed = true;
						ActorRef otherPatch = null;
						if (this.x == enterX && this.y == enterY) {
							otherPatch = World.patchMap.get(new Point(leaveX, leaveY));
						} else {
							otherPatch = World.patchMap.get(new Point(enterX, enterY));
						}
						otherPatch.tell(coordinated.coordinate(message));
					}
					final ActorRef ant = enter.ant;
					try{
						coordinated.atomic(new Runnable()
						{
							@Override
							public void run()
							{
								if(enterX == x && enterY == y)
								{
									ants.put(antID, ant);
									//System.out.println(antID + " enter " + x + " " + y);
									//System.out.println("ant " + ant + " added ");
								}
								if(leaveX == x && leaveY == y)
								{
									ants.remove(antID);
									//System.out.println(antID + " leave " + x + " " + y);
								}
								
							}
						});
					}
					catch(Exception e){
						succ = false;
						//throw e;
					}
					if( food == null || ants == null){
						//System.out.println(x + y + food.get() +  pher + ants.size());
					}
					if(succ){
						world.tell(new GUIUpdate(new GetPatchInfo(x, y, food.get(), pher, ants.size())));
						if(!rly){
							ant.tell(new Point(x,y));
							//System.out.println("ant " + ant + " told to move");
							if (keepGoing){
								ant.tell(new AntMove());
							}
						}
					}
					else{
						if (keepGoing){
							ant.tell(new AntMove());
						}
					}
				}
				else{
					
				}
				//System.out.println("(" + x +", " + y + ") " + ants.size());
				if(leaveX == x && leaveY == y)
				{
					//System.out.println("leave " + x + " " + y);
					//world.tell(new GUIUpdate(new GetPatchInfo(x, y, food.get(), pher, 0)));
				}
				else{
					//System.out.println("enter " + x + " " + y);
					///world.tell(new GUIUpdate(new GetPatchInfo(x, y, food.get(), pher, ants.size())));
				}
				
			}
			else
			{
				unhandled(coordinated);
			}
			return;
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

						return 0;
					}
						});
				eat.ate = true;
				getSender().tell(eat);
				//System.out.println("ant " + getSender() + " ate (" + x + ", " + y +")");
			}
			return;
		}
		if(o instanceof Drop){
			Integer amt = ((Drop)o).amt;
			final int amount = amt;
			Integer newFood = STM.getAndTransform(food, new Transformer<Integer>()
					{
				@Override
				public Integer apply(Integer a) {		
						a += amount;
					return a;
				}
					});
			//System.out.println("ant " + getSender() + " dropped (" + x + ", " + y +")");
			return;
			
		}
		if(o instanceof Scent)
		{
			Scent sent = (Scent)o;
			pher += sent.smell;
			//System.out.println("scent");
			return;
		}
		if(o instanceof GetAnts)
		{
			GetAnts gAnts = (GetAnts)o;
			gAnts.ants = ants.clone();
			getSender().tell(gAnts);
			return;
		}
		if(o instanceof AntMove){
			keepGoing = ((AntMove)o).go;
			world = getSender();
			if(ants.size()>0){
				Iterator<ActorRef> iter = ants.valuesIterator();
				while (iter.hasNext()){
					iter.next().tell(o);
				}
			}
			return;
		}
		if(o instanceof GetPatchInfo)
		{
			getSender().tell(new GetPatchInfo(x, y, food.get(), pher, ants.size()), getSelf());

			return;
		}
		if(o instanceof ActorRef){
			//if (o.getClass().equals(Ant.class)){
				ants.put(0, (ActorRef) o);
				//}
			return;
		}
		if(o instanceof Tick)
		{
			//antsCopy = new HashMap<>(ants);
			return;
		}
		throw new UnsupportedOperationException("Not supported yet." + o.toString());
	}
}
