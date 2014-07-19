package dany.hoppixupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;

import com.google.common.base.Charsets;

public class HoppixUpdater
{
	public static final String software_version = "1.8";
	
	private static final HoppixUpdater instance = new HoppixUpdater();
	private String[] args;
	
	private String version_updater_new;
	private String version_forge_old;
	private String version_forge_new;
	private String version_build_old;
	private String version_build_new;
	
	private String token;
	
	public static void main(String[] args)
	{
		try
		{
			instance().args = args;
			instance().version_forge_old = "?";
			instance().version_build_old = "?";
			instance().run();
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
	
	public static HoppixUpdater instance()
	{
		return instance;
	}
	
	public void run() throws Throwable
	{
		System.out.println("Welcome to Hoppix-Updater v" + software_version);
		System.out.println("###################");
		System.out.println("# Author: CatDany #");
		System.out.println("###################");
		System.out.println("");
		
		if (args.length < 1 || args[0] == null)
		{
			System.out.println("You are using wrong run command! Use this:");
			System.out.println("java -jar Hoppix-Updater-" + software_version + ".jar --token=$YOUR_TOKEN$");
			System.in.read();
			System.exit(-1);
		}
		
		System.out.println("Checking access..");
		if (isTokenValid(args[0].substring(8)))
		{
			System.out.println("Access is allowed!");
			System.out.println("");
		}
		else
		{
			System.out.println("Access is denied!");
			System.out.println("You are using wrong or outdated token.");
			System.in.read();
			System.exit(-1);
		}
		
		if (clearOlderVersions())
		{
			System.out.println("Older versions of Hoppix-Updater were deleted.");
		}
		
		System.out.println("Checking Hoppix-Updater for updates..");
		String updaterVersion = getUpdaterVersion();
		if (isUpdaterUpdated())
		{
			System.out.println("Hoppix-Updater is outdated!");
			System.out.println("Latest Hoppix-Updater version: " + updaterVersion);
			System.out.println("Downloading latest version..");
			downloadUpdater(updaterVersion);
			System.out.println("Hoppix-Updater is downloaded successfully. Changing BAT-file..");
			changeBatchFile(updaterVersion, token);
			System.out.println("BAT-file changed!");
			System.out.println("Hoppix-Updater is updated successfully!");
			System.out.println("");
			System.out.println("Close this application and start 'run.bat' again!");
			System.in.read();
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
		
		if (isForgeUpdated())
		{
			System.out.println("Forge is outdated! Downloading required version..");
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
			downloadBuild(buildVersion);
			if (isBuildFileValid() != null)
			{
				System.out.println("Build file is corrupted. Reason: " + isBuildFileValid().getErrorName() + "!");
				System.in.read();
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
			downloadLauncher();
			System.out.println("Launcher is downloaded successfully!");
		}
		
		if (isBuildUpdated())
		{
			System.out.println("Changes in build " + version_build_new + ":");
			for (String i : getLatestChanges())
			{
				System.out.println(i);
			}
			System.out.println("Press Enter to start Minecraft.");
			System.in.read();
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
			configBackupDir.delete();
		if (modsBackupDir.exists())
			modsBackupDir.delete();
		
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
		FileOutputStream fos = new FileOutputStream("Hoppix-Updater-" + version + ".jar");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
	}
	
	public void changeBatchFile(String version, String token) throws Throwable
	{
		File batch = new File("run.bat");
		if (batch.exists())
		{
			batch.delete();
		}
		batch.createNewFile();
		PrintWriter bw = new PrintWriter(batch);
		bw.println("@echo off");
		// bw.println("java -jar Hoppix-Updater-" + version + ".jar");
		bw.println("java -jar Hoppix-Updater-" + version + ".jar --token=" + token);
		bw.println("exit");
		bw.close();
	}
	
	public boolean clearOlderVersions() throws Throwable
	{
		File[] files = new File(".").listFiles();
		boolean clearing = false;
		for (File i : files)
		{
			if (i.exists() && i.getName().startsWith("Hoppix-Updater-") && i.getName().endsWith(".jar") && !i.getName().equals("Hoppix-Updater-" + software_version + ".jar") && !i.getName().endsWith("BIN.jar"))
			{
				i.delete();
				clearing = true;
			}
		}
		return clearing;
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