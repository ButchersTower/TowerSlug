package TowerSlug;

import java.util.ArrayList;

public class PathFind {

	// Need to make exceptions for when the player is going northwest and he
	// can walk north because that is the door, and the wall allows him to walk
	// east but then not north because there is a wall.

	// How to work with pathfinding with variable speeds not related to ticks
	// but instead it is px per tick.
	// Pass over an array of box locations. Then on each tick it moves in the
	// direction. does it convert the location to px, instead of box.

	// Player loc
	int px = 0;
	int py = 0;

	int startX = 0;
	int startY = 0;

	// Target loc
	int tx = 0;
	int ty = 0;

	int id;
	ArrayList<int[]> open;
	ArrayList<int[]> closed;

	Panel play;

	boolean error = false;

	// Blocks that cant be enterd from (with the character going in) the
	// direction.
	int[] eN = { -1 };
	int[] eE = { -1 };
	int[] eS = { -1 };
	int[] eW = { -1 };

	// blocks player can't exit in that direction
	int[] xN = { -1 };
	int[] xE = { -1 };
	int[] xS = { -1 };
	int[] xW = { -1 };

	int[] eNW = { -1 };
	int[] eNE = { -1 };
	int[] eSE = { -1 };
	int[] eSW = { -1 };

	int[] xNW = { -1 };
	int[] xNE = { -1 };
	int[] xSE = { -1 };
	int[] xSW = { -1 };

	int[] impBlocks = { 1, 4, 5, 6 };

	// used for checking the path to see if it goes directly from player to
	// target
	boolean pathDone = false;

	// direct path to target
	ArrayList<int[]> path;

	public PathFind(int tx, int ty, int px, int py, Panel play) {
		// plug in the block of player and target
		this.tx = tx;
		this.ty = ty;
		this.px = px;
		this.py = py;
		this.play = play;
		startX = tx;
		startY = ty;

		setImps();

		closed = new ArrayList<int[]>();
		open = new ArrayList<int[]>();
		closed.add(new int[] { px, py, -1, 0, -1, -1 });

		beginSearch();
	}

	boolean wave = false;

	int[][] targetNumbers;

	boolean steps = false;

	// number of target points teh target has passed.
	int step;

	public PathFind(int tx, int ty, int px, int py, Panel play, int id,
			int[][] tars, int step) {
		targetNumbers = tars;
		wave = true;
		// plug in the block of player and target
		this.tx = targetNumbers[step][0];
		this.ty = targetNumbers[step][1];
		this.px = px;
		this.py = py;
		this.play = play;
		this.id = id;
		startX = tx;
		startY = ty;
		steps = true;
		this.step = step;

		setImps();

		closed = new ArrayList<int[]>();
		open = new ArrayList<int[]>();
		closed.add(new int[] { px, py, -1, 0, -1, -1 });

		beginSearch();
	}

