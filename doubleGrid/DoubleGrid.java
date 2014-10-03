package TowerSlug.doubleGrid;

import javax.swing.JFrame;

public class DoubleGrid {
	// extends JFrame
	public DoubleGrid() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new Panel());
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setTitle("DoubleGrid");
	}

	public static void main(String[] args) {
		new DoubleGrid();
	}
}
