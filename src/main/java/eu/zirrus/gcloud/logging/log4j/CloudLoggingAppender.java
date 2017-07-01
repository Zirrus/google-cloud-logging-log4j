package eu.zirrus.gcloud.logging.log4j;

import java.io.Serializable;
import java.util.Collections;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.google.cloud.MonitoredResource;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Payload.StringPayload;
import com.google.cloud.logging.Severity;

@Plugin(name="CloudLoggingAppender", category="Core", elementType="appender", printObject=true)
public class CloudLoggingAppender extends AbstractAppender {
	
	protected String logName = "default";
	protected MonitoredResource resource;
	protected boolean ignoreExceptions = false;
	protected Logging logging;

	public CloudLoggingAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
		this(name, filter, layout, false);
	}

	public CloudLoggingAppender(String name, Filter filter, Layout<? extends Serializable> layout,
			boolean ignoreExceptions) {
		super(name, filter, layout, ignoreExceptions);
		this.logName = name;
		this.resource = MonitoredResource.newBuilder("global").build();
		this.ignoreExceptions = ignoreExceptions;
		// Instantiates a client
		this.logging = LoggingOptions.getDefaultInstance().getService();
	}

	public void append(LogEvent event) {
	    String text = event.getMessage().getFormattedMessage();

	    LogEntry entry = LogEntry.newBuilder(StringPayload.of(text))
	        .setLogName(logName)
	        .setResource(this.resource)
	        .setSeverity(Severity.valueOf(event.getLevel().name()))
	        .build();

	    // Writes the log entry
	    logging.write(Collections.singleton(entry));

	    System.out.printf("Logged: %s%n", text);
	}
	
    @PluginFactory
    public static CloudLoggingAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        if (name == null) {
            name = "default";
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new CloudLoggingAppender(name, filter, layout, true);
    }

}
