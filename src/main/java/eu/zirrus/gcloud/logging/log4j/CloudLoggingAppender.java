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

@Plugin(name="CloudLogging", category="Core", elementType="appender", printObject=true)
public class CloudLoggingAppender extends AbstractAppender {
	
	protected String logName = "default";
	protected MonitoredResource resource;
	protected boolean ignoreExceptions = false;
	protected Logging logging;

	public CloudLoggingAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
		this(name, filter, layout, false, null, null);
	}
	public CloudLoggingAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
		this(name, filter, layout, ignoreExceptions, null, null);
	}

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
