package net.mms_projects.copyit.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import net.mms_projects.copyit.FileStreamBuilder;
import net.mms_projects.copyit.PathBuilder;
import net.mms_projects.copyit.Settings;
import net.mms_projects.copyit.ui.AbstractUi;
import net.mms_projects.copyit.ui.SwtGui;

public class CopyItDesktop extends CopyIt {

	protected Settings settings;
	protected File lockFile;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CopyItDesktop app = new CopyItDesktop();
		app.run();
	}

	public static String getVersion() {
		String version = "";
		if (CopyItDesktop.class.getPackage().getSpecificationVersion() != null) {
			version += CopyItDesktop.class.getPackage()
					.getSpecificationVersion();
		} else {
			version += "0.0.1";
		}

		if (CopyItDesktop.getBuildNumber() != 0) {
			version += "-" + CopyItDesktop.getBuildNumber();
		}
		return version;
	}

	public static int getBuildNumber() {
		try {
			return Integer.parseInt(CopyItDesktop.class.getPackage()
					.getImplementationVersion());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public void run() {
		this.settings = new Settings();
		try {
			this.settings.setFileStreamBuilder(new StreamBuilder());
		} catch (IOException e) {
			e.printStackTrace();

			System.exit(1);
		}
		this.settings.loadProperties();
		this.lockFile = new File(PathBuilder.getConfigDirectory(), ".lock");
		if (this.lockFile.exists()) {
			System.out.println("An instance is already running. "
					+ "If not please remove the following lock file: "
					+ this.lockFile.getAbsolutePath());
			System.exit(0);
		} else {
			try {
				this.lockFile.createNewFile();

				this.lockFile.deleteOnExit();
			} catch (IOException e) {
				e.printStackTrace();

				System.exit(1);
			}
		}
		;

		AbstractUi ui = new SwtGui(this.settings);
		ui.open();

		this.settings.saveProperties();
	}

	class StreamBuilder extends FileStreamBuilder {

		private File settingsFile;

		public StreamBuilder() throws IOException {
			this.settingsFile = new File(PathBuilder.getConfigDirectory(),
					"options.properties");
			if (!this.settingsFile.exists()) {
				System.out.println("No settings file. Creating it.");
				this.settingsFile.createNewFile();
			}
		}

		@Override
		public FileInputStream getInputStream() throws IOException {
			return new FileInputStream(this.settingsFile);
		}

		@Override
		public FileOutputStream getOutputStream() throws IOException {
			return new FileOutputStream(this.settingsFile);
		}

	}

}
