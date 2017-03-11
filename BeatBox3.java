package BeatBox;

import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;

public class BeatBox3 {
	
	JPanel mainPanel;
	ArrayList<JCheckBox> checkboxList;
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	JFrame theFrame;
	
	String[] instrumentNames = {"Bass Drum", "Closed Hit-Hat", "Open HIt-Hat", "Acoustic Snare", "Crash Cymbal",
			"Hand Clap", "High Tom", "Hi Bingo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap",
			"Low-mid Tom", "High Agogo", "Open Hi Congra"};
	
	int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
	
	public static void main(String[] args) {
		new BeatBox3().buildGUI();
	}
	
	public void buildGUI() {
		this.theFrame = new JFrame("Cyber BeatBox");
		this.theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		this.checkboxList = new ArrayList<JCheckBox>();
		
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		buttonBox.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
		
		JButton start = new JButton("Start");
		start.addActionListener(new MyStartListener());
		buttonBox.add(start);
		
		JButton stop = new JButton("stop");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);
		
		JButton upTempo = new JButton("Tempo Up");
		upTempo.addActionListener(new MyUpTempoListener());
		buttonBox.add(upTempo);
		
		JButton downTempo = new JButton("Tempo Down");
		downTempo.addActionListener(new MyDownTempoListener());
		buttonBox.add(downTempo);
		
		JButton serializeIt = new JButton("serializeIt");
		serializeIt.addActionListener(new MySendListener());
		buttonBox.add(serializeIt);
		
		JButton restore = new JButton("restore");
		restore.addActionListener(new MyReadInListener());
		buttonBox.add(restore);
		
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		for(int i=0; i<16; i++){
			JLabel label = new JLabel(this.instrumentNames[i]);
			label.setFont(new Font("TimesNewRoman", Font.BOLD, 18));
			nameBox.add(label);
		}
		
		background.add(BorderLayout.EAST, buttonBox);
		background.add(BorderLayout.WEST, nameBox);
		
		this.theFrame.getContentPane().add(background);
		
		GridLayout grid = new GridLayout(16, 16);
		grid.setVgap(1);
		grid.setHgap(2);
		this.mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER, mainPanel);
		
		for(int i=0; i<256; i++){
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			this.checkboxList.add(c);
			this.mainPanel.add(c);
		}
		
		this.setUpMidi();
		
		this.theFrame.setBounds(50, 50, 300, 300);
		this.theFrame.pack();
		this.theFrame.setVisible(true);
	}

	public void setUpMidi() {
		try{
			this.sequencer = MidiSystem.getSequencer();
			this.sequencer.open();
			this.sequence = new Sequence(Sequence.PPQ, 4);
			this.track = sequence.createTrack();
			this.sequencer.setTempoInBPM(120);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void buildTrackAndStart() {
		int[] trackList = null;
		
		this.sequence.deleteTrack(this.track);
		this.track = this.sequence.createTrack();
		
		for(int i=0; i<16; i++){
			trackList = new int[16];
			
			int key = this.instruments[i];
			
			for(int j=0; j<16; j++){
				JCheckBox jc = (JCheckBox) this.checkboxList.get(j + (16*i));
				if(jc.isSelected()){
					trackList[j] = key;
				}
				else{
					trackList[j] = 0;
				}
			}

			makeTracks(trackList);
			this.track.add(this.makeEvent(176, 1, 127, 0, 16));
		}
		
		this.track.add(this.makeEvent(192, 9, 1, 0, 15));
		try{
			this.sequencer.setSequence(this.sequence);
			this.sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			this.sequencer.start();
			this.sequencer.setTempoInBPM(120);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public class MyStartListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			buildTrackAndStart();
		}
	}
	
	public class MyStopListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			sequencer.stop();
		}
	}
	
	public class MyUpTempoListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor(tempoFactor * 1.03f);
		}
	}

	public class MyDownTempoListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor(tempoFactor * 0.97f);
		}
	}
	
	public class MySendListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			
			boolean[] checkboxState = new boolean[256];
			
			for(int i=0; i<checkboxList.size(); i++){
				JCheckBox check = (JCheckBox) checkboxList.get(i);
				if(check.isSelected()){
					checkboxState[i] = true;
				}
			}
			
			try {
				FileOutputStream fileStream = new FileOutputStream(new File("Checkbox.ser"));
				ObjectOutputStream os = new ObjectOutputStream(fileStream);
				os.writeObject(checkboxState);
				os.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public class MyReadInListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			
			boolean[] checkboxState = null;
			
			try {
				FileInputStream fileIn = new FileInputStream(new File("Checkbox.ser"));
				ObjectInputStream is = new ObjectInputStream(fileIn);
				checkboxState = (boolean[]) is.readObject();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			for(int i=0; i<checkboxState.length; i++){
				JCheckBox check = (JCheckBox) checkboxList.get(i);
				if(checkboxState[i]){
					check.setSelected(true);
				}
				else{
					check.setSelected(false);
				}
			}
			
			sequencer.stop();
			buildTrackAndStart();
		}
	}
	
	
	public void makeTracks(int[] list) {
		for(int i=0; i<16; i++){
			int key = list[i];
			
			if(key!=0){
				this.track.add(this.makeEvent(144, 9, key, 100, i));
				this.track.add(this.makeEvent(128, 9, key, 100, i+1));
			}
		}
	}
	
	public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
		MidiEvent event = null;
		try {
			ShortMessage a = new ShortMessage();
			a.setMessage(comd, chan, one, two);
			event = new MidiEvent(a, tick);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return event;
	}

}
