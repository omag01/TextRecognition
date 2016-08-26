import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * ReaderMain can be used to create a graphical user interface
 * to perform text recognition on a handwritten word. 
 */

public class ReaderMain {
	
	/** The Reader to read text with. */
	private Reader r;
	
	/** The outer frame of the GUI. */
	private JFrame frame;
	
	/** The canvas to write on (images automatically saved from this and read). */
	private DrawingCanvas canvas;
	
	/** The panel directly below the canvas (containing the label and button). */
	private JPanel south;
	
	/** The text representing the handwriting currently in the canvas. */
	private JLabel label;
	
	/** The button to clear any writing current on the canvas. */
	private JButton clearButton;
	
	/** Creates the GUI. */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		ReaderMain gui = new ReaderMain();
	}
	
	/** Constructs a new ReaderMain GUI. */
	public ReaderMain() {
		// initialize components
		tryToFixLookAndFeel();
		r = new Reader();
		frame = new JFrame();
		south = new JPanel();
		canvas = new DrawingCanvas(400, 400);
		label = new JLabel("Recognized text will appear here");
		clearButton = new JButton("Clear");
		
		// add listener to "clear" button
		clearButton.addActionListener(new ActionListener() {
			/**
			 * Clears the DrawingCanvas when the button is clicked.
			 * 
			 * @param e The ActionEvent representing the click on the "clear" button.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				canvas.clear();
				frame.repaint();
			}
		});
		
		// add button and label to south panel
		south.add(label, BorderLayout.CENTER);
		south.add(clearButton, BorderLayout.EAST);
		
		// add components to frame and set initial frame settings
		frame.add(canvas, BorderLayout.CENTER);
		frame.add(south, BorderLayout.SOUTH);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setTitle("Text Recognition");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	/** 
	 * Tries to fix the look and feel of the GUI to match the System look and feel.
	 * If there are any errors (e.g. a certain OS does not support one of the
	 * GUI widgets), gives up and uses the Swing default "metal" look and feel.
	 */
	private void tryToFixLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// give up and just use the (ugly) Swing default look and feel
		}
	}	
	
	/**
	 * A custom panel that supports mouse drag-and-drop handwriting
	 * (similar to the pen tool in GIMP).
	 */
	@SuppressWarnings("serial")
	private class DrawingCanvas extends JPanel {
		
		/** The underlying image which is edited as the user draws on the canvas. */
		private BufferedImage img;
		
		/** The width of this DrawingCanvas. */
		private int width;
		
		/** The height of this DrawingCanvas. */
		private int height;
		
		/**
		 * Constructs a new DrawingCanvas of width <i>width</i> and height <i>height</i>.
		 * 
		 * @param width The width of the DrawingCanvas.
		 * @param height The height of the DrawingCanvas.
		 */
		public DrawingCanvas(int width, int height) {
			// set up image
			this.width = width;
			this.height = height;
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics g = img.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
			
			// add listeners
			MouseInputAdapter mia = new Pen();
			this.addMouseListener(mia);
			this.addMouseMotionListener(mia);
		}
		
		/** 
		 * Returns the dimensions of this DrawingCanvas.
		 * 
		 *  @return The Dimension of this canvas.
		 */
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(this.width, this.height);
		}
		
		/** 
		 * Paints the image on the panel.
		 * 
		 * @param g The graphics object to draw with.
		 */
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(img, 0, 0, null);
		}
		
		/**
		 * Clears this DrawingCanvas.
		 */
		public void clear() {
			Graphics g = img.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
		}
		
		/**
		 * Responds to mouse drag-and-drops by drawing lines on the canvas.
		 */
		private class Pen extends MouseInputAdapter {
			
			/** The start point of the line to draw. */
			private Point start;
			
			/** The end point of the line to draw. */
			private Point end;
			
			/**
			 * Responds to a mouse press by setting the start point of the line
			 * to draw at the location of the mouse press (as indicated by e).
			 * 
			 * @param e The MouseEvent representing the mouse press.
			 */
		    @Override
		    public void mousePressed(MouseEvent e) {
		        start = e.getPoint();
		    }
		    
		    /**
		     * Responds to the drag-and-drop of the mouse by drawing a line from
		     * the press point to the location of the drop (as indicated by e).
		     * 
		     * @param e The MouseEvent representing the mouse drag-and-drop.
		     */
		    @Override
		    public void mouseDragged(MouseEvent e) {
		    	// draw image
		        end = e.getPoint();
		        Graphics2D g2 = (Graphics2D) img.getGraphics();
		        g2.setColor(Color.BLACK);
		        g2.setStroke(new BasicStroke(4));
		        g2.drawLine(start.x, start.y, end.x, end.y);
		        
		        // read image
		        try {
			        // read image and reset label to proper text
		        	File f = new File("src/temp.jpg");
					ImageIO.write(img, "jpg", f);
			        label.setText(r.read(f));
			        frame.repaint();
			        start = end;
				} catch (IOException ioe) {
					// won't happen
				}
		    }
		}
	}
}