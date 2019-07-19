package circuitbreaker;

import com.examples.circuitbreaker.client.ServiceA;
import com.examples.circuitbreaker.configuration.CircuitBreakerProducer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CircuitBreakerTest {

    private static ServiceA serviceA;
    private static CircuitBreaker circuitBreaker;
    private CountDownLatch countDownLatch;

    @BeforeClass
    public static void setup() {
        serviceA = new ServiceA();
        CircuitBreakerProducer circuitBreakerProducer = new CircuitBreakerProducer();
        circuitBreakerProducer.init();
        circuitBreaker = circuitBreakerProducer.serviceACircuitBreaker();
        serviceA.setCircuitBreaker(circuitBreaker);
    }

    @After
    public void printStats() {
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        System.out.println(new Date() + " status: " + circuitBreaker.getState() + " ----> failureRate: " + metrics.getFailureRate() + " -- failed calls: " + metrics.getNumberOfFailedCalls() + "-- successful calls: " + metrics.getNumberOfSuccessfulCalls() + " -- notPermittedCalls: " + metrics.getNumberOfNotPermittedCalls());
    }

    @Test
    public void callServiceA() {
        String response = serviceA.getSuccess();
        assertNotNull("Response should not be null", response);
        assertEquals("Response Should be SUCCESS", "SUCCESS", response);
        assertEquals("Circuit Breaker Status should be CLOSED", CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        assertEquals("Circuit Breaker successful count should be 1", 1, circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
        assertEquals("Circuit Breaker failure count should be 0", 0, circuitBreaker.getMetrics().getNumberOfFailedCalls());
    }

    @Test
    public void testServiceA_MultipleCalls() throws InterruptedException {
        int threadCount = 5;
        countDownLatch = new CountDownLatch(threadCount);
        for (int i = 1; i <= threadCount; i++) {
            Thread worker = new Thread(new WorkerThread(i));
            worker.setName("Thread-" + i);
            worker.start();
        }
        countDownLatch.await();
        assertEquals("Circuit Breaker successful count should be 3", 3, circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
        assertEquals("Circuit Breaker failure count should be 3", 3, circuitBreaker.getMetrics().getNumberOfFailedCalls());
        assertEquals("Circuit Breaker state should be OPEN", CircuitBreaker.State.OPEN, circuitBreaker.getState());
    }

    private class WorkerThread implements Runnable {

        private Integer number;

        WorkerThread(int i) {
            this.number = i;
        }

        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName() + " calling serviceA with number " + number);
                String result = (number % 2 == 0) ? serviceA.getSuccess() : serviceA.getFailure();
                System.out.println(Thread.currentThread().getName() + " - result: " + result);
            } finally {
                countDownLatch.countDown();
            }
        }
    }

}
