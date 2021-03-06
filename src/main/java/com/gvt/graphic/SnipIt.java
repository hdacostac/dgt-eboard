package com.gvt.graphic;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gvt.windows.MainWindows;

public class SnipIt {

	private static Logger logger = LoggerFactory.getLogger(SnipIt.class);

	private Rectangle selectedBounds;

	public SnipIt() {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
				}

				JFrame frame = new JFrame();
				frame.setUndecorated(true);
				// This works differently under Java 6
				frame.setBackground(new Color(0, 0, 0, 0));
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLayout(new BorderLayout());
				frame.add(new SnipItPane());
				frame.setBounds(getVirtualBounds());
				frame.setVisible(true);
			}
		});
	}

	public class SnipItPane extends JPanel {

		private static final long serialVersionUID = 180026618908309432L;

		private Point mouseAnchor;
		private Point dragPoint;

		private SelectionPane selectionPane;

		public SnipItPane() {
			setOpaque(false);
			setLayout(null);

			selectionPane = new SelectionPane();
			add(selectionPane);

			MouseAdapter adapter = new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					mouseAnchor = e.getPoint();
					dragPoint = null;
					selectionPane.setLocation(mouseAnchor);
					selectionPane.setSize(0, 0);
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					dragPoint = e.getPoint();
					int width = dragPoint.x - mouseAnchor.x;
					int height = dragPoint.y - mouseAnchor.y;

					int x = mouseAnchor.x;
					int y = mouseAnchor.y;

					if (width < 0) {
						x = dragPoint.x;
						width *= -1;
					}

					if (height < 0) {
						y = dragPoint.y;
						height *= -1;
					}

					selectionPane.setBounds(x, y, width, height);
					selectionPane.revalidate();

					repaint();
				}
			};

			addMouseListener(adapter);
			addMouseMotionListener(adapter);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D) g.create();

			Rectangle bounds = new Rectangle(0, 0, getWidth(), getHeight());
			Area area = new Area(bounds);
			area.subtract(new Area(selectionPane.getBounds()));

			g2d.setColor(new Color(192, 192, 192, 64));
			g2d.fill(area);
		}
	}

	public class SelectionPane extends JPanel {

		private static final long serialVersionUID = -5198590860288484262L;

		private JButton button;
		private JLabel label;

		public SelectionPane() {
			setOpaque(false);
			setLayout(new GridBagLayout());

			button = new JButton("Close");

			label = new JLabel("Rectangle");
			label.setOpaque(true);
			label.setBorder(new EmptyBorder(4, 4, 4, 4));
			label.setBackground(Color.GRAY);
			label.setForeground(Color.WHITE);

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			add(label, gbc);

			gbc.gridy++;
			add(button, gbc);

			button.addActionListener(e -> {
				SwingUtilities.getWindowAncestor(SelectionPane.this).dispose();

				selectedBounds = new Rectangle(getX(), getY(), getWidth(), getHeight());

//				try {
//					Robot robot = new Robot();
//					BufferedImage screenFullImage = robot.createScreenCapture(selectedBounds);
//				} catch (AWTException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}

				logger.debug("Selected bounds:{}", selectedBounds);

				MainWindows.chessboardRecognition.start();
			});

			addComponentListener(new ComponentAdapter() {

				@Override
				public void componentResized(ComponentEvent e) {
					label.setText("Rectangle " + getX() + "x" + getY() + "x" + getWidth() + "x" + getHeight());
				}
			});

		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D) g.create();
			// I've chosen NOT to fill this selection rectangle, so that
			// it now appears as if you're "cutting" away the selection
			// g2d.setColor(new Color(128, 128, 128, 64));
			// g2d.fillRect(0, 0, getWidth(), getHeight());

			float[] dash1 = { 10.0f };
			BasicStroke dashed = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
			g2d.setColor(Color.BLACK);
			g2d.setStroke(dashed);
			g2d.drawRect(0, 0, getWidth() - 3, getHeight() - 3);
			g2d.dispose();
		}
	}

	private static Rectangle getVirtualBounds() {
		Rectangle bounds = new Rectangle(0, 0, 0, 0);

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] lstGDs = ge.getScreenDevices();
		for (GraphicsDevice gd : lstGDs) {
			bounds.add(gd.getDefaultConfiguration().getBounds());
		}

		return bounds;
	}

	public Rectangle getSelectedBounds() {
		return selectedBounds;
	}
}