	void setImps() {
		int[] temp = null;
		for (int i = 0; i < impBlocks.length; i++) {
			temp = new int[(eN.length + impBlocks.length)];
			for (int ig = 0; ig < eN.length; ig++) {
				temp[ig] = eN[ig];
				for (int ih = 0; ih < impBlocks.length; ih++) {
					temp[ih + eN.length] = impBlocks[ih];
				}
			}
		}
		eN = new int[eN.length + impBlocks.length];
		eN = temp;

		for (int i = 0; i < impBlocks.length; i++) {
			temp = new int[(eE.length + impBlocks.length)];
			for (int ig = 0; ig < eE.length; ig++) {
				temp[ig] = eE[ig];
				for (int ih = 0; ih < impBlocks.length; ih++) {
					temp[ih + eE.length] = impBlocks[ih];
				}
			}
		}
		eE = new int[eE.length + impBlocks.length];
		eE = temp;

		for (int i = 0; i < impBlocks.length; i++) {
			temp = new int[(eS.length + impBlocks.length)];
			for (int ig = 0; ig < eS.length; ig++) {
				temp[ig] = eS[ig];
				for (int ih = 0; ih < impBlocks.length; ih++) {
					temp[ih + eS.length] = impBlocks[ih];
				}
			}
		}
		eS = new int[eS.length + impBlocks.length];
		eS = temp;

		for (int i = 0; i < impBlocks.length; i++) {
			temp = new int[(eW.length + impBlocks.length)];
			for (int ig = 0; ig < eW.length; ig++) {
				temp[ig] = eW[ig];
				for (int ih = 0; ih < impBlocks.length; ih++) {
					temp[ih + eW.length] = impBlocks[ih];
				}
			}
		}
		eW = new int[eW.length + impBlocks.length];
		eW = temp;

		for (int i = 0; i < impBlocks.length; i++) {
			temp = new int[(xNW.length + impBlocks.length)];
			for (int ig = 0; ig < xNW.length; ig++) {
				temp[ig] = xNW[ig];
				for (int ih = 0; ih < impBlocks.length; ih++) {
					temp[ih + xNW.length] = impBlocks[ih];
				}
			}
		}
		eNW = new int[eNW.length + impBlocks.length];
		eNW = temp;

		for (int i = 0; i < impBlocks.length; i++) {
			temp = new int[(eNE.length + impBlocks.length)];
			for (int ig = 0; ig < eNE.length; ig++) {
				temp[ig] = eNE[ig];
				for (int ih = 0; ih < impBlocks.length; ih++) {
					temp[ih + eNE.length] = impBlocks[ih];
				}
			}
		}
		eNE = new int[eNE.length + impBlocks.length];
		eNE = temp;

		for (int i = 0; i < impBlocks.length; i++) {
			temp = new int[(eSE.length + impBlocks.length)];
			for (int ig = 0; ig < eSE.length; ig++) {
				temp[ig] = eSE[ig];
				for (int ih = 0; ih < impBlocks.length; ih++) {
					temp[ih + eSE.length] = impBlocks[ih];
				}
			}
		}
		eSE = new int[eSE.length + impBlocks.length];
		eSE = temp;

		for (int i = 0; i < impBlocks.length; i++) {
			temp = new int[(eSW.length + impBlocks.length)];
			for (int ig = 0; ig < eSW.length; ig++) {
				temp[ig] = eSW[ig];
				for (int ih = 0; ih < impBlocks.length; ih++) {
					temp[ih + eSW.length] = impBlocks[ih];
				}
			}
		}
		eSW = new int[eSW.length + impBlocks.length];
		eSW = temp;
	}

	void beginSearch() {
		getAdj(px, py, 0);
		while (!pathDone) {
			findLow();
		}
	}

	public void findLow() {
		// if the initial find adj didn't work because all of the blocks
		// couldn't be entered or players block can't be exited. So just stops
		// trying
		if (open.size() == 0) {
			pathDone = true;
			error = true;
		}

		// finds the value from the open list with the lowest p(layer) +
		// h(euristic) and sets it to g
		int g = 0;
		// System.out.println("howdoes it get here?\t1");
		for (int i = 1; i < open.size(); i++) {
			// System.out.println("howdoes it get here?\t2");
			// i = 1 because it automatically compares the i to g0. (if it was 0
			// it would compare it to itself the first time)
			if (open.get(i)[2] + open.get(i)[3] < open.get(g)[2]
					+ open.get(g)[3]) {
				g = i;
			}
		}

		try {
			// crashes because all of open gets added to the closed and there
			// are no more options.
			// adds the lowest value to the closed list.
			// System.out.println("open bugs: " + open.get(g)[0]);
			// System.out.println("open.get: "+open);
			addClosed(open.get(g));
			// removes it from the closed list
			open.remove(open.get(g));
			// adds the adj of the value just added to close list
			getAdj(closed.get(closed.size() - 1)[0],
					closed.get(closed.size() - 1)[1],
					closed.get(closed.size() - 1)[3]);
		} catch (Exception ex) {
			error = true;
			if (step == 1) {
				System.out.println("Error!");
			}
		}
	}

