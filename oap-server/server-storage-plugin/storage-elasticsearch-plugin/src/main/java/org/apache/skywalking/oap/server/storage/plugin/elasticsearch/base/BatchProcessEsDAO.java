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

package org.apache.skywalking.oap.server.storage.plugin.elasticsearch.base;

import org.apache.skywalking.oap.server.core.storage.IBatchDAO;
import org.apache.skywalking.oap.server.library.client.elasticsearch.ElasticSearchClient;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.library.client.request.PrepareRequest;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;

import java.io.IOException;
import java.util.List;

/**
 * @author peng-yongsheng
 */
public class BatchProcessEsDAO extends EsDAO implements IBatchDAO {

    private BulkProcessor bulkProcessor;

    public BatchProcessEsDAO(ElasticSearchClient client, int bulkActions, int flushInterval,
        int concurrentRequests) {
        super(client);
        this.bulkProcessor = new BulkProcessor(bulkActions, flushInterval, concurrentRequests);
    }

    @Override public void asynchronous(InsertRequest insertRequest) throws IOException {
        if (bulkProcessor.getClient() == null) {
            bulkProcessor.setClient(getClient().getClient());
        }
        try {
            bulkProcessor.add(insertRequest);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override public void synchronous(List<PrepareRequest> prepareRequests) {
        if (CollectionUtils.isNotEmpty(prepareRequests)) {
            BulkRequest request = new BulkRequest();

            for (PrepareRequest prepareRequest : prepareRequests) {
                if (prepareRequest instanceof InsertRequest) {
                    request.add((IndexRequest)prepareRequest);
                } else {
                    request.add((UpdateRequest)prepareRequest);
                }
            }
            getClient().synchronousBulk(request);
        }
    }
}
