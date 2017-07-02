package eu.zirrus.gcloud.logging.log4j;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;

import junit.framework.TestCase;

public class CloudLoggingAppenderTest extends TestCase {

	public void testInfo() {

		PluginManager.addPackage("eu.zirrus.gcloud.logging.log4j");
		LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
		context.setConfigLocation(new File("log4j2.xml").toURI());

		Logger logger = LogManager.getLogger(CloudLoggingAppenderTest.class);
		logger.debug("debug test");
		logger.info("info test");
		logger.warn("warn test");
		logger.error("error test");
		logger.fatal("fatal test");
	}

}
