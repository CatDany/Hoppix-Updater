package dany.hoppixupdater;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import net.lingala.zip4j.core.ZipFile;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Charsets;

import dany.hoppixupdater.Variables.State;

public class HoppixUpdater implements Runnable
{
	public static final String software_version = "2.4";
	public static final double JAVA_VERSION_REQUIRED = 1.8;
	public static final int JAVA_BUILD_REQUIRED = 40;
	
	private static final HoppixUpdater instance = new HoppixUpdater();
	private String[] args;
	
	private String version_updater_new;
	private String version_forge_old;
	private String version_forge_new;
	private String version_build_old;
	private String version_build_new;
	
	private String token;
	
	private GuiFrame frame;
	
	public static void main(String[] args)
	{
		instance().args = args;
		instance().version_forge_old = "?";
		instance().version_build_old = "?";
		new Thread(instance()).start();
	}
	
	public static HoppixUpdater instance()
	{
		return instance;
	}
	
	@Override
	public void run()
	{
		try
		{
			runProgram();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			try
			{
				File forgeversion = new File("forgeversion.txt");
				if (forgeversion.exists())
					forgeversion.delete();
				File buildversion = new File("buildversion.txt");
				if (buildversion.exists())
					buildversion.delete();
				File updaterversion = new File("updaterversion.txt");
				if (buildversion.exists())
					buildversion.delete();
				File tokenvalid = new File("token.tmp");
				if (buildversion.exists())
					buildversion.delete();
			}
			catch (Throwable tt) {}
		}
		System.exit(-1);
	}
	
