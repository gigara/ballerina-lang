// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/java;

# Ballerina interface to provide AMQP `Channel` related functionality.
public type Channel client object {

    handle amqpChannel;

    # Initializes a Ballerina `Channel` object with the given `Connection` object or connection parameters.
    # Creates a `Connection` object if only the connection configuration is given.
    #
    # + connectionOrConnectionConfig - Holds a Ballerina RabbitMQ `Connection` object or the connection parameters.
    public function __init(ConnectionConfiguration|Connection connectionOrConnectionConfig) {
        Connection connection = (connectionOrConnectionConfig is Connection) ?
                                connectionOrConnectionConfig : new Connection(connectionOrConnectionConfig);
        self.amqpChannel = createChannel(connection.amqpConnection);
    }

    # Actively declare a non-exclusive, auto-delete, non-durable queue, or queue with the given configurations.
    #
    # + queueConfig - Holds the parameters required to declare a queue.
    # + return - The name of the queue if autogenerated or nil if the queue was successfully
    #               generated with the given parameters. An error is returned if an I/O error is encountered.
    public remote function queueDeclare(QueueConfiguration? queueConfig = ()) returns string | Error? {
        var result = nativeQueueDeclare(queueConfig, self.amqpChannel);
        if (result is handle) {
            return java:toString(result);
        } else {
            return result;
        }
    }

    # Actively declare a non-auto-delete, non-durable exchange with no extra arguments,
    # If the arguments are specified, then the exchange is declared accordingly.
    #
    # + exchangeConfig - Holds parameters required to declare an exchange.
    # + return - An error if an I/O error is encountered or nil if successful.
    public remote function exchangeDeclare(ExchangeConfiguration exchangeConfig) returns Error? {
        return nativeExchangeDeclare(exchangeConfig, self.amqpChannel);
    }

    # Binds a queue to an exchange with the given binding key.
    #
    # + queueName - Name of the queue.
    # + exchangeName - Name of the exchange.
    # + bindingKey - Binding key used to bind the queue to the exchange.
    # + return - An error if an I/O error is encountered or nil if successful.
    public remote function queueBind(string queueName, string exchangeName, string bindingKey) returns Error? {
         return nativeQueueBind(java:fromString(queueName), java:fromString(exchangeName),
                    java:fromString(bindingKey), self.amqpChannel);
    }

    # Publishes a message. Publishing to a non-existent exchange will result in a channel-level
    # protocol error, which closes the channel.
    #
    # + messageContent - The message body.
    # + routingKey - The routing key.
    # + exchangeName - The name of the exchange to which the message is published.
    # + properties - Other properties for the message - routing headers etc.
    # + return - An error if an I/O error is encountered or nil if successful.
    public remote function basicPublish(@untainted MessageContent messageContent, string routingKey,
                        string exchangeName = "", public BasicProperties? properties = ()) returns Error? {
        return nativeBasicPublish(messageContent, java:fromString(routingKey),
         java:fromString(exchangeName), properties, self.amqpChannel);
    }

    # Deletes the queue with the given name although it is in use or has messages on it.
    # If the parameters ifUnused or ifEmpty is given, the queue is checked before deleting.
    #
    # + queueName - Name of the queue to be deleted.
    # + ifUnused - True if the queue should be deleted only if not in use.
    # + ifEmpty - True if the queue should be deleted only if empty.
    # + return - An error if an I/O error is encountered or nil if successful.
    public remote function queueDelete(string queueName, boolean ifUnused = false, boolean ifEmpty = false)
                        returns Error? {
        return nativeQueueDelete(java:fromString(queueName), ifUnused, ifEmpty, self.amqpChannel);
    }

    # Deletes the exchange with the given name.
    #
    # + exchangeName - The name of the exchange.
    # + return - An I/O error if an error is encountered or nil otherwise.
    public remote function exchangeDelete(string exchangeName) returns Error? {
        return nativeExchangeDelete(java:fromString(exchangeName), self.amqpChannel);
    }

    # Purges the contents of the given queue.
    #
    # + queueName - The name of the queue.
    # + return - An error if an I/O error is encountered or nil if successful.
    public remote function queuePurge(string queueName) returns Error? {
        return nativeQueuePurge(java:fromString(queueName), self.amqpChannel);
    }

    # Retrieves a message synchronously from the given queue, providing direct access to the messages in the queue.
    #
    # + queueName - The name of the queue.
    # + ackMode - Type of acknowledgement mode.
    # + return - `Message` object containing the retrieved message data or an `Error` if an
    #               I/O problem is encountered.
    public remote function basicGet(string queueName, AcknowledgementMode ackMode) returns Message | Error {
        boolean autoAck = ackMode is AUTO_ACK;
        return nativeBasicGet(java:fromString(queueName), autoAck, self.amqpChannel);
    }

    # Retrieve the Connection which carries this channel.
    #
    # + return - RabbitMQ Connection object or error if an I/O problem is encountered.
    public function getConnection() returns Connection | Error {
        return nativeGetConnection(self.amqpChannel);
    }

    # Closes the RabbitMQ `Channel`.
    #
    # + closeCode - The close code (For information, go to the "Reply Codes" section in the
    #               [AMQP 0-9-1 specification] (#https://www.rabbitmq.com/resources/specs/amqp0-9-1.pdf)).
    # + closeMessage - A message indicating the reason for closing the channel.
    # + return - An error if an I/O problem is encountered.
    public function close(int? closeCode = (), string? closeMessage = ()) returns Error? {
        return nativeChannelClose(closeCode, closeMessage, self.amqpChannel);
    }

    # Aborts the RabbitMQ `Channel`. Forces the `Channel` to close and waits for all the close operations
    # to complete. Any encountered exceptions in the close operations are silently discarded.
    #
    # + closeCode - The close code (For information, go to the "Reply Codes" section in the
    #               [AMQP 0-9-1 specification] (#https://www.rabbitmq.com/resources/specs/amqp0-9-1.pdf)).
    # + closeMessage - A message indicating the reason for closing the channel.
    # + return - An error if an I/O problem is encountered.
    public function abortChannel(int? closeCode = (), string? closeMessage = ()) returns Error? {
        return nativeChannelAbort(closeCode, closeMessage, self.amqpChannel);
    }

    function getChannel() returns handle {
        return self.amqpChannel;
    }
};

