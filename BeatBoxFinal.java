package BeatBox;

import java.awt.*;
import javax.swing.*;
import java.io.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.event.*;

public class BeatBoxFinal {

	JFrame theFrame;
	JPanel mainPanel;
	JList<String> incomingList;
	JTextField userMessage;
	ArrayList<JCheckBox> checkboxList;
	int nextNum;
	Vector<String> listVector = new Vector<String>();
	String userName;
	ObjectOutputStream out;
	ObjectInputStream in;
	HashMap<String, boolean[]> otherSeqsMap = new HashMap<String, boolean[]>();

	Sequencer sequencer;
	Sequence sequence;
	Sequence mySequence = null;
	Track track;

	String[] instrumentNames = { "Bass Drum", "Closed Hit-Hat", "Open HIt-Hat", "Acoustic Snare", "Crash Cymbal",
			"Hand Clap", "High Tom", "Hi Bingo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap",
			"Low-mid Tom", "High Agogo", "Open Hi Congra" };

	int[] instruments = { 35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63 };

	public static void main(String[] args) {
		Scanner scn = new Scanner(System.in);
		System.out.print("Enter your Name: ");
		String str = scn.nextLine();
		new BeatBoxFinal().startUp(str);
	}

	public void startUp(String name) {

		this.userName = name;
		// open connection to the server
		try {
			Socket sock = new Socket("127.0.0.1", 4242);
			this.out = new ObjectOutputStream(sock.getOutputStream());
			this.in = new ObjectInputStream(sock.getInputStream());
			Thread remote = new Thread(new RemoteReader());
			remote.start();
			System.out.println("Connected to the Server.");
		} catch (Exception e) {
			System.out.println("Couldn't connect - you'll have to play alone.");
		}
		this.setUpMidi();
		this.buildGui();
	}

	public void buildGui() {
		this.theFrame = new JFrame("Cyber BeatBox with '" + this.userName + "'");
		this.theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		this.checkboxList = new ArrayList<JCheckBox>();

		Box buttonBox = new Box(BoxLayout.Y_AXIS);

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

		JButton sendIt = new JButton("Send It");
		sendIt.addActionListener(new MySendListener());
		buttonBox.add(sendIt);
		
		this.userMessage = new JTextField();
		buttonBox.add(this.userMessage);

		this.incomingList = new JList<String>();
		this.incomingList.addListSelectionListener(new MyListSelectionListener());
		this.incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane theList = new JScrollPane(this.incomingList);
		buttonBox.add(theList);
		this.incomingList.setListData(this.listVector);

		Box nameBox = new Box(BoxLayout.Y_AXIS);
		for (int i = 0; i < 16; i++) {
			nameBox.add(new Label(this.instrumentNames[i]));
		}

		background.add(BorderLayout.EAST, buttonBox);
		background.add(BorderLayout.WEST, nameBox);

		this.theFrame.getContentPane().add(background);

		GridLayout grid = new GridLayout(16, 16);
		grid.setVgap(1);
		grid.setHgap(2);
		this.mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER, mainPanel);

		for (int i = 0; i < 256; i++) {
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			this.checkboxList.add(c);
			this.mainPanel.add(c);
		}

		this.theFrame.setBounds(50, 50, 300, 300);
		this.theFrame.pack();
		this.theFrame.setVisible(true);
	}

	public void setUpMidi() {
		try {
			this.sequencer = MidiSystem.getSequencer();
			this.sequencer.open();
			this.sequence = new Sequence(Sequence.PPQ, 4);
			this.track = this.sequence.createTrack();
			this.sequencer.setTempoInBPM(120);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void buildTrackAndStart() {
		ArrayList<Integer> trackList = null;
		this.sequence.deleteTrack(this.track);
		this.track = this.sequence.createTrack();

		for (int i = 0; i < 16; i++) {
			trackList = new ArrayList<Integer>();

			for (int j = 0; j < 16; j++) {
				JCheckBox jc = this.checkboxList.get(j + (16 * i));
				if (jc.isSelected()) {
					int key = this.instruments[i];
					trackList.add(new Integer(key));
				} else {
					trackList.add(null);
				}
			}
			this.makeTracks(trackList);
		}
		this.track.add(this.makeEvent(192, 9, 1, 0, 15));

		try {
			this.sequencer.setSequence(this.sequence);
			this.sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			this.sequencer.start();
			this.sequencer.setTempoInBPM(120);
		} catch (Exception e) {
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

	public class MySendListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			boolean[] checkboxState = new boolean[256];

			for (int i = 0; i < checkboxList.size(); i++) {
				JCheckBox check = checkboxList.get(i);
				if (check.isSelected()) {
					checkboxState[i] = true;
				}
			}
			String messageToSend = null;
			try {
				out.writeObject(userName + nextNum++ + ": " + userMessage.getText());
				out.writeObject(checkboxState);
			} catch (Exception e2) {
				System.out.println("Sorry dude. Could not send it to the server.");
			}

			userMessage.setText("");
		}

	}

	public class MyListSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				String selected = incomingList.getSelectedValue();
				if (selected != null) {
					// now go the map and change the sequence
					boolean[] selectedState = otherSeqsMap.get(selected);
					changeSequence(selectedState);
					sequencer.stop();
					buildTrackAndStart();
				}
			}
		}

	}

	public class RemoteReader implements Runnable {

		boolean[] checkboxState = null;
		String nameToShow = null;
		Object obj = null;

		@Override
		public void run() {

			try {
				while ((obj = in.readObject()) != null) {
					System.out.println("got an object from server");
					System.out.println(obj.getClass());
					nameToShow = (String) obj;
					checkboxState = (boolean[]) in.readObject();
					otherSeqsMap.put(nameToShow, checkboxState);
					listVector.add(nameToShow);
					incomingList.setListData(listVector);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public class MyPlayMineListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (mySequence != null) {
				sequence = mySequence;
			}
		}
	}

	public void changeSequence(boolean[] checkboxState) {
		for (int i = 0; i < 256; i++) {
			JCheckBox check = this.checkboxList.get(i);
			if (checkboxState[i]) {
				check.setSelected(true);
			} else {
				check.setSelected(false);
			}
		}
	}

	public void makeTracks(ArrayList<Integer> list) {
		Iterator<Integer> it = list.iterator();
		for (int i = 0; i < list.size(); i++) {
			Integer num = it.next();
			if (num != null) {
				this.track.add(this.makeEvent(144, 9, num, 100, i));
				this.track.add(this.makeEvent(128, 9, num, 100, i + 1));
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
