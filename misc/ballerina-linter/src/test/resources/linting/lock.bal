import ballerina/io;
int counter = 0;public function main() {
    process();
    io:println("final counter value - ", counter);
    io:println("final count field value - ", counterObj.count);
}type Counter object {
    int count = 0;    public function update() {
        foreach var i in 1 ... 1000 {
            lock {
                self.count = self.count + 1;
            }
        }
    }
};
Counter counterObj = new;function process() {
    worker w1 {
        counterObj.update();
        foreach var i in 1 ... 1000 {
               lock
                {
                counter = counter + 1;
            }
        }
    }
    worker w2 {
        counterObj.update();
        foreach var i in 1 ... 1000 {lock {
                counter
                   =            counter + 1;
            }
        }
    }
    worker w3 {
        counterObj.update();
        foreach var i in 1 ... 1000 {
            lock {                counter = counter + 1;} }
    }
    worker w4 {
        counterObj.update();
        foreach var i in 1 ... 1000 {
            lock {
                counter = counter + 1;
            }
        }
    }
    var result = wait {w1,w2,w3,w4};
}