	// Sets the 8 adj blocks to the open list.
	public void getAdj(int x, int y, int f) {
		// Temp int array
		// x, y, h, g, parentx, parenty
		int[] d = { x, y, getMd(x, y), f, -1, -1 };

		// That cardinal direction is available.
		boolean Na = false;
		boolean Ea = false;
		boolean Sa = false;
		boolean Wa = false;

		int xx;
		int yy;

		// takes x and y of the adj blocks
		// north
		xx = d[0];
		yy = d[1] - 1;
		// can the player exit from current block in this direction.
		if (exiting(0, Panel.getMapVar(x, y))) {
			// can the player enter new block in this direction
			if (entering(0, Panel.getMapVar(xx, yy))) {
				// can go in that direction (used for going diagonal)
				Na = true;
				// add to temp
				// istead of using temp just directly add using addOpen.
				addOpen(new int[] { xx, yy, getMd(xx, yy), d[3] + 9, x, y });
			}
		}
		// east
		xx = d[0] + 1;
		yy = d[1];
		if (exiting(1, Panel.getMapVar(x, y))) {
			if (entering(1, Panel.getMapVar(xx, yy))) {
				Ea = true;
				addOpen(new int[] { xx, yy, getMd(xx, yy), d[3] + 9, x, y });
			}
		}
		// south
		xx = d[0];
		yy = d[1] + 1;
		if (exiting(2, Panel.getMapVar(x, y))) {
			if (entering(2, Panel.getMapVar(xx, yy))) {
				Sa = true;
				addOpen(new int[] { xx, yy, getMd(xx, yy), d[3] + 9, x, y });
			}
		}
		// west
		xx = d[0] - 1;
		yy = d[1];
		if (exiting(3, Panel.getMapVar(x, y))) {
			if (entering(3, Panel.getMapVar(xx, yy))) {
				Wa = true;
				addOpen(new int[] { xx, yy, getMd(xx, yy), d[3] + 9, x, y });
			}
		}
		// play moving nw
		// if w or n is imp then dont add.
		if (Wa && Na) {

			xx = d[0] - 1;
			yy = d[1] - 1;
			if (exiting(4, Panel.getMapVar(x, y))) {
				if (entering(4, Panel.getMapVar(xx, yy))) {
					addOpen(new int[] { xx, yy, getMd(xx, yy), d[3] + 14, x, y });
				}
			}
		}
		// ne
		if (Na && Ea) {
			xx = d[0] + 1;
			yy = d[1] - 1;
			if (exiting(5, Panel.getMapVar(x, y))) {
				if (entering(5, Panel.getMapVar(xx, yy))) {
					addOpen(new int[] { xx, yy, getMd(xx, yy), d[3] + 14, x, y });
				}
			}
		}
		// sw
		if (Sa && Wa) {
			xx = d[0] - 1;
			yy = d[1] + 1;
			if (exiting(7, Panel.getMapVar(x, y))) {
				if (entering(7, Panel.getMapVar(xx, yy))) {
					addOpen(new int[] { xx, yy, getMd(xx, yy), d[3] + 14, x, y });
				}
			}
		}
		// se
		if (Sa && Ea) {
			xx = d[0] + 1;
			yy = d[1] + 1;
			if (exiting(6, Panel.getMapVar(x, y))) {
				if (entering(6, Panel.getMapVar(xx, yy))) {
					addOpen(new int[] { xx, yy, getMd(xx, yy), d[3] + 14, x, y });
				}
			}
		}
	}

	// check if player can enter a block
	boolean entering(int dir, int get) {
		// get is the block of type being tested
		if (dir == 0) {
			for (int i = 0; i < eN.length; i++) {
				if (get == eN[i]) {
					return false;

				}
			}
		} else if (dir == 1) {
			for (int i = 0; i < eE.length; i++) {
				if (get == eE[i]) {
					return false;
				}
			}
		} else if (dir == 2) {
			for (int i = 0; i < eS.length; i++) {
				if (get == eS[i]) {
					return false;
				}
			}
		} else if (dir == 3) {
			for (int i = 0; i < eW.length; i++) {
				if (get == eW[i]) {
					return false;
				}
			}
		} else if (dir == 4) {
			for (int i = 0; i < eNW.length; i++) {
				if (get == eNW[i]) {
					return false;
				}
			}
		} else if (dir == 5) {
			for (int i = 0; i < eNE.length; i++) {
				if (get == eNE[i]) {
					return false;
				}
			}
		} else if (dir == 6) {
			for (int i = 0; i < eSW.length; i++) {
				if (get == eSW[i]) {
					return false;
				}
			}
		} else if (dir == 7) {
			for (int i = 0; i < eSW.length; i++) {
				if (get == eSW[i]) {
					return false;
				}
			}
		}
		return true;
	}

