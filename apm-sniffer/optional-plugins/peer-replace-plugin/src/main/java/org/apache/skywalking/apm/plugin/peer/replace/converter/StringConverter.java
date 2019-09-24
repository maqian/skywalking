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

import java.util.regex.Pattern;

/**
 * 
 * @author Ma Qian(maqian258@gmail.com)
 * @date 2019-08-11 12:22
 * @version V1.0
 */
public class StringConverter {
    private Pattern matchPattern;
    private String replacement;

    public StringConverter(String matchPattern, String replacement) {
        this.matchPattern = Pattern.compile(matchPattern);
        this.replacement = replacement;
    }

    public String convert(String input) {
        return matchPattern.matcher(input).replaceAll(replacement);
    }
}
