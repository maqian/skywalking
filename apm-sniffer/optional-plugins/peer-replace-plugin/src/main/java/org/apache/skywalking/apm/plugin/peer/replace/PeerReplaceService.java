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

package org.apache.skywalking.apm.plugin.peer.replace;

import org.apache.skywalking.apm.agent.core.boot.AgentPackageNotFoundException;
import org.apache.skywalking.apm.agent.core.boot.OverrideImplementor;
import org.apache.skywalking.apm.agent.core.conf.ConfigNotFoundException;
import org.apache.skywalking.apm.agent.core.context.PeerService;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.plugin.peer.replace.conf.ReplaceConfig;
import org.apache.skywalking.apm.plugin.peer.replace.conf.ReplaceConfigInitializer;
import org.apache.skywalking.apm.plugin.peer.replace.converter.PeerConverter;
import org.apache.skywalking.apm.plugin.peer.replace.converter.SedPeerConverter;

/**
 *
 * @author Ma Qian(maqian258@gmail.com)
 * @date 2019-08-11 11:24
 * @version V1.0
 */
@OverrideImplementor(PeerService.class)
public class PeerReplaceService implements PeerService {

    private static final ILog LOGGER = LogManager.getLogger(PeerReplaceService.class);
    private PeerConverter converter;

    @Override
    public void prepare() throws Throwable {

    }

    @Override
    public void boot() {
        try {
            ReplaceConfigInitializer.initialize();
            converter = new SedPeerConverter(ReplaceConfig.Peer.REPLACE_PATTERNS);
        } catch (ConfigNotFoundException e) {
            LOGGER.error("peer replace config init error", e);
        } catch (AgentPackageNotFoundException e) {
            LOGGER.error("peer replace config init error", e);
        }
    }

    @Override
    public void onComplete() throws Throwable {

    }

    @Override
    public void shutdown() throws Throwable {

    }

    @Override
    public String replaceRemotePeer(String operationName, String remotePeer) {
        return converter.convert(operationName, remotePeer);
    }
}
