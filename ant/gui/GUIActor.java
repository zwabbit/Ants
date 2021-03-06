package ant.gui;

import java.awt.Color;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.JPanel;

import scala.concurrent.stm.TMap;
import scala.concurrent.stm.Ref.View;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ant.AntMove;
import ant.GetPatchInfo;
import ant.Patch;
import ant.Pause;
import ant.Play;
import ant.SpiderGUIUpdate;
import ant.World;

public class GUIActor extends UntypedActor {
	
	static GUIBackground gui;
	static GUIControls controls;
	static TreeSet<ActorRef> patches;
	static ActorRef world;
	public static int simSpeed = 1;
	@Override
	public void onReceive(Object o) throws Exception {

		if (o instanceof ActorRef) {
			world = ((ActorRef) o);
			((ActorRef) o).tell("getDetails", getSelf());
			return;
		} 
		if (o instanceof WaitForGUI){
			world.tell(o);
			return;
		}
		if (o instanceof GUIRequest){
			makeGUI((GUIRequest)o);
			return;
		}
		if (o instanceof GUIUpdate){
			/*for (ActorRef p: ((GUIUpdate)o).patches){
				getPatchInfo(p);
			}*/
		}
		if (o instanceof Pause){
			world.tell(o);
			return;
		}
		if(o instanceof Play){
			world.tell(o);
			return;
		}
		if (o instanceof SpiderGUIUpdate){
			int py = ((SpiderGUIUpdate) o).loc.y;
			int px = ((SpiderGUIUpdate) o).loc.x;
			int i = py * gui.yD + px;
			GUIBackground.colorPatch((JPanel)gui.gameBoard.getComponent(i), Color.black);
			if(World.waitForGUI){
				Thread.sleep(simSpeed);
				World.spiderList.get(0).tell(new GUIWaitingMessage());
			}
			return;
		}
		if (o instanceof GetPatchInfo){
			if (gui == null){
				
			}
			else{
				if(((GetPatchInfo) o).x == -1){
					for(ActorRef p : patches){
						p.tell((GetPatchInfo) o, getSelf());
					}
				}
				else{
					int py = ((GetPatchInfo) o).y;
					int px = ((GetPatchInfo) o).x;
					Integer food = ((GetPatchInfo) o).food;
					Integer antses = ((GetPatchInfo) o).ants;
					int i = py * gui.yD + px; 
					GUIBackground.updatePatchTT((JPanel)gui.gameBoard.getComponent(i), "(" + px + ", " + py + ") " + food + " " + antses);
					if (antses > 0){
						GUIBackground.colorPatch((JPanel)gui.gameBoard.getComponent(i), Color.red);	
					}
					else if (food != 0){
						GUIBackground.colorPatch((JPanel)gui.gameBoard.getComponent(i), Color.green);
					}
					else {
						GUIBackground.colorPatch((JPanel)gui.gameBoard.getComponent(i), Color.LIGHT_GRAY);
					}
					
					if(World.waitForGUI && ((GetPatchInfo)o).assocAnt != null){
						Thread.sleep(simSpeed);
						//System.out.println(getSender() + " done updating, telling ant to continue " + ((GetPatchInfo)o).assocAnt);
						getSender().tell(new GUIWaitingMessage(true, ((GetPatchInfo)o).assocAnt));
					}
					return;
				}
			}
			return;
		}
		if (o instanceof AntMove){
			world.tell(o);
			return;
		}
		throw new UnsupportedOperationException("Not supported yet.");
	}

	
	private void makeGUI(GUIRequest gr) {
		patches = gr.patches;
		gui = new GUIBackground(gr.x, gr.y, gr.patches, getSelf());
		gui.pack();
		gui.setResizable(true);
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
		
		controls = new GUIControls(getSelf());
		controls.pack();
		controls.setResizable(true);
		controls.setLocationRelativeTo(null);
		controls.setVisible(true);
	}
	
	public void getPatchInfo(ActorRef pa){
		pa.tell(new GetPatchInfo(), getSelf());
	}
}
