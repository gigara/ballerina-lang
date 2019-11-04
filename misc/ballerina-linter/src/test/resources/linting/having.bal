import ballerina/http;
import ballerina/io;type ClientRequest record {
    string host;
};type RequestCount record {
    string host;
    int count;
};stream<ClientRequest> requestStream = new;
stream<RequestCount> requestCountStream = new;function initRealtimeRequestCounter() returns () {
    requestCountStream.subscribe(printRequestCount);
    forever {
        from requestStream window timeBatch(10000)
        select requestStream.host, count() as count
            group by requestStream.host
               having    count > 6
        => (RequestCount[] counts) {
            foreach var c in counts {
                requestCountStream.publish(c);
            }
        }
    }
}
function printRequestCount(RequestCount reqCount) {
    io:println("ALERT!! : Received more than 6 requests from the " +
                        "host within 10 seconds : " + reqCount.host);
}listener http:Listener ep = new (9090);@http:ServiceConfig {
    basePath: "/"
}
service requestService on ep {    function __init() {
        initRealtimeRequestCounter();
    }    @http:ResourceConfig {
        methods: ["POST"],
        path: "/requests"
    }
    resource function requests(http:Caller conn, http:Request req) {
        string hostName = <@untainted> conn.remoteAddress.host;
        ClientRequest clientRequest = { host: hostName };
        requestStream.publish(clientRequest);        http:Response res = new;
        res.setJsonPayload("{'message' : 'request successfully " +
                                "received'}");
        error? result = conn->respond(res);
        if (result is error) {
            io:println("Error in responding to caller", result);
        }
    }
}
