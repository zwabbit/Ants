package ant.gui;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.*;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import ant.GetPatchInfo;
import ant.World;

public class GUIBackground extends JFrame implements MouseListener, MouseMotionListener {

	static JPanel gameBoard;
	static JLayeredPane layeredPane;
	static JLabel label;
	static HashMap<ActorRef, JPanel> patchmap = new HashMap<ActorRef, JPanel>();
	static int xD, yD;


	public GUIBackground(int xdim, int ydim, TreeSet<ActorRef> patches, ActorRef myactor){
		//init();
		xD = xdim;
		yD = ydim;
		Dimension boardSize = new Dimension(500,500);		
		label = new JLabel("bla");
		layeredPane = new JLayeredPane();
		getContentPane().add(layeredPane);
		layeredPane.setPreferredSize(boardSize);
		layeredPane.addMouseListener(this);
		layeredPane.addMouseMotionListener(this);
		gameBoard = new JPanel();
		layeredPane.add(gameBoard, JLayeredPane.DEFAULT_LAYER);
		gameBoard.setLayout(new GridLayout(xdim, ydim));
		gameBoard.setPreferredSize(boardSize);
		gameBoard.setBounds(0, 0, boardSize.width, boardSize.height);

		//guiWorld gWorld = new guiWorld();
		//add(gWorld);
		
		setTitle("Ant gui");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		for (int i = 0; i < xdim * ydim; i++){
			JPanel square = new JPanel(new BorderLayout());
			square.setToolTipText(patches.first().toString());
			square.setBackground(Color.white);
			square.addMouseListener(new MouseListener(){

				@Override
				public void mouseClicked(MouseEvent arg0) {
					GUIBackground.clicked(arg0.getSource());
					
				}

				@Override
				public void mouseEntered(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseExited(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mousePressed(MouseEvent arg0) {
					// TODO Auto-generated method stub
								
				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});
			gameBoard.add(square);
			
			/*int row = (i/8) % 2;
			if (row == 0){
				square.setBackground(i%2 == 0 ? Color.blue : Color.white);
			}
			else{ square.setBackground(i%2 == 0 ? Color.white : Color.blue);}
			*/
		}
		myactor.tell(new GetPatchInfo());
	}
	
	private static void clicked(Object source) {
		// TODO Auto-generated method stub
		((JPanel)source).setBackground(Color.getHSBColor((float) 0.5, (float)0.4, 2));
		return;
	}
	
	public static boolean updatePatchTT(JPanel p, String s){
		p.setToolTipText(s);
		return true;
	}
	
	public static boolean colorPatch(JPanel p, Color c){
		p.setBackground(c);
		return true;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseClicked(MouseEvent c) {
		// TODO Auto-generated method stub
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("Mouse clicked in frame");
	}


	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}