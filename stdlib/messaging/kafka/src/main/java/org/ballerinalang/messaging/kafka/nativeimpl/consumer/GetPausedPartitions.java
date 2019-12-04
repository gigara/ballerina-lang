/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.kafka.nativeimpl.consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;

import java.util.Set;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicPartitionRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.populateTopicPartitionRecord;

/**
 * Native function returns paused partitions for given consumer.
 */
public class GetPausedPartitions {

    public static Object getPausedPartitions(ObjectValue consumerObject) {
        KafkaConsumer<byte[], byte[]> kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        ArrayValue topicPartitionArray = new ArrayValueImpl(new BArrayType(getTopicPartitionRecord().getType()));
        try {
            Set<TopicPartition> pausedPartitions = kafkaConsumer.paused();
//            pausedPartitions.forEach(partition -> {
//                MapValue<String, Object> tp = populateTopicPartitionRecord(partition.topic(), partition.partition());
//                topicPartitionArray.append(tp);
//            });
            // TODO: Use the above commented code instead of the for loop once #17075 fixed.
            int i = 0;
            for (TopicPartition partition : pausedPartitions) {
                MapValue<String, Object> tp = populateTopicPartitionRecord(partition.topic(), partition.partition());
                topicPartitionArray.add(i++, tp);
            }

            return topicPartitionArray;
        } catch (KafkaException e) {
            return createKafkaError("Failed to retrieve paused partitions: " + e.getMessage(), CONSUMER_ERROR);
        }
    }
}
