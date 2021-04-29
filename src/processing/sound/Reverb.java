package processing.sound;

import processing.core.PApplet;

/**
 * This is a simple reverb effect.
 * 
 * @webref effects
 * @webBrief This is a simple reverb effect.
 * @param parent
 *            PApplet: typically use "this"
 **/
public class Reverb extends Effect<JSynReverb> {

	public Reverb(PApplet parent) {
		super(parent);
	}

	@Override
	protected JSynReverb newInstance() {
		return new JSynReverb();
	}

	
//	public void process(SoundObject input, float room, float damp, float wet) {
	/**
	 * Changes the damping factor of the reverb effect.
	 * 
	 * @webref reverb
	 * @webBrief Changes the damping factor of the reverb effect.
	 * @param damp
	 *            A float value controlling the damping factor of the reverb
	 **/
	public void damp(float damp) {
		if (Engine.checkRange(damp, "damp")) {
			this.left.setDamp(damp);
			this.right.setDamp(damp);
		}
	}

	/**
	 * Change the room size of the reverb effect.
	 * 
	 * @webref reverb
	 * @webBrief Change the room size of the reverb effect.
	 * @param room
	 *            A float value controlling the room size of the effect.
	 **/
	public void room(float room) {
		if (Engine.checkRange(room, "room")) {
			this.left.setRoom(room);
			this.right.setRoom(room);
		}
	}

	/**
	 * Set multiple parameters of the reverb. Parameters have to be in the right order.
	 * 
	 * @webref reverb
	 * @webBrief Set multiple parameters of the reverb.
	 * @param room
	 *            A value controlling the room size of the effect
	 * @param damp
	 *            A value controlling the damping factor of the reverb
	 * @param wet
	 *            A value controlling the wet/dry ratio of the reverb.
	 **/
	public void set(float room, float damp, float wet) {
		this.room(room);
		this.damp(damp);
		this.wet(wet);
	}

	/**
	 * Change the wet/dry ratio of the reverb.
	 * 
	 * @webref reverb
	 * @webBrief Change the wet/dry ratio of the reverb.
	 * @param wet
	 *            A float value controlling the wet/dry ratio of the reverb. TODO
	 *            document
	 **/
	public void wet(float wet) {
		if (Engine.checkRange(wet, "wet")) {
			this.left.setWet(wet);
			this.right.setWet(wet);
		}
	}
}
