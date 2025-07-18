ATMQueueSimulation - M/M/1 Queue Simulation in Java
==================================================

Overview:
---------
This Java program simulates an M/M/1 queuing system for an ATM using discrete-event simulation.
It generates random interarrival and service times using the Linear Congruential Method (LCM),
transforms them into exponential variates, and simulates the queuing process for 100 customers.

Key Concepts:
-------------
- M/M/1 Queue: A single-server queuing system where arrivals and services follow an exponential distribution.
- LCM (Linear Congruential Generator): A method for generating pseudo-random numbers uniformly distributed in [0,1).
- Exponential Distribution: Used to model the time between events in a Poisson process.

Main Components:
----------------

1. Constants and Data Structures:
   - MOD, A, C, SEED: Constants for LCM.
   - lambdaArrival, lambdaService: Arrival and service rates.
   - ArrayLists: Store interarrival times, service times, arrival times, etc.

2. LCM(int[] seed):
   - Generates a pseudo-random number between 0 and 1 using the linear congruential formula.
   - Ensures no overflow using long arithmetic and masking.

3. exponential(double u, double lambda):
   - Converts a uniform random number into an exponential variate using the inverse transform method.

4. testUniformity(List<Double> uniformList, String label):
   - Divides uniform values into 10 bins and prints the count per bin to verify uniformity.

5. testIndependence(List<Double> uniformList, String label):
   - Computes the Pearson correlation coefficient between successive values to test independence.

6. simulateQueue():
   - Computes arrival times by summing interarrival times.
   - Determines service start/end times using FIFO logic and tracks server busy time.

7. writeToExcelCSV(String filename):
   - Writes all customer events (arrival, service start/end, wait time, etc.) to a CSV file.
   - Calculates and appends performance metrics and a summary of findings.

Output:
-------
- "ATMQueue.csv": A CSV file that includes:
   * Customer event data (arrival, service times, etc.)
   * System performance metrics
   * Summary of simulation insights

Usage:
------
1. Compile the program:
   javac ATMQueueSimulation.java

2. Run the program:
   java ATMQueueSimulation

3. Open the generated 'ATMQueue.csv' file in Excel to view results.

Author:
-------
Samer Yousif
Simulation & Modeling Project
Submitted: 17/7/2025