	// check in player can exit a block
	boolean exiting(int dir, int get) {
		if (dir == 0) {
			for (int i = 0; i < xN.length; i++) {
				if (get == xN[i]) {
					return false;
				}
			}
		} else if (dir == 1) {
			for (int i = 0; i < xE.length; i++) {
				if (get == xE[i]) {
					return false;
				}
			}
		} else if (dir == 2) {
			for (int i = 0; i < xS.length; i++) {
				if (get == xS[i]) {
					return false;
				}
			}
		} else if (dir == 3) {
			for (int i = 0; i < xW.length; i++) {
				if (get == xW[i]) {
					return false;
				}
			}
		} else if (dir == 4) {
			for (int i = 0; i < xNW.length; i++) {
				if (get == xNW[i]) {
					return false;
				}
			}
		} else if (dir == 5) {
			for (int i = 0; i < xNE.length; i++) {
				if (get == xNE[i]) {
					return false;
				}
			}
		} else if (dir == 6) {
			for (int i = 0; i < xSE.length; i++) {
				if (get == xSE[i]) {
					return false;
				}
			}
		} else if (dir == 7) {
			for (int i = 0; i < xSW.length; i++) {
				if (get == xSW[i]) {
					return false;
				}
			}
		}
		return true;
	}

	// make a method to check for overlap on open list.
	// make an adding method for closed like i did for open

	void addOpen(int[] d) {
		// Check open (and closed) list to see if the x, y value being added is
		// already on
		int a = 0;
		for (int i = 0; i < open.size(); i++) {
			if (d[0] == open.get(i)[0]) {
				if (d[1] == open.get(i)[1]) {
					// if what your adding's g is lower than what is there. then
					// change parent.
					if (d[3] < open.get(i)[3]) {
						open.set(i, d);
					}
					a++;
				}
			}
		}
		// If no collisions have happened so far.
		if (a == 0) {
			// Checks closed list now
			for (int p = 0; p < closed.size(); p++) {
				if (d[0] == closed.get(p)[0]) {
					if (d[1] == closed.get(p)[1]) {
						a++;
					}
				}
			}
		}
		if (a == 0) {
			open.add(d);
		}
	}

	public void addClosed(int[] d) {
		int a = 0;
		// Check closed list to see if the x, y value being added is already on
		for (int p = 0; p < closed.size(); p++) {
			if (d[0] == closed.get(p)[0]) {
				if (d[1] == closed.get(p)[1]) {
					a++;
				}
			}
		}
		if (a == 0) {
			// sees if what is being added to closed is the target.
			closed.add(d);
			if (d[0] == tx) {
				if (d[1] == ty) {
					// If so it draws path and starts moving.
					// find the last added to closed list, should be on target
					path = new ArrayList<int[]>();
					path.add(closed.get(closed.size() - 1));
					while (!pathDone) {
						doStuff();
					}
				}
			}
		}
	}

	void doStuff() {
		// this runs through the
		int d = -1;
		// Searches all of closed for the parent of the node most recently
		// added to path.
		for (int i = 0; i < closed.size(); i++) {
			// searches closed for the parent x and y of the node most recently
			// added to path
			if (path.get(path.size() - 1)[4] == closed.get(i)[0]) {
				if (path.get(path.size() - 1)[5] == closed.get(i)[1]) {
					d = i;
				}
			}
		}
		if (d != -1) {
			// and then adds it to path
			path.add(closed.get(d));
		} else {
			// Cant find parent.
			// sice player loc's parent is -1, -1 (unacessable location) it
			// means that the path has reached the players block
			pathDone = true;
		}
	}

	// gets manhattan distance from block to target
	int getMd(int x, int y) {
		return (Math.abs(x - tx) * 10) + (Math.abs(y - ty) * 10);
	}

	boolean pathComplete = false;

	// used for moving player through path.
	int ga = -1;
	int gi = 0;

	boolean runOnce = true;

	void giInit() {
		if (runOnce)
			gi = path.size() - 2;
		runOnce = false;
	}

	int dx;
	int dy;

	int dirX;
	int dirY;

	int cx;
	int cy;

	int vertWalkSpeed = 4;

