package com.mykr.acgnhistoryanalyzer.temptest;

public class test {

}
class ThreadDemo7 {
    private static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        Runnable task = () -> {
            for (int i = 0; i < 10000; i++) {
                count++;
            }
            System.out.println(count);
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();
    }
}