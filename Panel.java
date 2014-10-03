package TowerSlug;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Panel extends JPanel implements Runnable, KeyListener,
		MouseListener {

	// clicking adds to
	// need to make slows and stuff
	// need way of showing what the current target is?
	// draw tower names on button

	// speed var for each entity
	// detect nearby entities and deal damage to all of the them for splash
	// attacks.

	// boolean to check when round is going
	// turn on game start, turn off last dies.

	/**
	 * Just Pasted the image into imageInit going to set selecetedTower to have
	 * the outline. then add options to upgrade tower.
	 */
	boolean roundOn = false;

	int towerTargeted = -1;

	int width = 13 * 32 + 200;
	int height = 18 * 32;

	int w1 = 13 * 32;
	int h1 = 18 * 32;

	int w2 = 200;
	int h2 = 18 * 32;

	int money = 80;
	int lives = 20;

	int endX = 6;
	int endY = 16;

	static Image[] healthI;
	Image[] txtAr;
	Image[] imageAr;

	Thread thread;
	Image image1;
	static Graphics g1;

	Image image2;
	static Graphics g2;

	// Tower you click on in map to upgrade and such.
	int selectedTower = -1;

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
	// 5 = typeOfTower
	// 6 = upgrade
	ArrayList<int[]> towers;

	// range, damage, attkSpeed, price
	int[][] towerList = { { 64, 10, 5, 10 }, { 128, 8, 7, 14 },
			{ 64, 4, 5, 14 } };
	int tic = 0;
	int[] tickies = new int[0];

	// / Make an array to hold all the information for the button locs.
	// [x][0] = x
	// [x][1] = y
	// [x][2] = w
	// [x][3] = h
	// [x][4] = graphics ##
	// [x][5] = what it does
	int[][] butts = { { 50, 450, 76, 40, 1, 0 }, { 50, 530, 76, 20, 1, 1 } };
	int[][] towerButts = { { 32, 120, 32, 32, 1, 3 },
			{ 84, 120, 32, 32, 1, 4 }, { 136, 120, 32, 32, 1, 5 } };
	int[][] tempButts = { { 50, 500, 88, 20, 1, 2 } };

	// 1 is delete, 2 is upgrade, 0 is next?

	static ArrayList<int[]> mapGround;
	static ArrayList<int[]> mapTowers;

	int towerSelect = -1;

	int round = 0;
	boolean shiftP = false;

	// gets the height of the top JFrame bar and margin
	static int topInset = 0;

	// start to first?
	// coordinates of points the player is going to purseue;
	// int[][] targetNumbers = { { 9, 3 }, { 9, 14 }, { 6, 16 } };
	int[][] targetNumbers = { { 6, 16 } };

	/**
	 * Start of Methods
	 */

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

		mapGround = TextInit.getmapGround();
		mapTowers = TextInit.getmapTowers();

		gStart();
	}

	/**
	 * Your methods go below here.
	 */

	public void gStart() {
		imageInit();

		// towerInit();
		towers = new ArrayList<int[]>();

		drawTargets();

		running = true;
		gLoop();
	}

	double relativeX;
	double relativeY;

	int alive = 0;

	public void gLoop() {
		while (running) {
			if (lives <= 0) {
				ArrayList<String> loseString = new ArrayList<String>();
				// System.out.println(" You Lose Nerd  " + alive + " alive ");
				loseString.add(" You Lose Nerd");
				loseString.add(" " + alive + " alive");
				txtBox(g1, 168, 38, 0, 62, 100, loseString);
				// running = false;
				drwGm();
			} else {
				dealWithMouse();

				// Do the things you want the gLoop to do below here
				getMouseLoc();

				checkEnd();

				drawMap();
				drawWave();
				towerCheck();

				drawSide();

				highlightButts();

				// Highlight selectedButton
				if (selectedTower != -1) {
					g1.drawImage(imageAr[7], towers.get(selectedTower)[0] * 32,
							towers.get(selectedTower)[1] * 32, null);
					// System.out.println("sle: " + selectedTower);

					ArrayList<String> downs = new ArrayList<String>();

					double tow = (double) (20)
							/ (double) (towers.get(selectedTower)[4]);

					double d = tow;
					BigDecimal bd = new BigDecimal(d);
					bd = bd.round(new MathContext(3));
					double rounded = bd.doubleValue();
					// Add a text box with the description of the tower.
					String tower0Dis = "Tower " + selectedTower;
					// String t2 = "Costs " + towers.get(selectedTower)[5];
					String t3 = "Deals " + towers.get(selectedTower)[3]
							+ " dmg";
					String t4 = "AttkSp " + rounded;
					String t5 = "BlockRange " + towers.get(selectedTower)[2]
							/ 64;

					downs.add(tower0Dis);
					// downs.add(t2);
					downs.add(t3);
					downs.add(t4);
					downs.add(t5);

					txtBox(g2, 160, 82, 0, 20, 300, downs);

				}

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
	}

	/*
	 * double d = ...; BigDecimal bd = new BigDecimal(d); bd = bd.round(new
	 * MathContext(3)); double rounded = bd.doubleValue();
	 */

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

	void drawMap() {
		for (int h = 0; h < mapGround.size(); h++) {
			for (int v = 0; v < mapGround.get(h).length; v++) {
				g1.drawImage(imageAr[mapGround.get(h)[v]], v * 32, h * 32, null);
			}
		}
		for (int h = 0; h < mapTowers.size(); h++) {
			for (int v = 0; v < mapTowers.get(h).length; v++) {
				g1.drawImage(imageAr[mapTowers.get(h)[v]], v * 32, h * 32, null);
			}
		}
	}

	void drawTargets() {
		for (int i = 0; i < targetNumbers.length; i++) {
			changeMap(targetNumbers[i][1], targetNumbers[i][0], 3);
		}
	}

	// make an entity and have them walk from the top to the bottom.
	// entity will need to have hp and speed and defence and walking
	// capabilities.
	int ent1X = 6 * 32;
	int ent1Y;

	int tower2SpashRange = 40;

	public static int getMapVar(int x, int y) {
		// System.out.println("lay: " + lan.get(0).length);
		// System.out.println("lan: " + lan.size());
		try {
			return mapTowers.get(y)[x];
		} catch (Exception ex) {
			return -1;
		}
	}

	public static void changeMap(int y, int x, int z) {
		mapTowers.get(y)[x] = z;
	}

	void towerCheck() {
		if (cmbt) {
			// checks missile size and adajusts it accordinly.
			if (towers.size() > missile.length) {
				int[][] buff = missile;
				missile = new int[towers.size()][];
				for (int i = 0; i < buff.length; i++) {
					missile[i] = buff[i];
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

			// move missile
			for (int i = 0; i < missile.length; i++) {
				if (missile[i] != null) {
					// if the mob dies this crashes
					if (mobAr[missile[i][2]] != null) {

						// move hypotnuse to target. distnace /mussels[3];
						// System.out.println("1: " + mobAr[missile[i][2]][0]);
						// System.out.println("2: " + missile[i][0] + 16);
						int deltX = mobAr[missile[i][2]][0] - missile[i][0]
								+ 16;
						int deltY = mobAr[missile[i][2]][1] - missile[i][1]
								+ 16;

						if (missile[i] == null) {
							System.out
									.println("ITS FUCKING NULL DAWG DONT KNOW WHAI");
						}
						missile[i][0] += deltX / missile[i][3];
						missile[i][1] += deltY / missile[i][3];

						// System.out.println("misX: " + missile[i][0]
						// + ", TarX: " + mobAr[missile[i][2]][0]);
						// System.out.println("misY: " + missile[i][1]
						// + ", TarY: " + mobAr[missile[i][2]][1]);

						// If the missel hits the target

						g1.drawOval(missile[i][0] - 3, missile[i][1] - 3, 6, 6);

						if (missile[i][0] == mobAr[missile[i][2]][0] + 16
								&& missile[i][1] == mobAr[missile[i][2]][1] + 16) {
							missile[i] = null;
							// System.out.println("HIT!");
						} else {
							missile[i][3] -= 1;
						}
						// move change in x/missile[i][3];
					}
				}
			}

			// tower deciding to shoot or not.
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
							if (tickies[i] == towers.get(i)[4]) {
								// if its an aoe tower
								if (towers.get(i)[5] == 2) {
									// System.out.println("tower2");
									// search the mobAr for mobs withing 10px
									for (int bee = 0; bee < mobAr.length; bee++) {
										// needs to make sure it doesnt hit the
										// target, and make sure it can only
										// attack one thing at a time.
										// No deal damage to everything in
										// range, but make sure the target
										// doesn't get hit with splash damage.
										if (mobAr[bee] != null
												&& mobAr[dee] != null) {
											if ((Math.abs(mobAr[bee][0]
													- mobAr[dee][0]) <= tower2SpashRange)
													&& (Math.abs(mobAr[bee][1]
															- mobAr[dee][1]) <= tower2SpashRange)) {
												// if (bee != dee) {
												mobAr[bee][2] -= towers.get(i)[3];
												System.out.println("hit: "
														+ timer());
												// draw something at the tower
												// to show splash
												g1.drawOval(mobAr[bee][0] + 14,
														mobAr[bee][1] + 14, 4,
														4);
												missile[i] = new int[] {
														towers.get(i)[0] * 32 + 16,
														towers.get(i)[1] * 32 + 16,
														dee, 4 };
												// }
											}
										}
										tickies[i] = 0;
									}
								} else {
									mobAr[dee][2] -= towers.get(i)[3];
									tickies[i] = 0;
									// g1.drawLine(towers.get(i)[0] * 32 + 16,
									// towers.get(i)[1] * 32 + 16,
									// mobAr[dee][0] + 16, mobAr[dee][1] + 16);
									missile[i] = new int[] {
											towers.get(i)[0] * 32 + 16,
											towers.get(i)[1] * 32 + 16, dee, 4 };
								}
							}
						}
					}
				}
				if (tickies[i] < towers.get(i)[4]) {
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

	ArrayList<String> textBox;

	// make buttons on the side that decide what tower your going to place
	// your money
	// your health

	void drawSide() {
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, w2, h2);
		alive = 0;
		for (int i = 0; i < mobAr.length; i++) {
			if (mobAr[i] != null) {
				alive++;
			}
		}

		textBox = new ArrayList<String>();
		textBox.add("money " + money);
		textBox.add("lives " + lives);
		textBox.add("round " + round);
		textBox.add("alive " + (alive - (mobAr.length - spawning)));
		textBox.add("spawning " + (mobAr.length - spawning));
		txtBox(g2, 180, 86, 0, 10, 10, textBox);

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
		for (int b = 0; b < towerButts.length; b++) {
			g2.drawImage(imageAr[b + 4], towerButts[b][0], towerButts[b][1],
					null);
		}
		// Highlight selectedButton
		if (selectedTower != -1) {
			if (towers.get(selectedTower)[6] == 0) {
				// Draws upgrade butt
				g2.setColor(Color.LIGHT_GRAY);
				g2.fillRect(tempButts[0][0], tempButts[0][1], tempButts[0][2],
						tempButts[0][3]);

				g2.setColor(Color.BLACK);
				int[] b = converter("upGrade");
				for (int c = 0; c < b.length; c++) {
					g2.drawImage(txtAr[b[c]], tempButts[0][0] + 2 + (c * 12),
							tempButts[0][1] + (tempButts[0][3] / 2) - 9, null);
				}
			}
		}

		// draws name on butts
		int[] b = converter("next");
		for (int c = 0; c < b.length; c++) {
			g2.drawImage(txtAr[b[c]], butts[0][0] + 2 + (c * 12), butts[0][1]
					+ (butts[0][3] / 2) - 9, null);
		}

		b = converter("delete");
		for (int c = 0; c < b.length; c++) {
			g2.drawImage(txtAr[b[c]], butts[1][0] + 2 + (c * 12), butts[1][1]
					+ (butts[1][3] / 2) - 9, null);
		}

	}

	int lastButt;

	// tests to see if the path is traversable once the new block is placed
	boolean testPath(int x, int y) {

		PathFind tempPath;
		boolean nigel = true;
		// System.out.println("targetNumbers.l: " + targetNumbers.length);
		// Should have it run through the target numbers
		for (int i = 0; i < targetNumbers.length; i++) {
			nigel = true;
			tempPath = (new PathFind(targetNumbers[i][0], targetNumbers[i][1],
					6, 1, this));
			if (tempPath.error) {
				System.out.println("Incomplete");
				nigel = false;
			}
		}

		/**
		 * // test path the 1st target then 2nd // boolean nigel = true;
		 * PathFind tempPath = (new PathFind(9, 3, 6, 1, this)); if
		 * (tempPath.error) { System.out.println("Incomplete"); nigel = false; }
		 * else { PathFind temp1Path = (new PathFind(x, y, 9, 3, this)); if
		 * (temp1Path.error) { System.out.println("Incomplete"); nigel = false;
		 * } }
		 */
		tempPath = null;
		return nigel;
	}

	void addTower(int x, int y, int tower) {
		// after it checks to see if a tower can go there it needs to add a
		// tower to the ArrayList
		int one = towerList[tower][0];
		int two = towerList[tower][1];
		int tre = towerList[tower][2];
		towers.add(new int[] { x, y, one, two, tre, tower, 0 });
	}

	void highlightButts() {
		if (towerSelect == 0) {
			g2.setColor(Color.BLUE);
			g2.drawRect(towerButts[0][0], towerButts[0][1],
					towerButts[0][2] - 1, towerButts[0][3] - 1);
		}
		if (towerSelect == 1) {
			g2.setColor(Color.BLUE);
			g2.drawRect(towerButts[1][0], towerButts[1][1],
					towerButts[1][2] - 1, towerButts[1][3] - 1);
		}
		if (towerSelect == 2) {
			g2.setColor(Color.BLUE);
			g2.drawRect(towerButts[2][0], towerButts[2][1],
					towerButts[2][2] - 1, towerButts[2][3] - 1);
		}

		ArrayList<String> downs = new ArrayList<String>();

		/**
		 * !!! Make this shit automated. !!!
		 */
		if (towerSelect == 0) {
			double tow = (double) (20) / (double) (towerList[0][2]);

			double d = tow;
			BigDecimal bd = new BigDecimal(d);
			bd = bd.round(new MathContext(3));
			double rounded = bd.doubleValue();

			// Add a text box with the description of the tower.
			String tower0Dis = "Arrow Tower";
			String t2 = "Costs " + towerList[0][3];
			String t3 = "Deals " + towerList[0][1] + " dmg";
			String t4 = "AttkSp " + rounded;
			String t5 = "BlockRange " + towerList[2][0] / 64;

			downs.add(tower0Dis);
			downs.add(t2);
			downs.add(t3);
			downs.add(t4);
			downs.add(t5);
		}
		if (towerSelect == 1) {
			double tow = (double) (20) / (double) (towerList[1][2]);

			double d = tow;
			BigDecimal bd = new BigDecimal(d);
			bd = bd.round(new MathContext(3));
			double rounded = bd.doubleValue();
			// Add a text box with the description of the tower.
			String tower1Dis = "LongBows";
			String t2 = "Costs " + towerList[1][3];
			String t3 = "Deals " + towerList[1][1] + " dmg";
			String t4 = "AttkSp " + rounded;
			String t5 = "BlockRange " + towerList[2][0] / 64;

			downs.add(tower1Dis);
			downs.add(t2);
			downs.add(t3);
			downs.add(t4);
			downs.add(t5);
		}
		if (towerSelect == 2) {
			double tow = (double) (20) / (double) (towerList[2][2]);

			double d = tow;
			BigDecimal bd = new BigDecimal(d);
			bd = bd.round(new MathContext(3));
			double rounded = bd.doubleValue();
			// Add a text box with the description of the tower.
			String tower0Dis = "Cannon Tower";
			String t2 = "Costs " + towerList[2][3];
			String t3 = "Deals " + towerList[2][1] + " dmg";
			String t4 = "AttkSp " + rounded;
			String t5 = "BlockRange " + towerList[2][0] / 64;

			downs.add(tower0Dis);
			downs.add(t2);
			downs.add(t3);
			downs.add(t4);
			downs.add(t5);
		}
		txtBox(g2, 160, 82, 0, 20, 300, downs);
	}

	// 0 = x
	// 1 = y
	// 2 == curHp
	// 3 == maxHp
	int[][] mobAr = { { 6 * 32, 1 * 32, 100, 100 },
			{ 6 * 32, 1 * 32, 100, 100 } };
	PathFind[] mobPath;

	boolean cmbt = false;

	boolean one = false;
	boolean needToSpawn = false;

	// spawn mob method gets called every tick once the round starts
	// every X ticks spawn a new mob.
	// initialize the pathfind for that mob.

	// runs path for all initialized pathFinds.
	void waveTick() {
		// System.out.println("mobAr.l: " + mobAr.length);

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
			if (spawnTick == 1) {
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

	void drawWave() {
		for (int i = 0; i < mobAr.length; i++) {
			try {
				g1.drawImage(imageAr[2], mobAr[i][0], mobAr[i][1], null);
				drawHealth(mobAr[i][0], mobAr[i][1], mobAr[i][2], mobAr[i][3]);
			} catch (Exception ex) {
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
		// make a pathfind going to the first target the once that is done make
		// it go to the second target
		mobPath[a] = new PathFind(9, 3,
				((mobAr[a][0] + 16) - ((mobAr[a][0] + 16) % 32)) / 32,
				((mobAr[a][1] + 16) - ((mobAr[a][1] + 16) % 32)) / 32, this, a,
				targetNumbers, 0);

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
			} else if (st.charAt(b) == 'A') {
				nw[b] = 0;
			} else if (st.charAt(b) == 'b') {
				nw[b] = 1;
			} else if (st.charAt(b) == 'B') {
				nw[b] = 1;
			} else if (st.charAt(b) == 'c') {
				nw[b] = 2;
			} else if (st.charAt(b) == 'C') {
				nw[b] = 2;
			} else if (st.charAt(b) == 'd') {
				nw[b] = 3;
			} else if (st.charAt(b) == 'D') {
				nw[b] = 3;
			} else if (st.charAt(b) == 'e') {
				nw[b] = 4;
			} else if (st.charAt(b) == 'E') {
				nw[b] = 4;
			} else if (st.charAt(b) == 'f') {
				nw[b] = 5;
			} else if (st.charAt(b) == 'F') {
				nw[b] = 5;
			} else if (st.charAt(b) == 'g') {
				nw[b] = 6;
			} else if (st.charAt(b) == 'G') {
				nw[b] = 6;
			} else if (st.charAt(b) == 'h') {
				nw[b] = 7;
			} else if (st.charAt(b) == 'H') {
				nw[b] = 7;
			} else if (st.charAt(b) == 'i') {
				nw[b] = 8;
			} else if (st.charAt(b) == 'I') {
				nw[b] = 8;
			} else if (st.charAt(b) == 'j') {
				nw[b] = 9;
			} else if (st.charAt(b) == 'J') {
				nw[b] = 9;
			} else if (st.charAt(b) == 'k') {
				nw[b] = 10;
			} else if (st.charAt(b) == 'K') {
				nw[b] = 10;
			} else if (st.charAt(b) == 'l') {
				nw[b] = 11;
			} else if (st.charAt(b) == 'L') {
				nw[b] = 11;
			} else if (st.charAt(b) == 'm') {
				nw[b] = 12;
			} else if (st.charAt(b) == 'M') {
				nw[b] = 12;
			} else if (st.charAt(b) == 'n') {
				nw[b] = 13;
			} else if (st.charAt(b) == 'N') {
				nw[b] = 13;
			} else if (st.charAt(b) == 'o') {
				nw[b] = 14;
			} else if (st.charAt(b) == 'O') {
				nw[b] = 14;
			} else if (st.charAt(b) == 'p') {
				nw[b] = 15;
			} else if (st.charAt(b) == 'P') {
				nw[b] = 15;
			} else if (st.charAt(b) == 'q') {
				nw[b] = 16;
			} else if (st.charAt(b) == 'Q') {
				nw[b] = 16;
			} else if (st.charAt(b) == 'r') {
				nw[b] = 17;
			} else if (st.charAt(b) == 'R') {
				nw[b] = 17;
			} else if (st.charAt(b) == 's') {
				nw[b] = 18;
			} else if (st.charAt(b) == 'S') {
				nw[b] = 18;
			} else if (st.charAt(b) == 't') {
				nw[b] = 19;
			} else if (st.charAt(b) == 'T') {
				nw[b] = 19;
			} else if (st.charAt(b) == 'u') {
				nw[b] = 20;
			} else if (st.charAt(b) == 'U') {
				nw[b] = 20;
			} else if (st.charAt(b) == 'v') {
				nw[b] = 21;
			} else if (st.charAt(b) == 'V') {
				nw[b] = 21;
			} else if (st.charAt(b) == 'w') {
				nw[b] = 22;
			} else if (st.charAt(b) == 'W') {
				nw[b] = 22;
			} else if (st.charAt(b) == 'x') {
				nw[b] = 23;
			} else if (st.charAt(b) == 'X') {
				nw[b] = 23;
			} else if (st.charAt(b) == 'y') {
				nw[b] = 24;
			} else if (st.charAt(b) == 'Y') {
				nw[b] = 24;
			} else if (st.charAt(b) == 'z') {
				nw[b] = 25;
			} else if (st.charAt(b) == 'Z') {
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
			} else if (st.charAt(b) == ',') {
				nw[b] = 37;
			} else if (st.charAt(b) == '?') {
				nw[b] = 38;
			} else if (st.charAt(b) == '¿') {
				nw[b] = 39;
			} else if (st.charAt(b) == '(') {
				nw[b] = 40;
			} else if (st.charAt(b) == ')') {
				nw[b] = 41;
			} else if (st.charAt(b) == 'é') {
				nw[b] = 4;
			} else if (st.charAt(b) == 'á') {
				nw[b] = 0;
			} else if (st.charAt(b) == 'ó') {
				nw[b] = 14;
			} else if (st.charAt(b) == 'í') {
				nw[b] = 8;
			} else if (st.charAt(b) == '.') {
				nw[b] = 26;
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
					// check to see if it is finished.
					// only called when its path is done.
					lives -= 1;
					mobAr[i] = null;
					mobPath[i] = null;
				}
			}
		}
	}

	void succeedThis(int id, int step) {
		// an id gets pluged in, so it finds the pathfinding with that id and
		// then makes a new pathfinding to the final target
		int tempStep = step + 1;
		for (int i = 0; i < mobPath.length; i++) {
			if (mobPath[i] != null) {
				if (mobPath[i].id == id) {
					// this is it.
					mobPath[i] = null;
					mobPath[i] = new PathFind(endX, endY,
							(mobAr[i][0] - (mobAr[i][0] % 32)) / 32,
							(mobAr[i][1] - (mobAr[i][1] % 32)) / 32, this, id,
							targetNumbers, tempStep);
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
		for (int a = 0; a < towerButts.length; a++) {
			if (towerButts[a][4] == 1) {
				if (migg.getY() > towerButts[a][1]) {
					if (migg.getY() < towerButts[a][1] + towerButts[a][3]) {
						if (migg.getX() - w1 > towerButts[a][0]) {
							if (migg.getX() - w1 < towerButts[a][0]
									+ towerButts[a][2]) {
								buttonP(towerButts[a][5], 0);
								buttPressed = true;
							}
						}
					}
				}
			}
		}
		for (int a = 0; a < tempButts.length; a++) {
			if (tempButts[a][4] == 1) {
				if (migg.getY() > tempButts[a][1]) {
					if (migg.getY() < tempButts[a][1] + tempButts[a][3]) {
						if (migg.getX() - w1 > tempButts[a][0]) {
							if (migg.getX() - w1 < tempButts[a][0]
									+ tempButts[a][2]) {
								buttonP(tempButts[a][5], 0);
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
	int[][] missile;

	boolean mouseClicked = false;
	int[] mouseInfo = new int[2];

	void dealWithMouse() {
		if (mouseClicked) {
			mouseClicked = false;
			System.out.println("MouseClicked: (" + mouseInfo[0] + ", "
					+ mouseInfo[1] + ")");
			if (mouseInfo[0] == 0) {
				if (!roundOn) {
					roundOn = true;
					cmbt = true;
					// next button
					spawning = 0;
					mobPath = new PathFind[allThese[round].length];
					mobAr = new int[allThese[round].length][];
					mobAr = allThese[round];
					missile = new int[towers.size()][];
					needToSpawn = true;
					round++;
				}
			} else if (mouseInfo[0] == 1) {
				// delete this tower
				if (selectedTower != -1) {
					changeMap(towers.get(selectedTower)[1],
							towers.get(selectedTower)[0], 0);
					towers.remove(selectedTower);
					selectedTower = -1;
				}
			} else if (mouseInfo[0] == 2) {
				// upgrade tower.
				// cant upgrade arrow tower, to guchi
				if (selectedTower != -1) {
					if (!(towers.get(selectedTower)[5] == 0)) {
						if (towers.get(selectedTower)[6] == 0) {
							if (money >= 8) {
								towers.get(selectedTower)[3] += 4;
								towers.get(selectedTower)[6] += 1;
								money -= 8;
							}
						}
					}
				}
			} else if (mouseInfo[0] == 3) {
				towerSelect = 0;
				selectedTower = -1;
			} else if (mouseInfo[0] == 4) {
				towerSelect = 1;
				selectedTower = -1;
			} else if (mouseInfo[0] == 5) {
				towerSelect = 2;
				selectedTower = -1;
			}
			// money -= 10;
		}
	}

	public void buttonP(int a, int b) {
		mouseClicked = true;
		mouseInfo[0] = a;
		mouseInfo[1] = b;

	}

	// list of what the first 10 waves will be
	// 0 = x, 1 = y, 2 = curHp, 3 maxHp
	int[][][] allThese = new int[10][][];

	void setAllThese() {
		allThese[0] = new int[][] { { 6 * 32, 1 * 32, 80, 80 },
				{ 6 * 32, 1 * 32, 80, 80 }, { 6 * 32, 1 * 32, 80, 80 } };
		allThese[1] = new int[][] { { 6 * 32, 1 * 32, 80, 80 },
				{ 6 * 32, 1 * 32, 80, 80 }, { 6 * 32, 1 * 32, 80, 80 },
				{ 6 * 32, 1 * 32, 120, 120 }, { 6 * 32, 1 * 32, 120, 120 } };
		allThese[2] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 200, 200 }, { 6 * 32, 1 * 32, 200, 200 },
				{ 6 * 32, 1 * 32, 200, 200 }, { 6 * 32, 1 * 32, 200, 200 },
				{ 6 * 32, 1 * 32, 200, 200 } };
		allThese[3] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 200, 200 }, { 6 * 32, 1 * 32, 200, 200 },
				{ 6 * 32, 1 * 32, 200, 200 }, { 6 * 32, 1 * 32, 200, 200 },
				{ 6 * 32, 1 * 32, 200, 200 } };
		allThese[4] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 250, 250 }, { 6 * 32, 1 * 32, 250, 250 },
				{ 6 * 32, 1 * 32, 250, 250 }, { 6 * 32, 1 * 32, 250, 250 },
				{ 6 * 32, 1 * 32, 250, 250 }, { 6 * 32, 1 * 32, 250, 250 },
				{ 6 * 32, 1 * 32, 250, 250 }, { 6 * 32, 1 * 32, 250, 250 },
				{ 6 * 32, 1 * 32, 250, 250 } };
		allThese[5] = new int[][] { { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 100, 100 }, { 6 * 32, 1 * 32, 100, 100 },
				{ 6 * 32, 1 * 32, 200, 200 }, { 6 * 32, 1 * 32, 200, 200 },
				{ 6 * 32, 1 * 32, 200, 200 }, { 6 * 32, 1 * 32, 200, 200 },
				{ 6 * 32, 1 * 32, 350, 350 }, { 6 * 32, 1 * 32, 350, 350 },
				{ 6 * 32, 1 * 32, 350, 350 }, { 6 * 32, 1 * 32, 350, 350 },
				{ 6 * 32, 1 * 32, 350, 350 }, { 6 * 32, 1 * 32, 350, 350 },
				{ 6 * 32, 1 * 32, 350, 350 }, { 6 * 32, 1 * 32, 350, 350 },
				{ 6 * 32, 1 * 32, 350, 350 }, { 6 * 32, 1 * 32, 350, 350 },
				{ 6 * 32, 1 * 32, 350, 350 }, { 6 * 32, 1 * 32, 350, 350 },
				{ 6 * 32, 1 * 32, 350, 350 }, { 6 * 32, 1 * 32, 350, 350 },
				{ 6 * 32, 1 * 32, 400, 400 }, { 6 * 32, 1 * 32, 400, 400 },
				{ 6 * 32, 1 * 32, 400, 400 }};
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

	public void imageInit() {
		imageAr = new Image[8];
		ImageIcon ii = new ImageIcon(this.getClass().getResource(
				"res/Grass1/GrassL.png"));
		imageAr[0] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Wall2/Wall2.png"));
		imageAr[1] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/evilSmall.png"));
		imageAr[2] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Grass1/Bush1.png"));
		imageAr[3] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/tower0.png"));
		imageAr[4] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/tower1.png"));
		imageAr[5] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/tower2.png"));
		imageAr[6] = ii.getImage();
		ii = new ImageIcon(this.getClass().getResource("res/Outline.png"));
		imageAr[7] = ii.getImage();

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

	boolean towersToBeAdded;
	// information to be had,
	// 0 = x
	// 1 = y
	// 2 = tower type
	ArrayList<int[]> addedTowers;

	@Override
	public void mousePressed(MouseEvent me) {
		addedTowers = new ArrayList<int[]>();
		// find which block im cliking on.
		buttons(me);
		// If the click is on the map.
		if (me.getX() < 13 * 32) {
			int x = (me.getX() - (me.getX() % 32)) / 32;
			int y = (me.getY() - (me.getY() % 32)) / 32;
			if (!roundOn) {
				boolean towerPlaced = false;
				for (int i = 0; i < towerList.length; i++) {
					if (towerSelect == i) {
						if (getMapVar(x, y) == 0) {
							if (money >= towerList[towerSelect][3]) {
								// changes map
								changeMap(y, x, towerSelect + 4);
								// sees if there is path
								if (testPath(endX, endY)) {
									// if there is it leaves it alone.
									addTower(x, y, i);
									towerPlaced = true;
									money -= towerList[towerSelect][3];
								} else {
									// if there is not then change map back to
									// blank.
									// should make a buffer int of what that
									// block
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
				}
				boolean foundTower = false;
				// Search to see if you click on one.
				if (!towerPlaced)
					for (int i = 0; i < towers.size(); i++) {
						if (x == towers.get(i)[0] && y == towers.get(i)[1]) {
							selectedTower = i;
							System.out.println("setSlece");
							foundTower = true;
							towerSelect = -1;
						}
					}
				if (!foundTower) {
					selectedTower = -1;
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
	}

	@Override
	public void mouseClicked(MouseEvent me) {

	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
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
