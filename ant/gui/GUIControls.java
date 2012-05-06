package ant.gui;

import javax.swing.*;

import akka.actor.ActorRef;
import ant.*;

import java.awt.*;
import java.awt.event.*;

public class GUIControls extends JFrame implements ItemListener{

	JButton playButton;
	JButton pauseButton;
	JButton updateButton;
	JPanel panel;	
	JButton stepButton;
	JCheckBox waitForGui;
	ActorRef actor;
	//JButton resetButton;
	
	//JTextArea stepSpeed;
	//int stepSize = 400;
	boolean doStep = false;
	boolean play = false;
	boolean reset = true;
	
	public GUIControls(ActorRef w){
		actor = w;
		init();
	}
	
	public void setPlay(boolean p){
		play = p;
	}
	
	private final void init(){
		panel = new JPanel();
		Dimension cpSize = new Dimension(200,200);
		getContentPane().add(panel);
		panel.setPreferredSize(cpSize);
		panel.setLayout(null);		
		playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				actor.tell(new AntMove(true));
			}
		});
		
		playButton.setBounds(0,0,60,30);
		
		pauseButton = new JButton("Pause");
		pauseButton.setBounds(0,30,90,30);
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				if(World.keepGoing){
					actor.tell(new Pause());
				}
				else{
					actor.tell(new Play());
					actor.tell(new AntMove(false));
				}
			}
		});
		
		updateButton = new JButton("Update GUI");
		updateButton.setBounds(0,60,110,30);
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				actor.tell(new GetPatchInfo());
			}
		});
		
		stepButton = new JButton("Step");
		stepButton.setBounds(0, 120, 60, 30);
		stepButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				actor.tell(new AntMove(false));
			}
		});
		
		waitForGui = new JCheckBox("Wait for GUI?");
		waitForGui.setSelected(false);
		waitForGui.addItemListener(this);
		waitForGui.setBounds(60, 100, 120, 30);
		/*
		resetButton = new JButton("Reset");
		resetButton.setBounds(120, 40, 80, 30);
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				play = false;
				reset = true;
			}
		});
		*/
		
		panel.add(pauseButton);
		panel.add(playButton);
		panel.add(stepButton);
		panel.add(updateButton);
		panel.add(waitForGui);
		
		setTitle("Control Panel");
		setSize(300,200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public void pauseCheck(){
		//stepSpeed.setText(Integer.toString(stepSize));
		if (reset){
			//World.resetWorld();
			reset = false;
		}
		//while(true){
			//if (play == true || doStep == true){
				//doStep = false;
				//return;
			//}
			
		//}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
        Object source = e.getItemSelectable();
 
        if (source == waitForGui) {
        	if (e.getStateChange() == ItemEvent.DESELECTED) {
        		actor.tell(new WaitForGUI(false));
            }
        	else{
        		actor.tell(new WaitForGUI(true));
        	}
        }
	}
}
