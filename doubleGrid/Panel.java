package TowerSlug.doubleGrid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

public class Panel extends JPanel implements Runnable {

	int width = 320;
	int height = 320;

	Image[] imageAr;

	Thread thread;
	Image image;
	Graphics g;

	// Vars for gLoop Below
	public int tps = 20;
	public int milps = 1000 / tps;
	long lastTick = 0;
	int sleepTime = 0;
	long lastSec = 0;
	int ticks = 0;
	long startTime;
	long runTime;
	private long nextTick = 0;
	private boolean running = false;

	// Vars for gLoop Above

	public Panel() {
		super();

		setPreferredSize(new Dimension(width, height));
		setFocusable(true);
		requestFocus();
	}

	public void addNotify() {
		super.addNotify();
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	ArrayList<int[]> map;

	public void run() {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
		this.setSize(new Dimension(width, height));

		try {
			TextInit.readMap();
			System.out.println("read Map");
		} catch (Exception ex) {
		}

		map = new ArrayList<int[]>();
		map = TextInit.getmap1();

		startTime = System.currentTimeMillis();

		gStart();
	}

	/**
	 * Methods go below here.
	 * 
	 */

	public void gStart() {
		imageInit();

		running = true;
		gLoop();
	}

	public void gLoop() {
		while (running) {
			System.out.println("Size: " + map.size());
			System.out.println("Leng: " + map.get(0).length);
			// Do the things you want the gLoop to do below here
			for (int h = 0; h < 40; h++) {
				for (int w = 0; w < 40; w++) {
					System.out.println("map: (" + map.get(h)[w] + ")");
					g.setColor(Color.GREEN);
					g.fillRect(w * 16, h * 16, 16, 16);
					g.setColor(Color.RED);
					g.drawLine(w * 16, h * 16, w * 16, h * 16 + 16);
					g.drawLine(w * 16, h * 16, w * 16 + 16, h * 16);
				}
			}

			// And above here.
			drwGm(g);

			ticks++;
			// Runs once a second and keeps track of ticks;
			// 1000 ms since last output
			if (timer() - lastSec > 1000) {
				if (ticks < tps - 1 || ticks > tps + 1) {
					if (timer() - startTime < 2000) {
						System.out.println("Ticks this second: " + ticks);
						System.out.println("timer(): " + timer());
						System.out.println("nextTick: " + nextTick);
					}
				}

				ticks = 0;
				lastSec = (System.currentTimeMillis() - startTime);
			}

			// Used to protect the game from falling beind.
			if (nextTick < timer()) {
				nextTick = timer() + milps;
			}

			// Limits the ticks per second
			if (timer() - nextTick < 0) {
				sleepTime = (int) (nextTick - timer());
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
				}

				nextTick += milps;
			}
		}
	}

	/**
	 * Methods go above here.
	 * 
	 */

	public long timer() {
		return System.currentTimeMillis() - startTime;

	}

	public void drwGm(Graphics g) {
		Graphics g2 = this.getGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
	}

	public void imageInit() {
		/**
		 * imageAr = new Image[1]; ImageIcon ie = new
		 * ImageIcon(this.getClass().getResource( "res/image.png")); imageAr[0]
		 * = ie.getImage();
		 */

	}
}