function createChannel(handle connection) returns handle =
@java:Method {
    class: "org.ballerinalang.messaging.rabbitmq.util.ChannelUtils"
} external;

function nativeQueueDeclare(QueueConfiguration? config, handle amqpChannel) returns handle | Error? =
@java:Method {
    name: "queueDeclare",
    class: "org.ballerinalang.messaging.rabbitmq.util.ChannelUtils"
} external;

function nativeExchangeDeclare(ExchangeConfiguration config, handle amqpChannel) returns Error? =
@java:Method {
    name: "exchangeDeclare",
    class: "org.ballerinalang.messaging.rabbitmq.util.ChannelUtils"
} external;

function nativeQueueBind(handle queueName, handle exchangeName, handle bindingKey, handle amqpChannel) returns Error? =
@java:Method {
    name: "queueBind",
    class: "org.ballerinalang.messaging.rabbitmq.util.ChannelUtils"
} external;

function nativeBasicPublish(MessageContent messageContent, handle routingKey, handle exchangeName,
BasicProperties? properties, handle amqpChannel) returns Error? =
@java:Method {
    name: "basicPublish",
    class: "org.ballerinalang.messaging.rabbitmq.util.ChannelUtils"
} external;

function nativeQueueDelete(handle queueName, boolean ifUnused, boolean ifEmpty, handle amqpChannel) returns Error? =
@java:Method {
    name: "queueDelete",
    class: "org.ballerinalang.messaging.rabbitmq.util.ChannelUtils"
} external;

function nativeExchangeDelete(handle exchangeName, handle amqpChannel) returns Error? =
@java:Method {
    name: "exchangeDelete",
    class: "org.ballerinalang.messaging.rabbitmq.util.ChannelUtils"
} external;

function nativeQueuePurge(handle queueName, handle amqpChannel) returns Error? =
@java:Method {
    name: "queuePurge",
    class: "org.ballerinalang.messaging.rabbitmq.util.ChannelUtils"
} external;

function nativeChannelClose(int? closeCode, string? closeMessage, handle amqpChannel) returns Error? =
@java:Method {
    name: "close",
    class: "org.ballerinalang.messaging.rabbitmq.util.ChannelUtils"
} external;

function nativeChannelAbort(int? closeCode, string? closeMessage, handle amqpChannel) returns Error? =
@java:Method {
    name: "abort",
    class: "org.ballerinalang.messaging.rabbitmq.util.ChannelUtils"
} external;

function nativeGetConnection(handle amqpChannel) returns Connection | Error =
@java:Method {
    name: "getConnection",
    class: "org.ballerinalang.messaging.rabbitmq.util.ChannelUtils"
} external;

function nativeBasicGet(handle queueName, boolean ackMode, handle amqpChannel) returns Message | Error =
@java:Method {
    name: "basicGet",
    class: "org.ballerinalang.messaging.rabbitmq.util.ChannelUtils"
} external;
