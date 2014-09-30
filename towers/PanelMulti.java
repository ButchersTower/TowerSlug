package towers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class PanelSpare extends JPanel implements Runnable, KeyListener,
		MouseListener {z

	// clicking adds to
	// need to make slows and stuff
	// need way of showing what the current target is?
	// draw tower names on button

	// speed var for each entity
	// detect nearby entities and deal damage to all of the them for splash
	// attacks.

	// Draw arrows going through the air to the entity. Draw a line going to the
	// entity and each tick have it redraw the line depending on where it is now
	// and where the entity is. Speed of travel is distance/4. then dist/3, and
	// so on. no because that will never end.

	// boolean to check when round is going
	// turn on game start, turn off last dies.
	boolean roundOn = false;

	int towerTargeted = -1;

	int width = 13 * 32 + 200;
	int height = 18 * 32;

	int w1 = 13 * 32;
	int h1 = 18 * 32;

	int w2 = 200;
	int h2 = 18 * 32;

	int endX = 6;
	int endY = 16;

	Image[] imageAr;

	Thread thread;
	Image image1;
	static Graphics g1;

	Image image2;
	static Graphics g2;

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

	// istead of having this information contained in tower just save the
	// location and the type of tower, do what is appropriate for that type of
	// tower when the person is in that towers range
	// 0 = x
	// 1 = y
	// 2 = range
	// 3 = damage
	// 4 = speed
	ArrayList<int[]> towers;

	// range, damage, attk speed
	int[][] towerList = { { 64, 10, 2 }, { 128, 4, 2 }, { 0, 0, 0 } };
	int tic = 0;
	int[] tickies = new int[0];

	// / Make an array to hold all the information for the button locs.
	// [x][0] = x
	// [x][1] = y
	// [x][2] = w
	// [x][3] = h
	// [x][4] = graphics ##
	// [x][5] = what it does
	int[][] butts = { { 50, 300, 50, 45, 1, 0 }, { 50, 100, 90, 20, 1, 1 },
			{ 50, 130, 90, 20, 1, 2 } };

	static ArrayList<int[]> map;

	// gets the height of the top JFrame bar and margin
	static int topInset = 0;

	static void setTopInset(int i) {
		topInset = i;
	}

	public Panel() {

		super();

		addKeyListener(this);
		addMouseListener(this);

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

	public void run() {
		this.setSize(new Dimension(width, height));
		image1 = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_RGB);
		g1 = (Graphics2D) image1.getGraphics();
		image2 = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_RGB);
		g2 = (Graphics2D) image2.getGraphics();
		startTime = System.currentTimeMillis();

		setAllThese();

		try {
			TextInit.readMap();
		} catch (Exception ex) {
		}

		map = TextInit.getmap1();

		gStart();
	}

	/**
	 * Methods go below here.
	 * 
	 */

	public void gStart() {
		imageInit();

		// towerInit();
		towers = new ArrayList<int[]>();

		running = true;
		gLoop();
	}

	double relativeX;
	double relativeY;

	void getMouseLoc() {
		// Used to figure out mouse location relative to jpanel.
		double mouseX = MouseInfo.getPointerInfo().getLocation().getX();
		double frameX = Towers.frame.getLocation().getX();
		double mouseY = MouseInfo.getPointerInfo().getLocation().getY()
				- topInset;
		double frameY = Towers.frame.getLocation().getY();
		relativeX = (mouseX - frameX);
		relativeY = (mouseY - frameY);
	}

	public void gLoop() {
		while (running) {
			// Do the things you want the gLoop to do below here
			getMouseLoc();

			checkEnd();

			drawMap();
			towerCheck();

			drawSide();
			highlightButts();

			waveTick();

			// if mouse is inside and a tower is selected draw a highlight
			// around
			// hovered square
			if (relativeX < w1 && relativeX > 0 && relativeY < h1
					&& relativeY > 0) {
				if (towerSelect != -1) {
					int rsX = (int) (relativeX - (relativeX % 32)) / 32;
					int rsY = (int) (relativeY - (relativeY % 32)) / 32;
					g1.setColor(Color.DARK_GRAY);
					g1.drawRect(rsX * 32, rsY * 32, 31, 31);
				}
			}

			// And above here.
			drwGm();

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

	void drawMap() {
		for (int h = 0; h < map.size(); h++) {
			for (int v = 0; v < map.get(h).length; v++) {
				g1.drawImage(imageAr[map.get(h)[v]], v * 32, h * 32, null);
			}
		}
	}

	// make an entity and have them walk from the top to the bottom.
	// entity will need to have hp and speed and defence and walking
	// capabilities.
	int ent1X = 6 * 32;
	int ent1Y;

	public static int getMapVar(int x, int y) {
		// System.out.println("lay: " + lan.get(0).length);
		// System.out.println("lan: " + lan.size());
		try {
			return map.get(y)[x];
		} catch (Exception ex) {
			return -1;
		}
	}

	public static void changeMap(int y, int x, int z) {
		map.get(y)[x] = z;
	}

	void towerCheck() {
		if (cmbt) {
			// checks missles size and adajusts it accordinly.
			if (towers.size() > missels.length) {
				int[][] buff = missels;
				missels = new int[towers.size()][];
				for (int i = 0; i < buff.length; i++) {
					missels[i] = buff[i];
				}
			}

			// if towers.size > tickies.length then keep all the old numbers of
			// tickies and add a new number to the end.
			if (towers.size() > tickies.length) {
				int[] buff = tickies;
				tickies = new int[towers.size()];
				for (int i = 0; i < buff.length; i++) {
					tickies[i] = buff[i];
				}
			}

			// move missels
			for (int i = 0; i < missels.length; i++) {
				if (missels[i] != null) {
					// if the mob dies this crashes
					if (mobAr[missels[i][2]] != null) {

						// move hypotnuse to target. distnace /mussels[3];
						// System.out.println("1: " + mobAr[missels[i][2]][0]);
						// System.out.println("2: " + missels[i][0] + 16);
						int deltX = mobAr[missels[i][2]][0] - missels[i][0]
								+ 16;
						int deltY = mobAr[missels[i][2]][1] - missels[i][1]
								+ 16;

						if (missels[i][0] == mobAr[missels[i][2]][0]
								&& missels[i][1] == mobAr[missels[i][2]][1]) {
							missels[i] = null;
						}

						missels[i][0] += deltX / missels[i][3];
						missels[i][1] += deltY / missels[i][3];

						// System.out.println("misX: " + missels[i][0]
						// + ", TarX: " + mobAr[missels[i][2]][0]);
						// System.out.println("misY: " + missels[i][1]
						// + ", TarY: " + mobAr[missels[i][2]][1]);

						// If the missel hits the target

						g1.drawOval(missels[i][0] - 3, missels[i][1] - 3, 6, 6);

						if (missels[i][0] == mobAr[missels[i][2]][0] + 16
								&& missels[i][1] == mobAr[missels[i][2]][1] + 16) {
							missels[i] = null;
							// System.out.println("HIT!");
						} else {
							missels[i][3] -= 1;
						}
						// move change in x/missels[i][3];
					}
				}
			}

			for (int i = 0; i < towers.size(); i++) {
				// if it can shoot then see if player is in range.
				// searches all the mobs for any in range.
				for (int dee = 0; dee < mobAr.length; dee++) {
					if (mobAr[dee] != null) {
						// center x
						int xee = (int) Math.abs(((towers.get(i)[0] * 32) + 16)
								- (mobAr[dee][0] + 16));
						// center y
						int yee = (int) Math.abs(((towers.get(i)[1] * 32) + 16)
								- (mobAr[dee][1] + 16));
						if (Math.hypot(xee, yee) < towers.get(i)[2]) {
							// g1.drawOval((towers.get(i)[0] * 32)
							// - towers.get(i)[2] + 16,
							// (towers.get(i)[1] * 32) - towers.get(i)[2]
							// + 16, 2 * towers.get(i)[2],
							// 2 * towers.get(i)[2]);

							// the mob is in range.
							// double check so it doesn't attack multiple mobs
							// in
							// the same tick
							if (tickies[i] == 4) {
								mobAr[dee][2] -= towers.get(i)[3];
								tickies[i] = 0;
								// g1.drawLine(towers.get(i)[0] * 32 + 16,
								// towers.get(i)[1] * 32 + 16,
								// mobAr[dee][0] + 16, mobAr[dee][1] + 16);
								missels[i] = new int[] {
										towers.get(i)[0] * 32 + 16,
										towers.get(i)[1] * 32 + 16, dee, 4 };
							}
						}
					}
				}
				if (tickies[i] < 4) {
					tickies[i]++;
				}

				/**
				 * // runs this every tick // checks to see which blocks have
				 * the player within its range for (int dee = 0; dee <
				 * mobAr.length; dee++) { int xee = (int)
				 * Math.abs(((towers.get(i)[0] * 32) + 16) - (xx + 16)); int yee
				 * = (int) Math.abs(((towers.get(i)[1] * 32) + 16) - (yy + 16));
				 * if (Math.hypot(xee, yee) < towers.get(i)[2]) {
				 * g1.drawOval((towers.get(i)[0] * 32) - 64 + 16,
				 * (towers.get(i)[1] * 32) - 64 + 16, 128, 128); if (tic == 4) {
				 * // System.out.println("attk"); health -= 5; tic = 0; } //
				 * System.out.println("tic: " + tic); tic++; } }
				 */
			}
		}
	}

	void towerInit() {
		towers.add(new int[] { 2, 3, 64, 10, 2 });
		towers.add(new int[] { 3, 3, 64, 10, 2 });
		towers.add(new int[] { 8, 7, 64, 10, 2 });
		towers.add(new int[] { 4, 11, 64, 10, 2 });
		for (int i = 0; i < towers.size(); i++) {
			changeMap(towers.get(i)[1], towers.get(i)[0], 3);
		}
	}

	ArrayList<String> textBox;

	int money = 100;

	// make buttons on the side that decide what tower your going to place
	// your money
	// your health

	void drawSide() {
		textBox = new ArrayList<String>();
		textBox.add("money " + money);
		txtBox(g2, 180, 60, 0, 10, 10, textBox);

		// Draws buttons
		for (int b = 0; b < butts.length; b++) {
			if (butts[b][4] == 0) {
				g1.setColor(Color.LIGHT_GRAY);
				g1.fillRect(butts[b][0], butts[b][1], butts[b][2], butts[b][3]);
			}
			if (butts[b][4] == 1) {
				g2.setColor(Color.LIGHT_GRAY);
				g2.fillRect(butts[b][0], butts[b][1], butts[b][2], butts[b][3]);
			}
		}

		// draws name on butts
		int[] b = converter("next");
		for (int c = 0; c < b.length; c++) {
			g2.drawImage(txtAr[b[c]], butts[0][0] + 2 + (c * 12), butts[0][1]
					+ (butts[0][3] / 2) - 9, null);
		}
		b = converter("tower 1");
		for (int c = 0; c < b.length; c++) {
			g2.drawImage(txtAr[b[c]], butts[1][0] + (c * 12), butts[1][1] + 2,
					null);
		}

	}

	int lastButt;

	// tests to see if the path is traversable once the new block is placed
	boolean testPath(int x, int y) {
		boolean nigel = true;
		PathFind tempPath = (new PathFind(x, y, 6, 1, this));
		if (tempPath.error) {
			System.out.println("Incomplete");
			nigel = false;
		}
		tempPath = null;
		return nigel;
	}

	void addTower(int x, int y, int tower) {
		// after it checks to see if a tower can go there it needs to add a
		// tower to the ArrayList
		int one = towerList[tower][0];
		int two = towerList[tower][1];
		int tre = towerList[tower][2];
		towers.add(new int[] { x, y, one, two, tre });
	}

	void highlightButts() {
		if (towerSelect == 1) {
			g2.setColor(Color.BLUE);
			g2.drawRect(butts[1][0], butts[1][1], butts[1][2] - 1,
					butts[1][3] - 1);
		}
		if (towerSelect == 2) {
			g2.setColor(Color.BLUE);
			g2.drawRect(butts[2][0], butts[2][1], butts[2][2] - 1,
					butts[2][3] - 1);
		}
	}

	// 0 = x
	// 1 = y
	// 2 == curHp
	// 3 == maxHp
	int[][] mobAr = { { 6 * 32, 1 * 32, 100, 100 },
			{ 6 * 32, 1 * 32, 100, 100 } };
	PathFind[] mobPath;

	boolean cmbt = false;
	// intitialize the path for each mob.
	// update them acordingly.

	// spawn mob method gets called every tick once the round starts
	// every X ticks spawn a new mob.
	// initialize the pathfind for that mob.

	// waveTick runs path for all initialized pathFinds.

	boolean one = false;

	void makeWave() {
		if (!one) {
			if (cmbt) {
				mobPath = new PathFind[mobAr.length];
				one = true;
				for (int i = 0; i < mobAr.length; i++) {
					mobPath[i] = new PathFind(
							endX,
							endY,
							((mobAr[0][0] + 16) - ((mobAr[0][0] + 16) % 32)) / 32,
							((mobAr[0][1] + 16) - ((mobAr[0][1] + 16) % 32)) / 32,
							this, i);
					System.out.println("RANS");
				}
			}
		}
		int x = 6;
		int y = 1;
		int maxHp;
		int health;
		// wavePath = new PathFind(endX, endY, x, y, this);
	}

	boolean needToSpawn = false;

	void waveTick() {
		// System.out.println("mobAr.l: " + mobAr.length);
		for (int i = 0; i < mobAr.length; i++) {
			try {
				g1.drawImage(imageAr[2], mobAr[i][0], mobAr[i][1], null);
				drawHealth(mobAr[i][0], mobAr[i][1], mobAr[i][2], mobAr[i][3]);
			} catch (Exception ex) {
			}
		}
		for (int i = 0; i < mobAr.length; i++) {
			if (mobAr[i] != null) {
				// if mob is dead
				if (mobAr[i][2] <= 0) {
					money += 10;
					try {
						mobPath[i] = null;
					} catch (Exception ex) {
					}
					mobAr[i] = null;
					checkEnd();
				}
			}
		}
		if (needToSpawn) {
			if (spawnTick == 4) {
				if (spawning < mobAr.length) {
					// System.out.println("mobAr " + spawning + ": "
					// + mobAr[spawning][0]);
					spawnWave(spawning);
					spawning++;
				} else {
					needToSpawn = false;
				}
				// spawn
				spawnTick = 0;
			} else {
				spawnTick++;
			}
		}
		if (cmbt) {
			for (int i = 0; i < mobPath.length; i++) {
				if (mobPath[i] != null) {
					mobPath[i].conCheck();
				}
			}
		}
	}

	void checkEnd() {
		if (roundOn) {
			boolean check = true;
			for (int i = 0; i < mobAr.length; i++) {
				if (mobAr[i] != null) {
					// System.out.println("fail: "+i);
					check = false;
				}
			}
			if (check) {
				System.out.println("ROUND DONE");
				roundOn = false;
			}
		}
	}

	int spawning = 0;

	int spawnTick = 0;

	// spawn with time in between
	void spawnWave(int a) {
		mobPath[a] = new PathFind(endX, endY,
				((mobAr[a][0] + 16) - ((mobAr[a][0] + 16) % 32)) / 32,
				((mobAr[a][1] + 16) - ((mobAr[a][1] + 16) % 32)) / 32, this, a);

	}

	/**
	 * bore
	 */

	public static void drawHealth(int x, int y, int curHp, int fulHp) {
		int de;
		if (curHp > 0) {
			if (curHp >= fulHp) {
				de = 24;
			} else if (curHp >= fulHp * 23 / 24) {
				de = 23;
			} else if (curHp >= fulHp * 22 / 24) {
				de = 22;
			} else if (curHp >= fulHp * 21 / 24) {
				de = 21;
			} else if (curHp >= fulHp * 20 / 24) {
				de = 20;
			} else if (curHp >= fulHp * 19 / 24) {
				de = 19;
			} else if (curHp >= fulHp * 18 / 24) {
				de = 18;
			} else if (curHp >= fulHp * 17 / 24) {
				de = 17;
			} else if (curHp >= fulHp * 16 / 24) {
				de = 16;
			} else if (curHp >= fulHp * 15 / 24) {
				de = 15;
			} else if (curHp >= fulHp * 14 / 24) {
				de = 14;
			} else if (curHp >= fulHp * 13 / 24) {
				de = 13;
			} else if (curHp >= fulHp * 12 / 24) {
				de = 12;
			} else if (curHp >= fulHp * 11 / 24) {
				de = 11;
			} else if (curHp >= fulHp * 10 / 24) {
				de = 10;
			} else if (curHp >= fulHp * 9 / 24) {
				de = 9;
			} else if (curHp >= fulHp * 8 / 24) {
				de = 8;
			} else if (curHp >= fulHp * 7 / 24) {
				de = 7;
			} else if (curHp >= fulHp * 6 / 24) {
				de = 6;
			} else if (curHp >= fulHp * 5 / 24) {
				de = 5;
			} else if (curHp >= fulHp * 4 / 24) {
				de = 4;
			} else if (curHp >= fulHp * 3 / 24) {
				de = 3;
			} else if (curHp >= fulHp * 2 / 24) {
				de = 2;
			} else {
				de = 1;
			}
			g1.drawImage(healthI[de], (int) x, (int) y + 32, null);
			// de =playerHp], xx+200, yy+200 + 32, null);
		} else {
			de = 0;
		}
	}

	public void txtBox(Graphics g, int wi, int hi, int font, int xl, int yl,
			ArrayList<String> st) {
		// Draws the outline of the text box
		g.setColor(Color.CYAN);
		g.fillRect(xl, yl, wi, hi);
		// line the last String was drawn on in the text box, top down.
		int lineDrawnOn = 0;
		// string array of all the words individually
		for (int stl = 0; stl < st.size(); stl++) {
			String[] words = st.get(stl).split("[ ]");

			int twi = 0, thi = 0;
			if (font == 0) {
				twi = 12;
				thi = 16;
			}
			if (font == 1) {
				twi = 6;
				thi = 8;
			}

			// How many letters can go in a row.
			int lettersPerRow = (wi - (wi % twi)) / twi;

			// first figure out how many lines there will be.
			int numLines = 0;
			// ghe is only temporary and is used to keep track of number of
			// words
			int ghe = 0;
			while (ghe < words.length) {
				ghe = repeat(ghe, 0, words, lettersPerRow);
				numLines++;
			}
			int[] figures = new int[numLines];
			for (int i = 0; i < figures.length; i++) {
				if (i == 0) {
					figures[0] = repeat(0, 0, words, lettersPerRow);
				} else {
					figures[i] = repeat(figures[i - 1], 0, words, lettersPerRow);
				}
			}
			for (int i = 0; i < figures.length; i++) {
				for (int ii = 0; ii < i; ii++) {
					figures[i] -= figures[ii];
				}
			}

			int drawPlace = xl;
			int drawnWords = 0;
			// this makes it draw all the lines
			for (int ig = 0; ig < figures.length; ig++) {
				// this draws one line
				for (int ih = 0; ih < figures[ig]; ih++) {
					// draws each word
					// System.out.println("figures[ig]: " + figures[ig]);
					int[] d = converter(words[drawnWords]);
					// System.out.println("words[drawnWords]: "+words[drawnWords]);
					for (int i = 0; i < d.length; i++) {
						// draws each letter
						g.drawImage(txtAr[d[i]], drawPlace, yl + (ig * thi)
								+ (lineDrawnOn * thi), null);
						drawPlace += twi;
					}
					if (drawPlace - xl + twi < wi) {
						// draws spaces if there is room
						g.drawImage(txtAr[26], drawPlace, yl + (ig * thi)
								+ (lineDrawnOn * thi), null);
						drawPlace += twi;
					}
					drawnWords++;

				}
				drawPlace = xl;
			}
			lineDrawnOn += figures.length;
		}
	}

	public static int[] converter(String st) {
		int a = st.length();
		int[] nw = new int[a];
		for (int b = 0; b < a; b++) {
			if (st.charAt(b) == 'a') {
				nw[b] = 0;
			} else if (st.charAt(b) == 'b') {
				nw[b] = 1;
			} else if (st.charAt(b) == 'c') {
				nw[b] = 2;
			} else if (st.charAt(b) == 'd') {
				nw[b] = 3;
			} else if (st.charAt(b) == 'e') {
				nw[b] = 4;
			} else if (st.charAt(b) == 'f') {
				nw[b] = 5;
			} else if (st.charAt(b) == 'g') {
				nw[b] = 6;
			} else if (st.charAt(b) == 'h') {
				nw[b] = 7;
			} else if (st.charAt(b) == 'i') {
				nw[b] = 8;
			} else if (st.charAt(b) == 'j') {
				nw[b] = 9;
			} else if (st.charAt(b) == 'k') {
				nw[b] = 10;
			} else if (st.charAt(b) == 'l') {
				nw[b] = 11;
			} else if (st.charAt(b) == 'm') {
				nw[b] = 12;
			} else if (st.charAt(b) == 'n') {
				nw[b] = 13;
			} else if (st.charAt(b) == 'o') {
				nw[b] = 14;
			} else if (st.charAt(b) == 'p') {
				nw[b] = 15;
			} else if (st.charAt(b) == 'q') {
				nw[b] = 16;
			} else if (st.charAt(b) == 'r') {
				nw[b] = 17;
			} else if (st.charAt(b) == 's') {
				nw[b] = 18;
			} else if (st.charAt(b) == 't') {
				nw[b] = 19;
			} else if (st.charAt(b) == 'u') {
				nw[b] = 20;
			} else if (st.charAt(b) == 'v') {
				nw[b] = 21;
			} else if (st.charAt(b) == 'w') {
				nw[b] = 22;
			} else if (st.charAt(b) == 'x') {
				nw[b] = 23;
			} else if (st.charAt(b) == 'y') {
				nw[b] = 24;
			} else if (st.charAt(b) == 'z') {
				nw[b] = 25;
			} else if (st.charAt(b) == ' ') {
				nw[b] = 26;
			} else if (st.charAt(b) == '0') {
				nw[b] = 27;
			} else if (st.charAt(b) == '1') {
				nw[b] = 28;
			} else if (st.charAt(b) == '2') {
				nw[b] = 29;
			} else if (st.charAt(b) == '3') {
				nw[b] = 30;
			} else if (st.charAt(b) == '4') {
				nw[b] = 31;
			} else if (st.charAt(b) == '5') {
				nw[b] = 32;
			} else if (st.charAt(b) == '6') {
				nw[b] = 33;
			} else if (st.charAt(b) == '7') {
				nw[b] = 34;
			} else if (st.charAt(b) == '8') {
				nw[b] = 35;
			} else if (st.charAt(b) == '9') {
				nw[b] = 36;
			} else if (st.charAt(b) == '.') {
				nw[b] = 37;
			}
		}
		return nw;
	}

	// returns number of words that can fit in that row.
	int repeat(int a, int b, String[] words, int lettersPerRow) {
		// cant be words[a], because this doesnt take into account the other
		// words before it.
		if (a >= words.length) {
			// System.out.println("end of the array");
			return a;
		}
		// if the next word can fit
		if (b + words[a].length() <= lettersPerRow) {
			// if there is room after the next word for a space.
			if (b + words[a].length() + 1 <= lettersPerRow) {
				b += words[a].length() + 1;
			} else {
				b += words[a].length();
			}
			a++;
			return repeat(a, b, words, lettersPerRow);
		} else {
			// it is to long to fit
			// a == number of words that can fit in that row
			return a;
			// after it returns a it should check if a is >= words.length
			// if not then add a to the first of int[]
			// then run repeat again for the next line.
		}

	}

	void removeThis(Object obj) {
		if (mobPath != null) {
			for (int i = 0; i < mobPath.length; i++) {
				if (obj == mobPath[i]) {
					mobAr[i] = null;
					mobPath[i] = null;
				}
			}
		}
	}

	/**
	 * Methods go above here.
	 * 
	 */

	public void buttons(MouseEvent migg) {
		boolean buttPressed = false;
		for (int a = 0; a < butts.length; a++) {
			if (butts[a][4] == 0) {
				if (migg.getY() > butts[a][1]) {
					if (migg.getY() < butts[a][1] + butts[a][3]) {
						if (migg.getX() > butts[a][0]) {
							if (migg.getX() < butts[a][0] + butts[a][2]) {
								buttonP(butts[a][5], 0);
								buttPressed = true;
							}
						}
					}
				}
			}
			if (butts[a][4] == 1) {
				if (migg.getY() > butts[a][1]) {
					if (migg.getY() < butts[a][1] + butts[a][3]) {
						if (migg.getX() - w1 > butts[a][0]) {
							if (migg.getX() - w1 < butts[a][0] + butts[a][2]) {
								buttonP(butts[a][5], 0);
								buttPressed = true;
							}
						}
					}
				}
			}
		}
		if (!buttPressed) {
			lastButt = -1;
		}
	}

	// [x][y] = towers.length; y = {0 = x, 1 = y, 2 = tx, 3 = ty , 4 = ticksLeft
	int[][] missels;

	public void buttonP(int a, int b) {
		System.out.println("MouseClicked: (" + a + ", " + b + ")");
		if (a == 0) {
			if (!roundOn) {
				roundOn = true;
				cmbt = true;
				// next button
				spawning = 0;
				mobPath = new PathFind[allThese[round].length];
				mobAr = new int[allThese[round].length][];
				mobAr = allThese[round];
				missels = new int[towers.size()][];
				needToSpawn = true;
				round++;
			}
		} else if (a == 1) {
			towerSelect = 1;
		} else if (a == 2) {
			towerSelect = 2;
		}
		// money -= 10;
	}

	int[][][] allThese = new int[10][][];

	void setAllThese() {
		allThese[0] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 } };
		allThese[1] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 } };
		allThese[2] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 } };
		allThese[3] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 } };
		allThese[4] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 } };
		allThese[5] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 } };
		allThese[6] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 } };
		allThese[7] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 } };
		allThese[8] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 } };
		allThese[9] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 } };
	}

	public long timer() {
		return System.currentTimeMillis() - startTime;

	}

	public void drwGm() {
		Graphics g2 = this.getGraphics();
		g2.drawImage(image1, 0, 0, null);
		g2.dispose();
		g2 = this.getGraphics();
		g2.drawImage(image2, w1, 0, null);
		g2.dispose();
	}

	static Image[] healthI;
	Image[] txtAr;

	int towerSelect = -1;

	int round = 0;
	boolean shiftP = false;

	public void imageInit() {
		imageAr = new Image[4];
		ImageIcon ii = new ImageIcon(this.getClass().getResource(
				"res/Grass1/GrassL.png"));
		imageAr[0] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Wall2/Wall2.png"));
		imageAr[1] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/evilSmall.png"));
		imageAr[2] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Wall2/WallTop.png"));
		imageAr[3] = ii.getImage();

		healthI = new Image[25];
		ii = new ImageIcon(this.getClass().getResource("res/Health/h0.png"));
		healthI[0] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h1.png"));
		healthI[1] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h2.png"));
		healthI[2] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h3.png"));
		healthI[3] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h4.png"));
		healthI[4] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h5.png"));
		healthI[5] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h6.png"));
		healthI[6] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h7.png"));
		healthI[7] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h8.png"));
		healthI[8] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h9.png"));
		healthI[9] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h10.png"));
		healthI[10] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h11.png"));
		healthI[11] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h12.png"));
		healthI[12] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h13.png"));
		healthI[13] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h14.png"));
		healthI[14] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h15.png"));
		healthI[15] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h16.png"));
		healthI[16] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h17.png"));
		healthI[17] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h18.png"));
		healthI[18] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h19.png"));
		healthI[19] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h20.png"));
		healthI[20] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h21.png"));
		healthI[21] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h22.png"));
		healthI[22] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h23.png"));
		healthI[23] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Health/h24.png"));
		healthI[24] = ii.getImage();

		txtAr = new Image[43];
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cA.png"));
		txtAr[0] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cB.png"));
		txtAr[1] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cC.png"));
		txtAr[2] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cD.png"));
		txtAr[3] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cE.png"));
		txtAr[4] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cF.png"));
		txtAr[5] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cG.png"));
		txtAr[6] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cH.png"));
		txtAr[7] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cI.png"));
		txtAr[8] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cJ.png"));
		txtAr[9] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cK.png"));
		txtAr[10] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cL.png"));
		txtAr[11] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cM.png"));
		txtAr[12] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cN.png"));
		txtAr[13] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cO.png"));
		txtAr[14] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cP.png"));
		txtAr[15] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cQ.png"));
		txtAr[16] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cR.png"));
		txtAr[17] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cS.png"));
		txtAr[18] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cT.png"));
		txtAr[19] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cU.png"));
		txtAr[20] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cV.png"));
		txtAr[21] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cW.png"));
		txtAr[22] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cX.png"));
		txtAr[23] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cY.png"));
		txtAr[24] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/cZ.png"));
		txtAr[25] = ii.getImage();
		ii = new ImageIcon(this.getClass()
				.getResource("res/font/tx/cSpace.png"));
		txtAr[26] = ii.getImage();

		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n0.png"));
		txtAr[27] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n1.png"));
		txtAr[28] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n2.png"));
		txtAr[29] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n3.png"));
		txtAr[30] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n4.png"));
		txtAr[31] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n5.png"));
		txtAr[32] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n6.png"));
		txtAr[33] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n7.png"));
		txtAr[34] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n8.png"));
		txtAr[35] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/tx/n9.png"));
		txtAr[36] = ii.getImage();

		ii = new ImageIcon(this.getClass().getResource(
				"res/font/Text/slash.png"));
		txtAr[37] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource(
				"res/font/Text/qMark.png"));
		txtAr[38] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource(
				"res/font/Text/qMarkI.png"));
		txtAr[39] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/Text/(.png"));
		txtAr[40] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/font/Text/).png"));
		txtAr[41] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource(
				"res/font/Text/underscore.png"));
		txtAr[42] = ii.getImage();
	}

	@Override
	public void mouseClicked(MouseEvent me) {

	}

	@Override
	public void mouseEntered(MouseEvent me) {
		System.out.println("enter: (" + me.getX() + ", " + me.getY() + ")");
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent me) {
		// find which block im cliking on.
		buttons(me);
		if (me.getX() < 13 * 32) {
			int x = (me.getX() - (me.getX() % 32)) / 32;
			int y = (me.getY() - (me.getY() % 32)) / 32;
			if (!roundOn) {
				if (towerSelect == 1) {
					if (getMapVar(x, y) == 0) {
						if (money >= 10) {
							// changes map
							changeMap(y, x, 3);
							// sees if there is path
							if (testPath(endX, endY)) {
								// if there is it leaves it alone.
								addTower(x, y, 0);
								money -= 10;
							} else {
								// if there is not then change map back to
								// blank.
								// should make a buffer int of what that block
								// was
								changeMap(y, x, 0);
							}
						} else {
							towerSelect = -1;
						}
						if (!shiftP) {
							towerSelect = -1;
						}
					}
				}
				if (towerSelect == 2) {
					if (getMapVar(x, y) == 0) {
						if (money >= 14) {
							// changes map
							changeMap(y, x, 2);
							// sees if there is path
							if (testPath(endX, endY)) {
								// if there is it leaves it alone.
								addTower(x, y, 1);
								money -= 14;
							} else {
								// if there is not then change map back to
								// blank.
								// should make a buffer int of what that block
								// was
								changeMap(y, x, 0);
							}
						} else {
							towerSelect = -1;
						}
						if (!shiftP) {
							towerSelect = -1;
						}
					}
				}
			} else {
				towerSelect = -1;
			}
			// run through all the towers and see if there is one with the x and
			// y of your click
			boolean towerFound = false;
			for (int i = 0; i < towers.size(); i++) {
				if (towers.get(i)[0] == x && towers.get(i)[1] == y) {
					towerTargeted = i;
					towerFound = true;
				}
			}
			if (!towerFound) {
				towerTargeted = -1;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
			shiftP = true;
		}

	}

	@Override
	public void keyReleased(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
			shiftP = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}
}
