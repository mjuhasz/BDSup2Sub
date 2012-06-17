/*
 * Copyright 2012 Miklos Juhasz (mjuhasz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bdsup2sub.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationAttributes {

    private static final String PROPERTY_FILE = "version.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final String BUILD_DATE = "date";

    private final Properties properties = new Properties();

    private static final ApplicationAttributes INSTANCE = new ApplicationAttributes();


    private ApplicationAttributes() {
        loadProperties();
    }

    public static ApplicationAttributes getInstance() {
        return INSTANCE;
    }

    public String getName() {
        return (String) properties.get(NAME);
    }

    public String getVersion() {
        return (String) properties.get(VERSION);
    }

    public String getBuildDate() {
        return (String) properties.get(BUILD_DATE);
    }

    private void loadProperties() {
        InputStream is = Constants.class.getClassLoader().getResourceAsStream(PROPERTY_FILE);
        if (is != null) {
            try {
                properties.load(is);
            } catch (IOException e) {
                fillWithDefaultValues();
            }
        } else {
            fillWithDefaultValues();
        }
    }

    private void fillWithDefaultValues() {
        properties.put(NAME, "BDSup2Sub");
        properties.put(VERSION, "unknown");
        properties.put(BUILD_DATE, "unknown");
    }
}
