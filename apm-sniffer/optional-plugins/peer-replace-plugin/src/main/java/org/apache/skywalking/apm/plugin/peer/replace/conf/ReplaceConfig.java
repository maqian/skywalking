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

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author Ma Qian(maqian258@gmail.com)
 * @date 2019-08-11 09:54
 * @version V1.0
 */
public class ReplaceConfig {
    public static class Peer {
        public static List<String> REPLACE_PATTERNS = Collections.EMPTY_LIST;
    }
}