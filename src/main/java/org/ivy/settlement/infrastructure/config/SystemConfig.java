package org.ivy.settlement.infrastructure.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.typesafe.config.Config;
import org.ivy.settlement.infrastructure.string.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * description:
 * @author carrot
 */
public abstract class SystemConfig {

    protected Config config;

    public SystemConfig(String configName) {
        this(new ConfigLoader(configName).getConfig());
    }

    public SystemConfig(Config config) {
        this.config = config;
        validateConfig();
        var logPath = logConfigPath();
        if (!StringUtils.isEmpty(logPath)) {
            loadLogConfig(logPath);
        }
    }

    public abstract String logConfigPath();

    public abstract String dbPath();


    private void validateConfig() {
        for (var method : this.getClass().getMethods()) {
            try {
                if (method.isAnnotationPresent(ValidateMe.class)) {
                    method.invoke(this);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error validating config method: " + method, e);
            }
        }
    }

    public void loadLogConfig(String logPath) {
        var logbackFile = new File(logPath);
        if (logbackFile.exists()) {
            var lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            var configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.getStatusManager().clear();
            lc.reset();
            try {
                configurator.doConfigure(logbackFile);
            }
            catch (JoranException e) {
                e.printStackTrace(System.err);
                System.exit(-1);
            }
        }
    }
}
