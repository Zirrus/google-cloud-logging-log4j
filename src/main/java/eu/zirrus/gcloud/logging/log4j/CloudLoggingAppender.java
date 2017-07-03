package eu.zirrus.gcloud.logging.log4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.MonitoredResource;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Payload.StringPayload;
import com.google.cloud.logging.Severity;

/**
 * Google Cloud Logging Appender for log4j2
 * 
 * @author Paul Woelfel <paul.woelfel@zirrus.eu>
 * 
 * This Appender can be used to send all log messages to Google Cloud Logging (https://cloud.google.com/logging/docs/). 
 * If not specified, it uses the default project id (set by glcoud config set) and the application default credentials.
 *
 */
@Plugin(name="CloudLogging", category="Core", elementType="appender", printObject=true)
public class CloudLoggingAppender extends AbstractAppender {
	
	protected String logName = "default";
	protected MonitoredResource resource;
	protected boolean ignoreExceptions = false;
	protected Logging logging;

	/**
	 * Create CloudLoggingAppender
	 * @param name appender name, also used for log file name in Cloud Logging
	 * @param filter log4j filter
	 * @param layout log4j layout
	 */
	public CloudLoggingAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
		this(name, filter, layout, true, null, null);
	}

	/**
	 * Create CloudLoggingAppender
	 * @param name appender name, also used for log file name in Cloud Logging
	 * @param filter log4j filter
	 * @param layout log4j layout
	 * @param ignoreExceptions ignore exceptions happening inside this appender
	 */
	public CloudLoggingAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
		this(name, filter, layout, ignoreExceptions, null, null);
	}

	/**
	 * Create CloudLoggingAppender
	 * @param name appender name, also used for log file name in Cloud Logging
	 * @param filter log4j filter
	 * @param layout log4j layout
	 * @param ignoreExceptions ignore exceptions happening inside this appender
	 * @param projectId Google Cloud Project ID
	 * @param credentialsFile JSON file with service account
	 */
	public CloudLoggingAppender(String name, Filter filter, Layout<? extends Serializable> layout,
			boolean ignoreExceptions, String projectId, String credentialsFile) {
		super(name, filter, layout, ignoreExceptions);
		this.logName = name;
		this.ignoreExceptions = ignoreExceptions;
		// Instantiates a client
		LoggingOptions loggingOptions;
		com.google.cloud.logging.LoggingOptions.Builder builder = LoggingOptions.newBuilder();
		if (projectId != null){
			builder.setProjectId(projectId);
		}
		if (credentialsFile != null){
			try {
				Credentials credentials;
				credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFile));
				builder.setCredentials(credentials);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		loggingOptions = LoggingOptions.getDefaultInstance();
		loggingOptions = builder.build();
		
		this.logging = loggingOptions.getService();
		this.resource = MonitoredResource.newBuilder("global").build();
	}

	/* (non-Javadoc)
	 * @see org.apache.logging.log4j.core.Appender#append(org.apache.logging.log4j.core.LogEvent)
	 */
	public void append(LogEvent event) {
	    String text = event.getMessage().getFormattedMessage();

	    LogEntry entry = LogEntry.newBuilder(StringPayload.of(text))
	        .setLogName(logName)
	        .setResource(this.resource)
	        .setSeverity(getCloudLoggingSeverity(event.getLevel()))
	        .build();

	    // Writes the log entry
	    logging.write(Collections.singleton(entry));

//	    System.out.printf("Logged: %s%n", text);
	}
	
	/**
	 * map log4j level to Google Cloud Logging Severity
	 * @param level log4j log level
	 * @return Google Cloud Severity
	 */
	public static Severity getCloudLoggingSeverity(Level level){
		switch(level.intLevel()){
			case 100:
				return Severity.CRITICAL;
			case 200:
				return Severity.ERROR;
			case 300:
				return Severity.WARNING;
			case 400:
				return Severity.INFO;
			case 500:
			case 600:
				return Severity.DEBUG;
		}
		return Severity.INFO;
	}
	
    /**
     * Factory for log4j configuration
     * @param name appender name, also used for logName
     * @param layout log4j layout
     * @param filter log4j filter
     * @param projectId optional project id
     * @param credentialsFile optional JSON service account file
     * @return CloudLoggingAppender instance
     */
    @PluginFactory
    public static CloudLoggingAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("projectId") String projectId,
            @PluginAttribute("credentialsFile") String credentialsFile) {
        if (name == null) {
            name = "default";
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new CloudLoggingAppender(name, filter, layout, true, projectId, credentialsFile);
    }

}
