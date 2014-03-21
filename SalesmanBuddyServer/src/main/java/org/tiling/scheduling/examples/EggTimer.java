package org.tiling.scheduling.examples;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class EggTimer {
	private final Timer timer = new Timer();
    private final int minutes;

    public EggTimer(int minutes) {
        this.minutes = minutes;
    }

    public void start() {
        timer.schedule(new TimerTask() {
            public void run() {
                playSound();
                timer.cancel();
            }
            private void playSound() {
                System.out.println("Your egg is ready!");
                String fileName="F:\\Tools\\All-In-One-Eclipse\\eclipse\\startup.jar";
                try {
					Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL \""+fileName+"\"");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                // Start a new thread to play a sound...
            }
        }, minutes * 60 * 1000);
    }

    public static void main(String[] args) {
        EggTimer eggTimer = new EggTimer(1);
        eggTimer.start();
    }

}
