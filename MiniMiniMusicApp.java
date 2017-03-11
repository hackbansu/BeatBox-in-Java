package BeatBox;

import javax.sound.midi.*;

public class MiniMiniMusicApp {

	public static void main(String[] args) {
		MiniMiniMusicApp mini = new MiniMiniMusicApp();
		mini.play();
	}
	
	public void play() {
		try{
			Sequencer player = MidiSystem.getSequencer();
			player.open();
			
			Sequence seq = new Sequence(Sequence.PPQ, 4);
			
			Track track = seq.createTrack();
			
			ShortMessage first = new ShortMessage();
			first.setMessage(192,1,102,0);
			MidiEvent note = new MidiEvent(first, 1);
			track.add(note);
			
			ShortMessage a = new ShortMessage();
			a.setMessage(144,1,44,100);
			MidiEvent noteon = new MidiEvent(a, 1);
			track.add(noteon);
			
			ShortMessage b = new ShortMessage();
			b.setMessage(128,1,44,100);
			MidiEvent noteoff = new MidiEvent(b, 16);
			track.add(noteoff);
			
			player.setSequence(seq);
			
			player.start();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
