package dany.hoppixupdater;

public class Variables
{
	public static State state = State.NULL;
	
	public static enum State
	{
		NULL("Loading..."),
		CHECKING_ACCESS("Checking access..."),
		SETTING_UP("Setting up..."),
		DOWNLOADING_UPDATER("Downloading updater..."),
		DOWNLOADING_FORGE("Downloading Forge..."),
		DOWNLOADING_MODPACK("Downloading modpack..."),
		DOWNLOADING_LAUNCHER("Downloading launcher..."),
		FINALLIZING("Finalizing...");
		
		public final String name;
		
		private State(String name)
		{
			this.name = name;
		}
	}
}