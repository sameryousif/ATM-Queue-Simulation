
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ATMQueueSimulation {
    static int MOD = (int) Math.pow(2, 31);
    static int A = 1103515245;
    static int C = 12345;
    static int SEED = 42;

    static double lambdaArrival = 0.5;
    static double lambdaService = 1.0;

    static List<Double> uniformInterarrival = new ArrayList<>();
    static List<Double> uniformService = new ArrayList<>();
    static List<Double> interarrivalTimes = new ArrayList<>();
    static List<Double> serviceTimes = new ArrayList<>();
    static List<Double> arrivalTimes = new ArrayList<>();
    static List<Double> startServiceTimes = new ArrayList<>();
    static List<Double> endServiceTimes = new ArrayList<>();

    // Linear Congruential Generator
    public static double LCM(int[] seed) {
        long temp = ((long) A * seed[0] + C) % MOD;
        seed[0] = (int) temp;
        return (double) (seed[0] & 0x7FFFFFFF) / MOD; 
    }

    // Exponential random variate
    public static double exponential(double u, double lambda) {
        return -Math.log(1 - u) / lambda;
    }

    public static void testIndependence(List<Double> uniformList, String label) {
        int n = uniformList.size() - 1;
        double meanU = 0;
        double meanV = 0;

        for (int i = 0; i < n; i++) {
            meanU += uniformList.get(i);
            meanV += uniformList.get(i + 1);
        }

        meanU /= n;
        meanV /= n;

        double covariance = 0, varU = 0, varV = 0;

        for (int i = 0; i < n; i++) {
            double u = uniformList.get(i);
            double v = uniformList.get(i + 1);
            covariance += (u - meanU) * (v - meanV);
            varU += (u - meanU) * (u - meanU);
            varV += (v - meanV) * (v - meanV);
        }

        double correlation = covariance / Math.sqrt(varU * varV);

        System.out.printf("\nIndependence Test for %s Random Numbers:\n", label);
        System.out.printf("Pearson Correlation Coefficient (Ui vs Ui+1): %.4f\n", correlation);

        if (Math.abs(correlation) < 0.1) {
            System.out.println("→ Likely independent.");
        } else {
            System.out.println("→ Might not be independent.");
        }
    }

    public static void main(String[] args) throws IOException {
        int[] seed = { SEED };

        // Generate 50 uniform numbers for interarrival and service times
        for (int i = 0; i < 50; i++) {
            uniformInterarrival.add(LCM(seed));
            uniformService.add(LCM(seed));
        }

        testUniformity(uniformInterarrival, "Interarrival");
        testUniformity(uniformService, "Service");
        testIndependence(uniformInterarrival, "Interarrival");
        testIndependence(uniformService, "Service");

        // Generate 100 exponential values using the 50 uniform numbers 
        for (int i = 0; i < 100; i++) {
            interarrivalTimes.add(exponential(uniformInterarrival.get(i % 50), lambdaArrival));
            serviceTimes.add(exponential(uniformService.get(i % 50), lambdaService));
        }

        simulateQueue();
        writeToExcelCSV("ATMQueue.csv");
    }

    public static void testUniformity(List<Double> uniformList, String label) {
        int[] bins = new int[10];

        for (double u : uniformList) {
            if (u >= 1.0)
                u = 0.999999; // ensure value is within range
            int bin = (int) (u * 10); // bin will be in [0, 9]
            bins[bin]++;
        }

        System.out.println("\nUniformity Test for " + label + " Random Numbers:");
        for (int i = 0; i < 10; i++) {
            System.out.printf("Bin %d (%.1f-%.1f): %d\n", i + 1, i / 10.0, (i + 1) / 10.0, bins[i]);
        }
    }

    static void simulateQueue() {
        double lastDeparture = 0;

        arrivalTimes.add(interarrivalTimes.get(0));
        for (int i = 1; i < interarrivalTimes.size(); i++) {
            arrivalTimes.add(arrivalTimes.get(i - 1) + interarrivalTimes.get(i));
        }

        for (int i = 0; i < arrivalTimes.size(); i++) {
            double arrival = arrivalTimes.get(i);
            double service = serviceTimes.get(i);

            double startService = Math.max(arrival, lastDeparture);
            double endService = startService + service;

            startServiceTimes.add(startService);
            endServiceTimes.add(endService);

            lastDeparture = endService;
        }
    }

    static void writeToExcelCSV(String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);
        fw.write("Customer,Arrival Time,Start Service,End Service,Wait Time,Service Time,Time in System\n");

        double totalWait = 0, totalService = 0, totalSystemTime = 0;
        int completed = arrivalTimes.size();
        double totalTime = endServiceTimes.get(endServiceTimes.size() - 1);
        double busyTime = 0;

        for (int i = 0; i < completed; i++) {
            double wait = startServiceTimes.get(i) - arrivalTimes.get(i);
            double service = serviceTimes.get(i);
            double timeInSystem = endServiceTimes.get(i) - arrivalTimes.get(i);
            busyTime += service;

            fw.write(String.format(Locale.US, "%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f\n",
                    i + 1,
                    arrivalTimes.get(i),
                    startServiceTimes.get(i),
                    endServiceTimes.get(i),
                    wait,
                    service,
                    timeInSystem));

            totalWait += wait;
            totalService += service;
            totalSystemTime += timeInSystem;
        }

        double meanArrivalRate = completed / arrivalTimes.get(completed - 1);
        double meanServiceRate = completed / totalService;
        double serverUtilization = busyTime / totalTime;
        double meanNumInSystem = meanArrivalRate * (totalSystemTime / completed);
        double systemThroughput = completed / totalTime;

        fw.write("\nMetrics,,,\n");
        fw.write(String.format(Locale.US, "Mean Arrival Rate (λ),%.4f\n", meanArrivalRate));
        fw.write(String.format(Locale.US, "Mean Service Rate (μ),%.4f\n", meanServiceRate));
        fw.write(String.format(Locale.US, "Server Utilization (U),%.4f\n", serverUtilization));
        fw.write(String.format(Locale.US, "Mean Number in System (N),%.4f\n", meanNumInSystem));
        fw.write(String.format(Locale.US, "System Throughput (X),%.4f\n", systemThroughput));

        fw.write("\nSummary of Insights,,,\n");
        fw.write("→ The simulation shows how an M/M/1 queue behaves under load.\n");
        fw.write(String.format(Locale.US, "→ The system had %d customers arrive and complete service.\n", completed));
        fw.write(String.format(Locale.US, "→ Average arrival rate (λ) was %.4f and service rate (μ) was %.4f.\n",
                meanArrivalRate, meanServiceRate));
        fw.write(String.format(Locale.US, "→ The server was busy %.2f%% of the time.\n", serverUtilization * 100));
        fw.write(String.format(Locale.US, "→ On average, %.4f customers were in the system.\n", meanNumInSystem));
        fw.write(String.format(Locale.US, "→ Average time a customer spent in the system was %.4f units.\n",
                totalSystemTime / completed));
        fw.write(
                "→ The system throughput is a good approximation of the arrival rate since all customers were served.\n");

        fw.close();
        System.out.println("\nSimulation complete. Results saved to " + filename);
    }
}
