package net.mms_projects.copy_it.run_configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.mms_projects.copy_it.app.CopyItDesktop;

public class AllInOneJar {

	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger(AllInOneJar.class);

		log.info("Running from a all in one jar...");

		CopyItDesktop.main(args);
	}

}
