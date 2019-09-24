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

import org.apache.skywalking.oap.server.library.client.request.PrepareRequest;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author Ma Qian(maqian258@gmail.com)
 * @date 2019-08-12 22:54
 * @version V1.0
 */
public class BulkProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BatchProcessEsDAO.class);
    private final AtomicLong executionIdGen = new AtomicLong(0);

    private RestHighLevelClient client;
    private final int bulkActions;
    private final int flushInterval;
    private final int concurrentRequests;
    private final ArrayBlockingQueue<PrepareRequest> queue;
    private final AtomicBoolean flushingQueue;
    private final AtomicLong lastFlushTime;

    public BulkProcessor(int bulkActions, int flushInterval, int concurrentRequests) {
        this.bulkActions = bulkActions;
        this.flushInterval = (int)TimeUnit.SECONDS.toMillis(flushInterval);
        this.concurrentRequests = concurrentRequests;
        this.queue = new ArrayBlockingQueue<>(bulkActions * 4);
        this.flushingQueue = new AtomicBoolean(false);
        this.lastFlushTime =  new AtomicLong(0);
    }

    public RestHighLevelClient getClient() {
        return client;
    }

    public void setClient(RestHighLevelClient client) {
        this.client = client;
    }

    public void add(PrepareRequest request) throws InterruptedException {
        queue.offer(request, flushInterval, TimeUnit.MILLISECONDS);
        if ((queue.size() >= bulkActions || interval() >= flushInterval)
                && flushingQueue.compareAndSet(false, true)) {
            try {
                batchPersistence(batchRequests());
            } finally {
                flushingQueue.set(false);
                lastFlushTime.set(now());
            }
        }
    }

    private long interval() {
        return now() - lastFlushTime.get();
    }

    private long now() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    private List<PrepareRequest> batchRequests() {
        int size = queue.size() >= bulkActions ? bulkActions : queue.size();
        List<PrepareRequest> requests = new ArrayList<>(size);
        queue.drainTo(requests, size);
        return requests;
    }

    private void batchPersistence(List<PrepareRequest> requests) {

        if (logger.isDebugEnabled()) {
            logger.debug("bulk data size: {}", requests.size());
        }

        BulkRequest request = new BulkRequest();
        requests.forEach(builder -> {
            if (builder instanceof IndexRequest) {
                request.add((IndexRequest)builder);
            }
            if (builder instanceof UpdateRequest) {
                request.add((UpdateRequest)builder);
            }
        });

        request.timeout(TimeValue.timeValueSeconds(30));
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.NONE);
        long executionId = executionIdGen.incrementAndGet();
        int numberOfActions = request.numberOfActions();
        logger.info("Executing bulk [{}] with {} requests", executionId, numberOfActions);

        client.bulkAsync(request, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse response) {
                if (response.hasFailures()) {
                    logger.warn("Bulk [{}] executed with failures, {}", executionId, response.buildFailureMessage());
                } else {
                    logger.info("Bulk [{}] completed in {} milliseconds", executionId, response.getTook().getMillis());
                }
            }

            @Override
            public void onFailure(Exception e) {
                logger.error(String.format("Bulk [%s] Failed to execute bulk", executionId), e);
            }
        });
    }
}
