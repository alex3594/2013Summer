package me.xiangchen.app.duetstudy;

import java.util.Calendar;

import me.xiangchen.lib.xacPhoneGesture;
import me.xiangchen.technique.tiltsense.xacTiltSenseFeatureMaker;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

public class WristTilt extends TechniqueShell {

	public static final int TILTTIMEOUT = 1500;
	xacPhoneGesture pressAndHold;
	long timeFromHold;

	public WristTilt(Context context) {
		super(context);
		technique = WRISTTILT;
		
		numClasses = 2;
		classLabels = new int[] { xacTiltSenseFeatureMaker.NONE,
				xacTiltSenseFeatureMaker.TILTOUT };
		int numDataPointsPerClass = 50;
		numBlocks = 5;
		numTrialsPerBlock = numClasses * numDataPointsPerClass / numBlocks;

		labelCounter = new int[numClasses];
		radii = new float[numClasses];
		for (int i = 0; i < numClasses; i++) {
			labelCounter[i] = 0;
			radii[i] = 1;
		}

		pressAndHold = new xacPhoneGesture(xacPhoneGesture.PRESSANDHOLD);

		tvStatus.setText("Wrist tilting");
	}

	@Override
	public boolean doTouch(MotionEvent event) {

		// boolean retValue = false;
		isTouching = true;
		int action = event.getAction();

		if (!isBreak) {
//			if (isReadyForNextTrial) {

				if (isStarted) {
					long curTime = Calendar.getInstance().getTimeInMillis();

					switch (action) {
					case MotionEvent.ACTION_DOWN:
						pressAndHold.update(event);
						timeFromHold = -1;

						break;
					case MotionEvent.ACTION_MOVE:
						if (pressAndHold.getResult() != pressAndHold.YES) {
							pressAndHold.update(event);
							xacTiltSenseFeatureMaker.clearData();
							isReadyForNextTrial = false;
						} else {
							if (timeFromHold < 0) {
								label = nextClassLabel();
								setCues();
								timeFromHold = curTime;
							} else {
								if (curTime - timeFromHold > TILTTIMEOUT) {
									if (xacTiltSenseFeatureMaker.isDataReady()) {
										int tiltResult = xacTiltSenseFeatureMaker
												.calculateTilting();
										xacTiltSenseFeatureMaker.setLabels(label, tiltResult);
										xacTiltSenseFeatureMaker.sendOffData();
										xacTiltSenseFeatureMaker.clearData();
										isReadyForNextTrial = false;
										Log.d(LOGTAG, label + " : " + tiltResult);
										tvCue.setText("Release");
									}
								}
							}
						}
						break;
					case MotionEvent.ACTION_UP:
						break;
					}
				}
			

			if (action == MotionEvent.ACTION_UP) {
				if (isStarted) {
					
					trial++;
					
					if (trial == numTrialsPerBlock) {
						block++;
						isBreak = true;
						if (block == numBlocks) {
							tvCue.setText("End of technique");
						} else {
							tvCue.setText("End of block");
						}
					} else {
						tvCue.setText("Please wait ...");
					}					
				} else {
					isStarted = true;
					block = 0;
					trial = 0;
				}
				
				xacTiltSenseFeatureMaker.clearData();
				isReadyForNextTrial = false;
				isTouching = false;
			}
		}

		return true;

	}

	@Override
	public void runOnTimer() {
		if (!isBreak && !isTouching) {
			if (!xacTiltSenseFeatureMaker.isDataReady()) {
				tvCue.setText("Please wait ...");
				isReadyForNextTrial = false;
				// Log.d(LOGTAG, "wait...");
			} else {
				if (!isReadyForNextTrial) {
					if (isStarted) {
						label = nextClassLabel();
						tvCue.setText("Press and hold");
						setStatus();
					} else {
						tvCue.setText("Tap to start...");
					}

					isReadyForNextTrial = true;
					// Log.d(LOGTAG, "ready");
				}
			}
		}
	}

	@Override
	protected void setCues() {
		switch (label) {
		case xacTiltSenseFeatureMaker.TILTOUT:
			tvCue.setText("Tilt the wrist");
			break;
		case xacTiltSenseFeatureMaker.NONE:
			tvCue.setText("Press and hold");
			break;
		}
	}

}
