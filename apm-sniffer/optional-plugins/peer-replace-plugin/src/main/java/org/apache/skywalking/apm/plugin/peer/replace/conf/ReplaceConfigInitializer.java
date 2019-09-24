/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.plugin.peer.replace.conf;

import java.io.*;
import java.util.*;
import org.apache.skywalking.apm.agent.core.boot.*;
import org.apache.skywalking.apm.agent.core.conf.ConfigNotFoundException;
import org.apache.skywalking.apm.agent.core.logging.api.*;
import org.apache.skywalking.apm.util.*;

/**
 *
 * @author Ma Qian(maqian258@gmail.com)
 * @date 2019-08-11 09:54
 * @version V1.0
 */
public class ReplaceConfigInitializer {
    private static final ILog LOGGER = LogManager.getLogger(ReplaceConfigInitializer.class);
    private static String CONFIG_FILE_NAME = "/config/apm-peer-replace-plugin.config";
    private static String PROP_KEY_PREFIX = "skywalking.";

    /**
     * Try to locate `apm-peer-replace-plugin.config`, which should be in the /optional-plugins/apm-trace-ignore-plugin/
     * dictionary of agent package.
     * <p>
     * Also try to override the config by system.env and system.properties. All the keys in these two places should
     * start with {@link #PROP_KEY_PREFIX}. e.g. in system.properties `skywalking.peer.replace_pattern=your_path` to override
     * `trace.ignore_path` in apm-peer-replace-plugin.config file.
     * <p>
     */
    public static void initialize() throws ConfigNotFoundException, AgentPackageNotFoundException {
        InputStream configFileStream;
        try {
            configFileStream = loadConfigFromAgentFolder();
            Properties properties = new Properties();
            properties.load(configFileStream);
            PropertyPlaceholderHelper helper = PropertyPlaceholderHelper.INSTANCE;
            for (String key : properties.stringPropertyNames()) {
                String value = (String)properties.get(key);
                //replace the key's value. properties.replace(key,value) in jdk8+
                properties.put(key, helper.replacePlaceholders(value, properties));
            }
            ConfigInitializer.initialize(properties, ReplaceConfig.class);
        } catch (Exception e) {
            LOGGER.error(e, "Failed to read the config file, skywalking is going to run in default config.");
        }

        try {
            overrideConfigBySystemProp();
        } catch (Exception e) {
            LOGGER.error(e, "Failed to read the system prop.");
        }
    }

    private static void overrideConfigBySystemProp() throws IllegalAccessException {
        Properties properties = new Properties();
        Properties systemProperties = System.getProperties();
        Iterator<Map.Entry<Object, Object>> entryIterator = systemProperties.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<Object, Object> prop = entryIterator.next();
            if (prop.getKey().toString().startsWith(PROP_KEY_PREFIX)) {
                String realKey = prop.getKey().toString().substring(PROP_KEY_PREFIX.length());
                properties.put(realKey, prop.getValue());
            }
        }

        if (!properties.isEmpty()) {
            ConfigInitializer.initialize(properties, ReplaceConfig.class);
        }
    }

    /**
     * Load the config file, where the agent jar is.
     *
     * @return the config file {@link InputStream}, or null if not needEnhance.
     */
    private static InputStream loadConfigFromAgentFolder() throws AgentPackageNotFoundException, ConfigNotFoundException {
        File configFile = new File(AgentPackagePath.getPath(), CONFIG_FILE_NAME);
        if (configFile.exists() && configFile.isFile()) {
            try {
                LOGGER.info("peer replace config file found in {}.", configFile);
                return new FileInputStream(configFile);
            } catch (FileNotFoundException e) {
                throw new ConfigNotFoundException("Fail to load apm-peer-replace-plugin.config", e);
            }
        }
        throw new ConfigNotFoundException("Fail to load peer replace config file.");
    }
}
