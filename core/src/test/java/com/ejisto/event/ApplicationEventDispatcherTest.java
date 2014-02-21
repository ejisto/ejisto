package com.ejisto.event;

import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.BaseApplicationEvent;
import com.ejisto.modules.executor.TaskManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/20/14
 * Time: 5:43 PM
 */
public class ApplicationEventDispatcherTest {

    private static final long ONE_SECOND = 1000L;
    private final BaseApplicationEvent event = new ApplicationError(this, ApplicationError.Priority.FATAL, null);
    private final TaskManager taskManager = new TaskManager();
    private ApplicationEventDispatcher applicationEventDispatcher;
    private CountDownLatch latch;

    @Before
    public void init() {
        applicationEventDispatcher = new ApplicationEventDispatcher(taskManager);
        latch = new CountDownLatch(1);
        applicationEventDispatcher.registerApplicationEventListener(new ApplicationListener<ApplicationError>() {
            @Override
            public void onApplicationEvent(ApplicationError event) {
                try {
                    Thread.sleep(ONE_SECOND);
                    latch.countDown();
                } catch (InterruptedException e) {
                    throw new AssertionError(e);
                }
            }

            @Override
            public Class<ApplicationError> getTargetEventType() {
                return ApplicationError.class;
            }
        });
    }

    @Test
    public void testBroadcast() throws Exception {
        applicationEventDispatcher.broadcast(event);
        Assert.assertEquals(1, latch.getCount());
        latch.await(ONE_SECOND, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testSynchronousBroadcast() throws Exception {
        applicationEventDispatcher.synchronousBroadcast(event);
        Assert.assertEquals(0, latch.getCount());
    }
}