	public void runProgram() throws Throwable
	{
		this.frame = new GuiFrame();
		
		System.out.println("Welcome to Hoppix-Updater v" + software_version);
		System.out.println("###################");
		System.out.println("# Author: CatDany #");
		System.out.println("###################");
		System.out.println("");
		
		if (!verifyJavaVersion())
		{
			JOptionPane.showMessageDialog(new OPFrame(), "Your Java version is unsupported.\nUpdate to the latest Java 8 64-bit.", "Unsupported JRE", JOptionPane.ERROR_MESSAGE);
			Desktop.getDesktop().browse(new URI("https://www.java.com/ru/download/manual.jsp"));
			System.exit(-1);
		}
		
		if ((token = readToken()) == null)
		{
			token = JOptionPane.showInputDialog(new OPFrame(), "Enter your token here", null);
			writeToken(token);
		}
		
		System.out.println("Checking access..");
		Variables.state = State.CHECKING_ACCESS;
		if (isTokenValid(token))
		{
			System.out.println("Access is allowed!");
			System.out.println("");
		}
		else
		{
			System.out.println("Access is denied!");
			System.out.println("You are using wrong or outdated token.");
			writeToken(null);
			JOptionPane.showMessageDialog(new OPFrame(), "You are using wrong or outdated token!", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		
		Variables.state = State.SETTING_UP;
		
		System.out.println("Checking Hoppix-Updater for updates..");
		String updaterVersion = getUpdaterVersion();
		if (isUpdaterUpdated())
		{
			System.out.println("Hoppix-Updater is outdated!");
			System.out.println("Latest Hoppix-Updater version: " + updaterVersion);
			System.out.println("Downloading latest version..");
			Variables.state = State.DOWNLOADING_UPDATER;
			downloadUpdater(updaterVersion);
			System.out.println("Hoppix-Updater is updated successfully!");
			System.out.println("");
			System.out.println("Close this application and start it again!");
			JOptionPane.showMessageDialog(new OPFrame(), "Restart Hoppix-Updater to continue.", "Hoppix-Updater self-updated", JOptionPane.INFORMATION_MESSAGE);
			System.exit(-1);
		}
		else
		{
			System.out.println("No updates found of Hoppix-Updater found.");
			System.out.println("");
		}
		
		clearForgeVersionFile();
		String forgeVersion = getForgeVersion();
		System.out.println("Required forge version: " + forgeVersion);
		
		clearBuildVersionFile();
		String buildVersion = getBuildVersion();
		System.out.println("Required modpack build version: " + buildVersion);
		System.out.println("");
		
		String mcversion = forgeVersion.split("-")[0];
		if (!isVanillaVersionExisting(mcversion))
		{
			JOptionPane.showMessageDialog(new OPFrame(), "Run vanilla " + mcversion + " at least once before installing Hoppix modpack!", "Warning", JOptionPane.WARNING_MESSAGE);
			System.exit(-1);
		}
		
		if (isForgeUpdated())
		{
			System.out.println("Forge is outdated! Downloading required version..");
			Variables.state = State.DOWNLOADING_FORGE;
			downloadForge(forgeVersion);
			System.out.println("Forge is successfully downloaded.");
			System.out.println("");
			System.out.println("Click 'OK' in a small window that'll appear right now.");
			System.out.println("Wait for it to download all the libraries and click 'OK' again.");
			System.out.println("Do not change Minecraft path! It must be '.minecraft'");
			runForgeInstaller();
			System.out.println("Forge is installed!");
			System.out.println("");
		}
		
		if (isBuildUpdated())
		{
			System.out.println("Modpack is outdated! Downloading required version..");
			Variables.state = State.DOWNLOADING_MODPACK;
			downloadBuild(buildVersion);
			if (isBuildFileValid() != null)
			{
				System.out.println("Build file is corrupted. Reason: " + isBuildFileValid().getErrorName() + "!");
				JOptionPane.showMessageDialog(new OPFrame(), "Build file is corrupted. Reason: " + isBuildFileValid().getErrorName() + "!", "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
			System.out.println("Modpack is downloaded successfully. Unzipping..");
			unpackBuild();
			System.out.print("Modpack is installed!");
			System.out.println("");
		}
		
		System.out.println("Clearing temprorary files..");
		clearTempFiles();
		System.out.println("Temprorary files are deleted!");
		System.out.println("");
		
		if (!isLauncherExisting())
		{
			System.out.println("Downloading launcher..");
			Variables.state = State.DOWNLOADING_LAUNCHER;
			downloadLauncher();
			System.out.println("Launcher is downloaded successfully!");
		}
		
		if (isBuildUpdated())
		{
			System.out.println("Changes in build " + version_build_new + ":");
			String changesInOneLine = "";
			for (String i : getLatestChanges())
			{
				System.out.println(i);
				changesInOneLine += i + "\n";
			}
			JOptionPane.showMessageDialog(new OPFrame(), "Hoppix Modpack updated. Latest changes:\n" + changesInOneLine, "Done", JOptionPane.INFORMATION_MESSAGE);
			System.out.println("Starting Minecraft..");
		}
		else
		{
			System.out.println("No updates found, starting Minecraft..");
		}
		
		Runtime.getRuntime().exec("Minecraft.exe");
		System.exit(0);
	}
	
	public void clearForgeVersionFile() throws Throwable
	{
		File forgeVerFile = new File("forgeversion.txt");
		if (forgeVerFile.exists())
		{
			version_forge_old = new String(Files.readAllBytes(Paths.get(new File("forgeversion.txt").toURI())));
		}
		forgeVerFile.delete();
		forgeVerFile.createNewFile();
	}
	
	public String getForgeVersion() throws Throwable
	{
		URL website = new URL("http://files.hoppix.ru/info/forgeversion.txt");
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream("forgeversion.txt");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		String forgeVersion = new String(Files.readAllBytes(Paths.get(new File("forgeversion.txt").toURI())));
		version_forge_new = forgeVersion;
		return forgeVersion;
	}
	
	public void clearBuildVersionFile() throws Throwable
	{
		File buildVerFile = new File("buildversion.txt");
		if (buildVerFile.exists())
		{
			version_build_old = new String(Files.readAllBytes(Paths.get(new File("buildversion.txt").toURI())));
		}
		buildVerFile.delete();
		buildVerFile.createNewFile();
	}
	
	public String getBuildVersion() throws Throwable
	{
		URL website = new URL("http://files.hoppix.ru/info/buildversion.txt");
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream("buildversion.txt");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		String buildVersion = new String(Files.readAllBytes(Paths.get(new File("buildversion.txt").toURI())));
		version_build_new = buildVersion;
		return buildVersion;
	}
	
	public boolean isForgeUpdated() throws Throwable
	{
		return !version_forge_new.equals(version_forge_old);
	}
	
	public boolean isBuildUpdated() throws Throwable
	{
		return !version_build_new.equals(version_build_old);
	}
	
	public void downloadForge(String version) throws Throwable
	{
		/*
		String mcVersion = version.split("-")[0];
		String forgeVersion = version.split("-")[1];
		String major = forgeVersion.split(".")[0];
		String minor = forgeVersion.split(".")[1];
		String revision = forgeVersion.split(".")[2];
		String build = forgeVersion.split(".")[3];
		*/
		
		// http://files.minecraftforge.net/maven/net/minecraftforge/forge/1.7.2-10.12.2.1126/forge-1.7.2-10.12.2.1126-installer.jar
		// http://files.minecraftforge.net/maven/net/minecraftforge/forge/1.7.10-10.13.3.1384-1.7.10/forge-1.7.10-10.13.3.1384-1.7.10-installer.jar
		String forgeurl = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/"
				+ version + "/forge-" + version + "-installer.jar";
		URL website = new URL(forgeurl);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream("forge-installer.jar");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}
	
	public void runForgeInstaller() throws Throwable
	{
		Process pr = Runtime.getRuntime().exec("java -jar forge-installer.jar");
		pr.waitFor();
	}
	
	public void downloadBuild(String version) throws Throwable
	{
		// http://files.hoppix.ru/build/build_17.zip
		// http://files.hoppix.ru/access/download.php?build=1&token=%TOKEN%
		String buildurl = "http://files.hoppix.ru/access/download.php"
				+ "?build=" + version
				+ "&token=" + token;
		URL website = new URL(buildurl);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream("build.hoppix");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}
	
	public void unpackBuild() throws Throwable
	{
		File configBackupDir = new File("config_backup");
		File modsBackupDir = new File("mods_backup");
		
		if (configBackupDir.exists())
			FileUtils.deleteDirectory(configBackupDir);
		if (modsBackupDir.exists())
			FileUtils.deleteDirectory(modsBackupDir);
		
		File configDir = new File("config");
		File modsDir = new File("mods");
		
		if (configDir.exists())
			configDir.renameTo(new File("config_backup"));
		if (modsDir.exists())
			modsDir.renameTo(new File("mods_backup"));
		
		ZipFile build = new ZipFile(new File("build.hoppix"));
		build.extractAll(".");
		File readme = new File("readme.txt");
		if (readme.exists())
		{
			readme.delete();
		}
	}
	
	public boolean isLauncherExisting() throws Throwable
	{
		File launcher = new File("Minecraft.exe");
		return launcher.exists();
	}
	
	public void downloadLauncher() throws Throwable
	{
		// https://s3.amazonaws.com/Minecraft.Download/launcher/Minecraft.exe
		String mcurl = "https://s3.amazonaws.com/Minecraft.Download/launcher/Minecraft.exe";
		URL website = new URL(mcurl);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream("Minecraft.exe");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}
	
	public void clearTempFiles() throws Throwable
	{
		File forgeInstaller = new File("forge-installer.jar");
		if (forgeInstaller.exists())
			forgeInstaller.delete();
		File buildZip = new File("build.zip");
		if (buildZip.exists())
			buildZip.delete();
	}
	
	public String getUpdaterVersion() throws Throwable
	{
		URL website = new URL("http://files.hoppix.ru/info/updaterversion.txt");
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream("updaterversion.txt");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		String updaterVersion = new String(Files.readAllBytes(Paths.get(new File("updaterversion.txt").toURI())));
		new File("updaterversion.txt").delete();
		version_updater_new = updaterVersion;
		return updaterVersion;
	}
	
	public boolean isUpdaterUpdated() throws Throwable
	{
		return !version_updater_new.equals(software_version);
	}
	
	public void downloadUpdater(String version) throws Throwable
	{
		// http://files.hoppix.ru/updater/Hoppix-Updater-1.1.jar
		String mcurl = "http://files.hoppix.ru/updater/Hoppix-Updater-" + version + ".jar";
		URL website = new URL(mcurl);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream("Hoppix-Updater.jar");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}
	
	public List<String> getLatestChanges() throws Throwable
	{
		List<String> allLines = Files.readAllLines(Paths.get(new File("changelog.txt").toURI()), StandardCharsets.UTF_8);
		List<String> changes = new ArrayList<String>();
		for (String i : allLines)
		{
			if (i.equals(""))
				break;
			if (i.startsWith("-"))
			{
				int colonIndex = i.indexOf(":");
				String str1 = i.substring(0, colonIndex);
				int hashIndex = i.indexOf("#");
				if (hashIndex == -1)
				{
					changes.add(str1);
				}
				else
				{
					String str2 = i.substring(hashIndex);
					changes.add(str1 + " " + str2);
				}
			}
		}
		return changes;
	}
	
	public boolean isTokenValid(String token) throws Throwable
	{
		URL website = new URL("http://files.hoppix.ru/access/checkAccess.php?token=" + token);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream("token.tmp");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		boolean isTokenValid = Boolean.parseBoolean(new String(Files.readAllBytes(Paths.get(new File("token.tmp").toURI()))));
		new File("token.tmp").delete();
		this.token = token;
		return isTokenValid;
	}
	
	public String readToken() throws Throwable
	{
		File file = new File("token.txt");
		if (file.exists())
		{
			String t = Files.readAllLines(file.toPath(), Charsets.ISO_8859_1).get(0);
			if (t.matches("^[a-zA-Z0-9]{16}$") && t.length() == 16)
			{
				return t;
			}
		}
		return null;
	}
	
	public void writeToken(String token) throws Throwable
	{
		File file = new File("token.txt");
		if (!file.exists())
		{
			file.createNewFile();
		}
		if (token == null)
		{
			file.delete();
			return;
		}
		PrintWriter bw = new PrintWriter(file);
		bw.println(token);
		bw.close();
	}
	
	public boolean isVanillaVersionExisting(String mcversion) throws Throwable
	{
		if (!System.getProperty("os.name").startsWith("Windows"))
		{
			return true;
		}
		File file = new File(System.getenv("APPDATA") + "\\.minecraft\\versions\\" + mcversion + "\\" + mcversion + ".jar");
		return file.exists();
	}
	
	public boolean verifyJavaVersion()
	{
		if (!System.getProperty("os.name").startsWith("Windows"))
		{
			return true;
		}
		String version = System.getProperty("java.version");
		double ver = Double.parseDouble(version.substring(0, 3));
		int build = Integer.parseInt(version.substring(6));
		return ver == JAVA_VERSION_REQUIRED && build >= JAVA_BUILD_REQUIRED;
	}
	
	/**
	 * @return null if build is valid
	 * @throws Throwable
	 */
	public EnumRemoteScriptErrorType isBuildFileValid() throws Throwable
	{
		List<String> lines = Files.readAllLines(Paths.get(new File("build.hoppix").toURI()), Charsets.ISO_8859_1);
		String line = lines.get(0);
		if (line.startsWith("error"))
		{
			int indexOfSeparator = line.indexOf("|");
			String errorMsg = line.substring(0, indexOfSeparator).trim();
			switch (errorMsg)
			{
			case "error.FileNotFound":
				return EnumRemoteScriptErrorType.FILE_NOT_FOUND;
			case "error.AccessDenied":
				return EnumRemoteScriptErrorType.ACCESS_DENIED;
			default:
				return EnumRemoteScriptErrorType.UNKNOWN_ERROR;
			}
		}
		return null;
	}
	
	static enum EnumRemoteScriptErrorType
	{
		FILE_NOT_FOUND("Remote File Not Found"),
		ACCESS_DENIED("Access Denied"),
		UNKNOWN_ERROR("Unknown Error");
		
		private String errorName;
		
		private EnumRemoteScriptErrorType(String errorName)
		{
			this.errorName = errorName;
		}
		
		public String getErrorName()
		{
			return errorName;
		}
	}
}