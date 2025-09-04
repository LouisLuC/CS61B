package timingtest;

import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();

        for (int i = 1; i <= 128; i *= 2) {
            int numOfElements = i * 1000;
            // int getLastCallsNum = 10000;
            int M = 10000;

            // Add items to SLList to build the data structure
            SLList<Integer> slList = new SLList<>();
            for (int j = 0; j < numOfElements; j++) {
                slList.addLast(j);
            }

            // start timer and call methods
            Stopwatch sw = new Stopwatch();
            for (int j = 0; j < M; j++) {
                slList.getLast();
            }
            double timeInSeconds = sw.elapsedTime();

            Ns.addLast(numOfElements);
            opCounts.addLast(M);
            times.addLast(timeInSeconds);
        }
        printTimingTable(Ns, times, opCounts);
    }
}

