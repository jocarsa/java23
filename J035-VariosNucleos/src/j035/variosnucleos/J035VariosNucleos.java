package j035.variosnucleos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

// Autor Jose Vicente Carratala
public class J035VariosNucleos{
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        double numero = 1.0000000002;
        long totalIterations = 10000000000L;
        int numThreads = Runtime.getRuntime().availableProcessors(); // Get number of available cores
        System.out.println("En tu sistema tienes  "+numThreads+" cores disponibles");
        
        long chunkSize = totalIterations / numThreads;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        List<Future<Double>> futures = new ArrayList<>();
        
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            futures.add(executor.submit(() -> {
                double localResult = 1.0000000002;
                long start = threadId * chunkSize;
                long end = (threadId == numThreads - 1) ? totalIterations : (threadId + 1) * chunkSize;

                for (long j = start; j < end; j++) {
                    localResult *= 1.00000000534;
                }
                return localResult;
            }));
        }

        for (Future<Double> future : futures) {
            numero *= future.get();
        }

        executor.shutdown();
        
        System.out.println(numero);

    }
}
