import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class SoundManager {
	Clip clip;
	int volume;
	int position;

	public SoundManager(int volume) {
		this.volume = volume;
	}

	public void play(String fileName, boolean isLoop) {
		try {
			AudioInputStream audioInputStream = AudioSystem
					.getAudioInputStream(getClass().getClassLoader().getResource(fileName));
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			volumeControl.setValue(volume);
			if (isLoop)
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			clip.start();
		} catch (Exception e) {
		}
	}

	public void stop() {
		clip.stop();
		clip.close();
	}

	public void replay() {
		try {
			clip.setFramePosition(position);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void pause() {
		position = clip.getFramePosition();
		clip.stop();
	}
}
