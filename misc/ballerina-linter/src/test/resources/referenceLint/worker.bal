import ballerina/io;
public function main() {
    io:println("Worker execution started");
    worker w1 {
        int n = 10000000;
        int sum = 0;
        foreach var i in 1 ... n {
            sum += i;
        }
        io:println("sum of first ", n, " positive numbers = ", sum);
    }
    worker w2 {
        int n = 10000000;
        int m = 1;
        int sum = 0;
        foreach var i in 1 ... n {
            sum += i * i;
            int y;
        }
        io:println("sum of squares of first ", n,
        " positive numbers = ", sum);
    }
}
