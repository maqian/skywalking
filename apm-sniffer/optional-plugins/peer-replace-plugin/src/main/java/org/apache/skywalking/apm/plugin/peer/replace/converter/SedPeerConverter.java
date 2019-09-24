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

package org.apache.skywalking.apm.plugin.peer.replace.converter;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.plugin.peer.replace.PeerReplaceService;
import org.apache.skywalking.apm.util.PropertyPlaceholderHelper;

import java.util.*;

/**
 * 
 * @author Ma Qian(maqian258@gmail.com)
 * @date 2019-08-11 11:24
 * @version V1.0
 */
public class SedPeerConverter implements PeerConverter {
    private static final ILog LOGGER = LogManager.getLogger(PeerReplaceService.class);

    private List<StringConverter> converters = new ArrayList<StringConverter>();

    public SedPeerConverter(List<String> patterns) {
        if (patterns != null && !patterns.isEmpty()) {
            for (String pattern : patterns) {
                try {
                    StringConverter converter = parse(pattern != null ? pattern.trim() : "");
                    if (converter != null) {
                        converters.add(converter);
                    }
                } catch (Exception e) {
                    LOGGER.error("parse replace pattern failed. pattern: " + pattern, e);
                }
            }
        }
    }

    @Override
    public String convert(String operationName, String remotePeer) {
        for (StringConverter converter : converters) {
            String input = remotePeer + operationName;
            String result = converter.convert(input);
            if (!result.equals(input)) {
                return result;
            }
        }
        return remotePeer;
    }

    private StringConverter parse(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return null;
        }

        char separator = pattern.charAt(pattern.length() - 1);
        if (pattern.charAt(0) != 's' || pattern.charAt(1) != separator) {
            return null;
        }

        char[] patternChars = pattern.toCharArray();
        StringBuilder matchBuffer = new StringBuilder(pattern.length() - 3);
        StringBuilder replacementBuffer = new StringBuilder(pattern.length() - 3);
        StringBuilder appendBuffer = matchBuffer;
        boolean translate = false;
        for (int cursor = 2; cursor < patternChars.length - 1; ++cursor) {
            char c = patternChars[cursor];
            if (translate) {
                //translate \n to $n
                if (Character.isDigit(c)) {
                    appendBuffer.append("$");
                }
                appendBuffer.append(c);
                translate = false;
            } else {
                //start parse replacement chars
                if (appendBuffer != replacementBuffer && c == separator) {
                    appendBuffer = replacementBuffer;
                    continue;
                }
                if (c != '\\') {
                    appendBuffer.append(c);
                } else {
                    translate = true;
                }
            }
        }
        if (matchBuffer.length() == 0) {
            return null;
        }
        if (replacementBuffer.length() == 0) {
            return null;
        }

        return new StringConverter(replacePlaceHolder(matchBuffer.toString()),
                replacePlaceHolder(replacementBuffer.toString()));
    }

    private String replacePlaceHolder(String original) {
        return PropertyPlaceholderHelper.INSTANCE.replacePlaceholders(original, new Properties());
    }

}