	// Checks to see if players location is the last square of the path
	void conCheck() {
		// constant check that gets ran every tick.
		// when this happens, once every 20 ticks (once a second) the player
		// moves through the path until it hits the target at which it deletes
		// the ArrayList
		if (!pathComplete) {
			// use this to stop the loop from running once it doesnt needto.
			if (path != null) {
				// System.out.println("not null");
				for (int i = 0; i < path.size(); i++) {
					// System.out.println("pathSize: "+path.size());
					// if players spot is in path
					if (path.get(i)[0] == px && path.get(i)[1] == py) {
						// now what, path al has everything the player needs to
						// move to. Start moving player every second
						pathComplete = true;
					}
				}
			}
		} else {
			giInit();
			if (ga == -1) {
				try {
					// block distance from player to next node
					cx = px - path.get(gi)[0];
					cy = py - path.get(gi)[1];
				} catch (Exception ex) {
					// Throws an error when at the end of its path because
					// gi = -1;
					cx = 0;
					cy = 0;
				}
				dx = 0;
				dy = 0;
				if (cy == 0 || cx == 0) {
					// if he is not walking diagonally.
					ga = 4;
				} else {
					ga = 6;
				}
			} else if (ga == 0) {
				if (gi >= 0) {
					px = path.get(gi)[0];
					py = path.get(gi)[1];
					dx = 0;
					dy = 0;
					gi -= 1;
					// ga = 0;
					if (wave) {
						play.mobAr[id][0] = px * 32;
						play.mobAr[id][1] = py * 32;
					} else {
					}
					// Delta to tar.
					// neg if going east or south
					try {
						cx = px - path.get(gi)[0];
						cy = py - path.get(gi)[1];
					} catch (Exception ex) {
						// Throws an error when at the end of its path because
						// gi = -1;
						cx = 0;
						cy = 0;
					}
					// Finds the direction of the next block.
					// 0 for no chance, 1 for positive chance, 2 for negative
					// change.
					if (cy == 0 || cx == 0) {
						// if he is not walking diagonally.
						ga = 4;
					} else {
						ga = 6;
					}
				}
			}
			if (ga > 0) {
				ga -= 1;

				if (cx == 0) {
					if (cy < 0) {
						dy += 8;
					}
					if (cy > 0) {
						dy -= 8;
					}
				} else if (cy == 0) {
					if (cx < 0) {
						dx += 8;
					}
					if (cx > 0) {
						dx -= 8;
					}
				}

				else if (cy < 0) {
					dy += 6;
					if (cx < 0) {
						// SE
						dx += 6;
					} else {
						// sw
						dx -= 6;
					}
				} else if (cy > 0) {
					dy -= 6;
					if (cx < 0) {
						// NE
						dx += 6;
					} else {
						// Nw
						dx -= 6;
					}
				}
				if (wave) {
					play.mobAr[id][0] = (px * 32) + dx;
					play.mobAr[id][1] = (py * 32) + dy;
				} else {
				}
			}
			if (px == tx && py == ty) {
				// play.deletePathFinding(id);
				// System.out.println("DEELEETEEE");
				if (step + 1 < targetNumbers.length) {
					// succeed this
					play.succeedThis(id, step);
				} else {
					play.removeThis(this);
				}
			}
		}
	}

	/**
	 * Optional labeling methods
	 */

	/*
	 * void drawPath() { try { for (int i = 0; i < path.size(); i++) {
	 * Panel.g1.drawImage(ImgLoad.small[0], path.get(i)[0] * 32, path.get(i)[1]
	 * * 32, null); } } catch (Exception ex) {
	 * 
	 * } }
	 * 
	 * // label boxes void lableBoxes() { for (int i = 0; i < closed.size();
	 * i++) { int[] b = Panel.converter(Integer.toString(closed.get(i)[3])); for
	 * (int c = 0; c < b.length; c++) { Panel.g1.drawImage(ImgLoad.txtMc[b[c]],
	 * (closed.get(i)[0] * 32) + 7 + (c * 6), (closed.get(i)[1] * 32) + 16,
	 * null); } } // Draws for open list for (int i = 0; i < open.size(); i++) {
	 * int[] b = Panel.converter(Integer.toString(open.get(i)[3])); for (int c =
	 * 0; c < b.length; c++) { Panel.g1.drawImage(ImgLoad.txtMc[b[c]],
	 * (open.get(i)[0] * 32) + 7 + (c * 6), (open.get(i)[1] * 32) + 16, null); }
	 * } }
	 */

}
