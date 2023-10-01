/*
 * Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.theentropyshard.teslauncher.settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Settings {
    void load(InputStream inputStream) throws IOException;

    void save(OutputStream outputStream) throws IOException;

    String getString(String key);

    String getString(String key, String def);

    int getInt(String key);

    int getInt(String key, int def);

    long getLong(String key);

    long getLong(String key, long def);

    boolean getBoolean(String key);

    boolean getBoolean(String key, boolean def);

    float getFloat(String key);

    float getFloat(String key, float def);

    double getDouble(String key);

    double getDouble(String key, double def);

    void setValue(String key, String value);

    boolean isEmpty();
}
