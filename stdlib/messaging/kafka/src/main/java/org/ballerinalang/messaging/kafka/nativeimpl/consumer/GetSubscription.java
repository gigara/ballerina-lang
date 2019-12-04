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
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;

import java.util.Set;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.createKafkaError;

/**
 * Native function returns topic array which consumer is subscribed to.
 */
public class GetSubscription {

    public static Object getSubscription(ObjectValue consumerObject) {
        KafkaConsumer<byte[], byte[]> kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);

        try {
            Set<String> subscriptions = kafkaConsumer.subscription();
            ArrayValue arrayValue = new ArrayValueImpl(new BArrayType(org.ballerinalang.jvm.types.BTypes.typeString));
            if (!subscriptions.isEmpty()) {
                // TODO: Remove this counter variable, and use append method in for loop once #17075 fixed.
                int i = 0;
                for (String subscription : subscriptions) {
                    arrayValue.add(i++, subscription);
                }
            }
            return arrayValue;
        } catch (KafkaException e) {
            return createKafkaError("Failed to retrieve subscribed topics: " + e.getMessage(), CONSUMER_ERROR);
        }
    }
}
