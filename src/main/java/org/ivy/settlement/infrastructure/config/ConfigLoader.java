package org.ivy.settlement.infrastructure.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.ivy.settlement.infrastructure.string.StringUtils;
import org.ivy.settlement.infrastructure.anyhow.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * description:
 * @author carrot
 */
public class ConfigLoader {

    public Config config;

    public ConfigLoader(String configName) {
        try {
            var apiConfig = ConfigFactory.parseFile(loadConfigFile(configName));

            Config javaSystemProperties = ConfigFactory.load("no-such-resource-only-system-props");
            Config referenceConfig = ConfigFactory.parseResources(configName);
            config = apiConfig;
            config = apiConfig
                    .withFallback(referenceConfig);

            config = javaSystemProperties.withFallback(config)
                    .resolve();     // substitute variables in config if any
        } catch (Exception e) {
            //logger.error("Can't read config.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads resources using given ClassLoader assuming, there could be several resources
     * with the same name
     */
    public static List<InputStream> loadResources(
            final String name, final ClassLoader classLoader) throws IOException {
        final List<InputStream> list = new ArrayList<InputStream>();
        final Enumeration<URL> systemResources =
                (classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader)
                        .getResources(name);
        while (systemResources.hasMoreElements()) {
            list.add(systemResources.nextElement().openStream());
        }
        return list;
    }

    public Config getConfig() {
        return config;
    }

    private static  File loadConfigFile(String confName) {
        String confPath = System.getProperty("confPath");
        File configFile;
        if (!StringUtils.isEmpty(confPath)) {
            configFile = new File(confPath);
        } else {
            configFile = new File("./resource/" + confName);
        }
        Assert.ensure(configFile.exists(), "{} not exist!", configFile.getAbsolutePath());
        Assert.ensure(configFile.canRead(), "{} can't read!", configFile.getAbsolutePath());
        return configFile;
    }
}
