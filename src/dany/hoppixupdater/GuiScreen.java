package dany.hoppixupdater;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;

import dany.hoppixupdater.Variables.State;

public class GuiScreen extends JPanel implements Runnable, KeyListener
{
	public final Thread thread = new Thread(this, "AWT-Thread");
	
	public final GuiFrame frame;
	
	public GuiScreen(GuiFrame frame)
	{
		super();
		this.frame = frame;
		
		thread.setDaemon(true);
		thread.start();
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		g.clearRect(0, 0, getWidth(), getHeight());
		drawCenteredString(g, Variables.state.name);
	}
	
	@Override
	public void run()
	{
		while (true)
		{
			repaint();
		}
	}
	
	public void drawCenteredString(Graphics g, String str, int x, int y)
	{
		FontMetrics font = g.getFontMetrics();
		int strX = x - font.stringWidth(str) / 2;
		int strY = y - font.getHeight() / 2;
		g.drawString(str, strX, strY);
	}
	
	public void drawCenteredString(Graphics g, String str)
	{
		drawCenteredString(g, str, getWidth() / 2, getHeight() / 2);
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == e.VK_ENTER)
		{
			//
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}
	
	@Override
	public void keyReleased(KeyEvent e) {}
}