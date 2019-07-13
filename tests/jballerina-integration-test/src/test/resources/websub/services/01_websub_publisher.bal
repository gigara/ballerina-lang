// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/runtime;
import ballerina/websub;

const string WEBSUB_TOPIC_ONE = "http://one.websub.topic.com";
const string WEBSUB_TOPIC_TWO = "http://two.websub.topic.com";
const string WEBSUB_TOPIC_THREE = "http://three.websub.topic.com";
const string WEBSUB_TOPIC_FOUR = "http://four.websub.topic.com";
const string WEBSUB_TOPIC_FIVE = "http://one.redir.topic.com";
const string WEBSUB_TOPIC_SIX = "http://two.redir.topic.com";

boolean remoteTopicRegistered = false;

websub:WebSubHub webSubHub = startHubAndRegisterTopic();

websub:Client websubHubClientEP = new websub:Client(webSubHub.hubUrl);

listener http:Listener publisherServiceEP = new http:Listener(8080);

service publisher on publisherServiceEP {
    @http:ResourceConfig {
        methods: ["GET", "HEAD"]
    }
    resource function discover(http:Caller caller, http:Request req) {
        http:Response response = new;
        // Add a link header indicating the hub and topic
        websub:addWebSubLinkHeader(response, [webSubHub.hubUrl], WEBSUB_TOPIC_ONE);
        var err = caller->accepted(message = response);
        if (err is error) {
            log:printError("Error responding on ordering", err = err);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/notify/{subscriber}"
    }
    resource function notify(http:Caller caller, http:Request req, string subscriber) {
        remoteRegisterTopic();
        json jsonPayload = <json> req.getJsonPayload();
        string mode = jsonPayload.mode.toString();
        string contentType = jsonPayload.content_type.toString();

        var err = caller->accepted();
        if (err is error) {
            log:printError("Error responding on notify request", err = err);
        }

        if (subscriber != "skip_subscriber_check") {
            checkSubscriberAvailability(WEBSUB_TOPIC_ONE, "http://localhost:" + subscriber + "/websub");
            checkSubscriberAvailability(WEBSUB_TOPIC_ONE, "http://localhost:" + subscriber + "/websubTwo");
            checkSubscriberAvailability(WEBSUB_TOPIC_ONE, "http://localhost:" + subscriber + "/websubThree?topic=" +
                    WEBSUB_TOPIC_ONE + "&fooVal=barVal");
        }

        if (mode == "internal") {
            err = webSubHub.publishUpdate(WEBSUB_TOPIC_ONE, getPayloadContent(contentType, mode));
            if (err is error) {
                log:printError("Error publishing update directly", err = err);
            }
        } else {
            err = websubHubClientEP->publishUpdate(WEBSUB_TOPIC_ONE, getPayloadContent(contentType, mode));
            if (err is error) {
                log:printError("Error publishing update remotely", err = err);
            }
        }
    }

    resource function topicInfo(http:Caller caller, http:Request req) {
        if (req.hasHeader("x-topic")) {
            string topicName = req.getHeader("x-topic");
            websub:SubscriberDetails[] details = webSubHub.getSubscribers(topicName);
            json j = <json> json.convert(details[0]);
            var err = caller->respond(j);
            if (err is error) {
                log:printError("Error responding on topicInfo request", err = err);
            }
        } else {
            map<string> allTopics = {};
            int index=1;
            string [] availableTopics = webSubHub.getAvailableTopics();
            foreach var topic in availableTopics {
                allTopics["Topic_" + index] = topic;
                index += 1;
            }
            json j = <json> json.convert(allTopics);
            var err = caller->respond(j);
            if (err is error) {
                log:printError("Error responding on topicInfo request", err = err);
            }
        }
    }
}

service publisherTwo on publisherServiceEP {
    @http:ResourceConfig {
        methods: ["GET", "HEAD"]
    }
    resource function discover(http:Caller caller, http:Request req) {
        http:Response response = new;
        // Add a link header indicating the hub and topic
        websub:addWebSubLinkHeader(response, [webSubHub.hubUrl], WEBSUB_TOPIC_FOUR);
        var err = caller->accepted(message = response);
        if (err is error) {
            log:printError("Error responding on ordering", err = err);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function notify(http:Caller caller, http:Request req) {
        checkSubscrberAvailabilityAndPublishDirectly(WEBSUB_TOPIC_THREE, "http://localhost:8383/websub",
                                                     {"action":"publish","mode":"internal-hub"});
        checkSubscrberAvailabilityAndPublishDirectly(WEBSUB_TOPIC_FOUR, "http://localhost:8383/websubTwo",
                                                     {"action":"publish","mode":"internal-hub-two"});

        var err = caller->accepted();
        if (err is error) {
            log:printError("Error responding on notify request", err = err);
        }
    }
}

service contentTypePublisher on publisherServiceEP {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/notify/{port}"
    }
    resource function notify(http:Caller caller, http:Request req, string port) {
        json jsonPayload = <json> req.getJsonPayload();
        string mode = jsonPayload.mode.toString();
        string contentType = jsonPayload.content_type.toString();

        var err = caller->accepted();
        if (err is error) {
            log:printError("Error responding on notify request", err = err);
        }

        if (port != "skip_subscriber_check") {
            checkSubscriberAvailability(WEBSUB_TOPIC_ONE, "http://localhost:" + port + "/websub");
            checkSubscriberAvailability(WEBSUB_TOPIC_ONE, "http://localhost:" + port + "/websubTwo");
        }

        if (mode == "internal") {
            err = webSubHub.publishUpdate(WEBSUB_TOPIC_ONE, getPayloadContent(contentType, mode));
            if (err is error) {
                log:printError("Error publishing update directly", err = err);
            }
        } else {
            err = websubHubClientEP->publishUpdate(WEBSUB_TOPIC_ONE, getPayloadContent(contentType, mode));
            if (err is error) {
                log:printError("Error publishing update remotely", err = err);
            }
        }
    }
}

function checkSubscrberAvailabilityAndPublishDirectly(string topic, string subscriber, json payload) {
    checkSubscriberAvailability(topic, subscriber);
    var err = webSubHub.publishUpdate(topic, payload);
    if (err is error) {
        log:printError("Error publishing update directly", err = err);
    }
}

function startHubAndRegisterTopic() returns websub:WebSubHub {
    websub:WebSubHub internalHub = startWebSubHub();
    var err = internalHub.registerTopic(WEBSUB_TOPIC_ONE);
    if (err is error) {
        log:printError("Error registering topic directly", err = err);
    }
    err = internalHub.registerTopic(WEBSUB_TOPIC_THREE);
    if (err is error) {
        log:printError("Error registering topic directly", err = err);
    }
    err = internalHub.registerTopic(WEBSUB_TOPIC_FOUR);
    if (err is error) {
        log:printError("Error registering topic directly", err = err);
    }
    err = internalHub.registerTopic(WEBSUB_TOPIC_FIVE);
    if (err is error) {
        log:printError("Error registering topic directly", err = err);
    }
    err = internalHub.registerTopic(WEBSUB_TOPIC_SIX);
    if (err is error) {
        log:printError("Error registering topic directly", err = err);
    }
    return internalHub;
}

function startWebSubHub() returns websub:WebSubHub {
    var result = websub:startHub(new http:Listener(9191), hubConfiguration = { remotePublish : { enabled : true }});
    if (result is websub:WebSubHub) {
        return result;
    } else {
        return result.startedUpHub;
    }
}

function remoteRegisterTopic()  {
    if (remoteTopicRegistered) {
        return;
    }
    var err = websubHubClientEP->registerTopic(WEBSUB_TOPIC_TWO);
    if (err is error) {
        log:printError("Error registering topic remotely", err = err);
    }
    remoteTopicRegistered = true;
}

function getPayloadContent(string contentType, string mode) returns string|xml|json|byte[]|io:ReadableByteChannel {
    string errorMessage = "unknown content type";
    if (contentType == "" || contentType == "json") {
        if (mode == "internal") {
            json j = {"action":"publish","mode":"internal-hub"};
            return j;
        }
        json k = {"action":"publish","mode":"remote-hub"};
        return k;
    } else if (contentType == "string") {
        if (mode == "internal") {
            return "Text update for internal Hub";
        }
        return "Text update for remote Hub";
    } else if (contentType == "xml") {
        if (mode == "internal") {
            return xml `<websub><request>Notification</request><type>Internal</type></websub>`;
        }
        return xml `<websub><request>Notification</request><type>Remote</type></websub>`;
    } else if (contentType == "byte[]" || contentType == "io:ReadableByteChannel") {
        errorMessage = "content type " + contentType + " not yet supported with WebSub tests";
    }
    error e = error(websub:WEBSUB_ERROR_CODE, message = errorMessage);
    panic e;
}

function checkSubscriberAvailability(string topic, string callback) {
    int count = 0;
    boolean subscriberAvailable = false;
    while (!subscriberAvailable && count < 60) {
        websub:SubscriberDetails[] topicDetails = webSubHub.getSubscribers(topic);
        if (isSubscriberAvailable(topicDetails, callback)) {
            return;
        }
        runtime:sleep(1000);
        count += 1;
    }
}

function isSubscriberAvailable(websub:SubscriberDetails[] topicDetails, string callback) returns boolean {
    foreach var detail in topicDetails {
        if (detail.callback == callback) {
            return true;
        }
    }
    return false;
}
