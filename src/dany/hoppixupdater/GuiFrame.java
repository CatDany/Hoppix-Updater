package dany.hoppixupdater;

import javax.swing.JFrame;

public class GuiFrame extends JFrame
{
	public GuiFrame()
	{
		super();
		
		setTitle("Hoppix-Updater");
		setSize(300, 140);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setVisible(true);
		
		GuiScreen panel = new GuiScreen(this);
		add(panel);
		addKeyListener(panel);
	}
}