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

import ballerina/auth;
import ballerina/h2;
import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/runtime;
import ballerina/websub;

const string WEBSUB_PERSISTENCE_TOPIC_ONE = "http://one.persistence.topic.com";
const string WEBSUB_PERSISTENCE_TOPIC_TWO = "http://two.persistence.topic.com";
const string WEBSUB_TOPIC_ONE = "http://one.websub.topic.com";

auth:ConfigAuthStoreProvider basicAuthProvider = new;
http:BasicAuthHeaderAuthnHandler basicAuthnHandler = new(basicAuthProvider);

websub:WebSubHub webSubHub = startHubAndRegisterTopic();

listener http:Listener publisherServiceEP = new http:Listener(8080);

websub:Client websubHubClientEP = new websub:Client(webSubHub.hubUrl, config = {
    auth: {
        scheme: http:BASIC_AUTH,
        config: {
            username: "peter",
            password: "pqr"
        }
    }
});

service publisher on publisherServiceEP {
    @http:ResourceConfig {
        methods: ["GET", "HEAD"]
    }
    resource function discover(http:Caller caller, http:Request req) {
        http:Response response = new;
        // Add a link header indicating the hub and topic
        websub:addWebSubLinkHeader(response, [webSubHub.hubUrl], WEBSUB_PERSISTENCE_TOPIC_ONE);
        var err = caller->accepted(message = response);
        if (err is error) {
            log:printError("Error responding on discovery", err = err);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/notify/{subscriber}"
    }
    resource function notify(http:Caller caller, http:Request req, string subscriber) {
        var payload = req.getJsonPayload();
        if (payload is error) {
            panic payload;
        }

        checkSubscriberAvailability(WEBSUB_PERSISTENCE_TOPIC_ONE, "http://localhost:" + subscriber + "/websub");
        var err = webSubHub.publishUpdate(WEBSUB_PERSISTENCE_TOPIC_ONE, untaint <json> payload);
        if (err is error) {
            log:printError("Error publishing update directly", err = err);
        }

        http:Response response = new;
        err = caller->accepted(message = response);
        if (err is error) {
            log:printError("Error responding on notify request", err = err);
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
        websub:addWebSubLinkHeader(response, [webSubHub.hubUrl], WEBSUB_PERSISTENCE_TOPIC_TWO);
        var err = caller->accepted(message = response);
        if (err is error) {
            log:printError("Error responding on discovery", err = err);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function notify(http:Caller caller, http:Request req) {
        var payload = req.getJsonPayload();
        if (payload is error) {
            panic payload;
        }

        checkSubscriberAvailability(WEBSUB_PERSISTENCE_TOPIC_TWO, "http://localhost:8383/websubTwo");
        var err = webSubHub.publishUpdate(WEBSUB_PERSISTENCE_TOPIC_TWO, untaint <json> payload);
        if (err is error) {
            log:printError("Error publishing update directly", err = err);
        }

        http:Response response = new;
        err = caller->accepted(message = response);
        if (err is error) {
            log:printError("Error responding on notify request", err = err);
        }
    }
}

service publisherThree on publisherServiceEP {
    @http:ResourceConfig {
        methods: ["GET", "HEAD"]
    }
    resource function discover(http:Caller caller, http:Request req) {
        http:Response response = new;
        // Add a link header indicating the hub and topic
        websub:addWebSubLinkHeader(response, [webSubHub.hubUrl], WEBSUB_TOPIC_ONE);
        var err = caller->accepted(message = response);
        if (err is error) {
            log:printError("Error responding on discovery", err = err);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function notify(http:Caller caller, http:Request req) {
        var payload = req.getJsonPayload();
        if (payload is error) {
            panic payload;
        }
        checkSubscriberAvailability(WEBSUB_TOPIC_ONE, "http://localhost:8484/websubFour");
        var err = websubHubClientEP->publishUpdate(WEBSUB_TOPIC_ONE, untaint <json> payload);
        if (err is error) {
            log:printError("Error publishing update remotely", err = err);
        }

        http:Response response = new;
        err = caller->accepted(message = response);
        if (err is error) {
            log:printError("Error responding on notify request", err = err);
        }
    }
}

service helperService on publisherServiceEP {
    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function restartHub(http:Caller caller, http:Request req) {
        if (!webSubHub.stop()) {
            log:printError("hub shutdown failed!");
        }
        webSubHub = startHubAndRegisterTopic();
        checkpanic caller->accepted();
    }
}

function startHubAndRegisterTopic() returns websub:WebSubHub {
    websub:WebSubHub internalHub = startWebSubHub();
    var err = internalHub.registerTopic(WEBSUB_PERSISTENCE_TOPIC_ONE);
    if (err is error) {
        log:printError("Error registering topic", err = err);
    }
    err = internalHub.registerTopic(WEBSUB_PERSISTENCE_TOPIC_TWO);
    if (err is error) {
        log:printError("Error registering topic", err = err);
    }
    err = internalHub.registerTopic(WEBSUB_TOPIC_ONE);
    if (err is error) {
        log:printError("Error registering topic", err = err);
    }
    return internalHub;
}

function startWebSubHub() returns websub:WebSubHub {
    h2:Client h2Client = new({
        path: "./target/hubDB",
        name: "hubdb",
        username: "",
        password: "",
        poolOptions: { maximumPoolSize: 5 }
    });
    websub:HubPersistenceStore hpo = new websub:H2HubPersistenceStore(h2Client);
    var result = websub:startHub(new http:Listener(9191, config =  {
        auth: {
            authnHandlers: [basicAuthnHandler]
        },
        secureSocket: {
            keyStore: {
                path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                password: "ballerina"
            },
            trustStore: {
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            }
        }
    }),
    hubConfiguration = { remotePublish : { enabled : true }, hubPersistenceStore: hpo });
    if (result is websub:WebSubHub) {
        return result;
    } else {
        return result.startedUpHub;
    }
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
