package com.oddlabs.tt.bugclient;

import com.oddlabs.tt.util.Utils;

import java.util.ResourceBundle;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;

public final strictfp class BugClientWindow {
	private final static ResourceBundle bundle = ResourceBundle.getBundle(BugClientWindow.class.getName());

	public static void showReporter(final URL url, final int revision, final File log_dir) {
		final boolean[] done = new boolean[1];
		SwingUtilities.invokeLater(new Runnable() {
			public final void run() {
				showReporter(new Runnable() {
					public final void run() {
						synchronized (done) {
							done[0] = true;
							done.notify();
						}
					}
				}, url, revision, log_dir);
			}
		});
		synchronized (done) {
			while (!done[0]) {
				try {
					done.wait();
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
	}

	private static void showReporter(final Runnable done_action, final URL url, final int revision, final File log_dir) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			System.out.println("Unable to load native look and feel: " + ex);
		}
		final JFrame frame = new JFrame(Utils.getBundleString(bundle, "submit_caption"));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public final void windowClosed(WindowEvent e) {
				done_action.run();
			}
		});
		final Object cancel_action = new Object();
		Action close_action = new AbstractAction() {
			{
				String cancel_text = Utils.getBundleString(ResourceBundle.getBundle(com.oddlabs.tt.gui.CancelButton.class.getName()), "cancel");
				putValue(Action.NAME, cancel_text);
			}
			public final void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				frame.dispose();
			}
		};
		frame.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), cancel_action);
		frame.getRootPane().getActionMap().put(cancel_action, close_action);
		frame.setResizable(false);

		// Descriptions
		JTextArea description = createReadOnlyText(Utils.getBundleString(bundle, "describe") + "\n" + Utils.getBundleString(bundle, "note"));

		// Email field
		JLabel email_label = new JLabel(Utils.getBundleString(bundle, "email"));
		final JFormattedTextField email_text = new JFormattedTextField(new RegexFormatter(com.oddlabs.util.Utils.EMAIL_PATTERN));
		Box email_box = new Box(BoxLayout.X_AXIS);
		email_box.add(email_label);
		email_box.add(Box.createHorizontalStrut(5));
		email_box.add(email_text);

		// Comment box
		final JTextArea comment_area = new JTextArea();
		JScrollPane comment_scroll = new JScrollPane(comment_area);
		comment_area.setRows(20);
		comment_area.setLineWrap(true);
		comment_area.setWrapStyleWord(true);

		// OK, Cancel buttons
		JButton ok_button = new JButton();
		Action ok_action = new AbstractAction() {
			{
				String ok_text = Utils.getBundleString(ResourceBundle.getBundle(com.oddlabs.tt.gui.OKButton.class.getName()), "ok");
				putValue(Action.NAME, ok_text);
			}
			public final void actionPerformed(ActionEvent e) {
				setEnabled(false);
				Object email_value = email_text.getValue();
				uploadBugReport(frame, new Runnable() {
						public final void run() {
							frame.setVisible(false);
							frame.dispose();
							done_action.run();
						}
					}, new Runnable() {
						public final void run() {
							setEnabled(true);
						}
					}, url, revision, email_value != null ? email_value.toString() : "", comment_area.getText(), log_dir);
			}
			public final void setEnabled(boolean enable) {
				super.setEnabled(enable);
				comment_area.setEnabled(enable);
				email_text.setEnabled(enable);
			}
		};
		ok_button.setAction(ok_action);
		JButton cancel_button = new JButton();
		cancel_button.setAction(close_action);
		Box button_box = new Box(BoxLayout.X_AXIS);
		button_box.add(Box.createGlue());
		button_box.add(ok_button);
		button_box.add(cancel_button);

		// Layout setup
		JPanel p = new JPanel(new SpringLayout());
		p.add(description);
		p.add(email_box);
		p.add(comment_scroll);
		p.add(button_box);

		makeCompactGrid(p, 4, 1, 6, 6, 6, 6);

		frame.setContentPane(p);
		frame.getRootPane().setDefaultButton(ok_button);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private static SpringLayout.Constraints getConstraintsForCell(
			int row, int col,
			Container parent,
			int cols) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component c = parent.getComponent(row * cols + col);
		return layout.getConstraints(c);
			}

	public static void makeCompactGrid(Container parent,
			int rows, int cols,
			int initialX, int initialY,
			int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout)parent.getLayout();
		} catch (ClassCastException exc) {
			System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
			return;
		}

		//Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width,
						getConstraintsForCell(r, c, parent, cols).
						getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints =
					getConstraintsForCell(r, c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		//Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height,
						getConstraintsForCell(r, c, parent, cols).
						getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints =
					getConstraintsForCell(r, c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		//Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}


	private static void place(SpringLayout layout, Container parent, JComponent component, JComponent upper) {
		layout.putConstraint(SpringLayout.WEST, component, 5, SpringLayout.WEST, parent);
		layout.putConstraint(SpringLayout.EAST, component, -5, SpringLayout.EAST, parent);
		layout.putConstraint(SpringLayout.NORTH, component, 5, SpringLayout.SOUTH, upper);
	}

	private static JTextArea createReadOnlyText(String txt) {
		JTextArea description = new JTextArea(txt);
		description.setColumns(30);
		description.setOpaque(false);
		description.setEnabled(false);
		description.setEditable(false);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setBackground(UIManager.getColor("Label.background"));
		description.setFont(javax.swing.UIManager.getFont("Label.font"));
		description.setDisabledTextColor(UIManager.getColor("Label.foreground"));
		return description;
	}

	private static void uploadBugReport(final JFrame owner, final Runnable done_action, final Runnable failed_action, final URL url, final int revision, final String email, final String comment, final File log_dir) {
		String title = Utils.getBundleString(ResourceBundle.getBundle(BugClientWindow.class.getName()), "report_caption");
		final ProgressDialog progress_dialog = createProgressMonitor(owner, title);
/*		final int NUM_PROGRESS = 100;
		final ProgressMonitor monitor = new ProgressMonitor(owner, title, "", 0, NUM_PROGRESS);
		monitor.setProgress(0);*/
//		monitor.setMillisToDecideToPopup(0);
		final boolean[] cancel_flag = new boolean[1];
		final SwingWorker worker = new SwingWorker() {
			public final Object construct() {
				try {
					Uploader.upload(url, revision, email, comment, log_dir, new Uploader.Progress() {
						public final void setProgress(float p) {
/*							final int int_progress = (int)(p*NUM_PROGRESS);
							SwingUtilities.invokeLater(new Runnable() {
								public final void run() {
									monitor.setProgress(int_progress);
								}
							});*/
						}
					});
					return null;
				} catch (IOException e) {
					return e;
				}
			}
			public final void finished() {
				super.finished();
				if (cancel_flag[0])
					return;
//				monitor.close();
				progress_dialog.close();
				if (getValue() != null) {
					IOException ioe = ((IOException)getValue());
					System.out.println("Upload failed: " + ioe);
					String reason = ioe.toString();
					JOptionPane.showMessageDialog(owner, Utils.getBundleString(bundle, "connection_failed") + "\n" + reason);
					failed_action.run();
				} else {
					JOptionPane.showMessageDialog(owner, Utils.getBundleString(bundle, "success"));
					done_action.run();
				}
			}
		};
		progress_dialog.setCancelAction(new Runnable() {
			public final void run() {
				worker.interrupt();
				cancel_flag[0] = true;
				failed_action.run();
			}
		});
		worker.start();
		progress_dialog.show();
	}

	private static ProgressDialog createProgressMonitor(JFrame owner, String title) {
		final JDialog upload_dialog = new JDialog(owner, title, true);
		final Runnable[] cancel_action = new Runnable[1];
		final Runnable composed_cancel_action = new Runnable() {
			public final void run() {
				upload_dialog.setVisible(false);
				upload_dialog.dispose();
				cancel_action[0].run();
			}
		};
		Action close_action = new AbstractAction() {
			{
				String cancel_text = Utils.getBundleString(ResourceBundle.getBundle(com.oddlabs.tt.gui.CancelButton.class.getName()), "cancel");
				putValue(Action.NAME, cancel_text);
			}
			public final void actionPerformed(ActionEvent e) {
				upload_dialog.setVisible(false);
				upload_dialog.dispose();
			}
		};
		upload_dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		upload_dialog.addWindowListener(new WindowAdapter() {
			public final void windowClosed(WindowEvent e) {
				cancel_action[0].run();
			}
		});
		JButton cancel_button = new JButton();
		cancel_button.setAction(close_action);
		JProgressBar progress_bar = new JProgressBar();
		progress_bar.setIndeterminate(true);
		Container content_pane = upload_dialog.getContentPane();
		content_pane.add(cancel_button);
		content_pane.add(progress_bar);
		SpringLayout layout = new SpringLayout();
		content_pane.setLayout(layout);
		layout.putConstraint(SpringLayout.NORTH, progress_bar, 5, SpringLayout.NORTH, content_pane);
		layout.putConstraint(SpringLayout.WEST, progress_bar, 5, SpringLayout.WEST, content_pane);
		layout.putConstraint(SpringLayout.EAST, content_pane, 5, SpringLayout.EAST, progress_bar);

		layout.putConstraint(SpringLayout.EAST, cancel_button, -5, SpringLayout.EAST, content_pane);
		layout.putConstraint(SpringLayout.NORTH, cancel_button, 5, SpringLayout.SOUTH, progress_bar);
		layout.putConstraint(SpringLayout.SOUTH, content_pane, 5, SpringLayout.SOUTH, cancel_button);
		upload_dialog.pack();
		Dimension owner_size = owner.getSize();
		Dimension dialog_size = upload_dialog.getSize();
		upload_dialog.setLocation(new Point((owner_size.width - dialog_size.width)/2, (owner_size.height - dialog_size.height)/2));
		upload_dialog.setLocationRelativeTo(owner);
//		upload_dialog.setVisible(true);
		return new ProgressDialog() {
			public final void close() {
				composed_cancel_action.run();
			}
			public final void show() {
				upload_dialog.setVisible(true);
			}
			public final void setCancelAction(Runnable runnable) {
				cancel_action[0] = runnable;
			}
		};
	}

	private static strictfp interface ProgressDialog {
		void close();
		void show();
		void setCancelAction(Runnable runnable);
	}
}
