package ru.yandex.qatools.camelot.core.impl;

import com.hazelcast.core.HazelcastInstance;
import org.apache.camel.Exchange;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.qatools.camelot.api.annotations.OnInit;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.core.FoundMethodProcessor;
import ru.yandex.qatools.camelot.core.PluginMethodInvoker;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:camelot-core-context.xml", "classpath*:test-camelot-core-context.xml"})
@DirtiesContext(classMode = AFTER_CLASS)
@SuppressWarnings("unchecked")
public class SynchronizedAnnotatedMethodInvokerTest {
    @Autowired
    HazelcastInstance hazelcastInstance;

    private final AtomicInteger count = new AtomicInteger();
    Plugin plugin;

    public static class TestPlugin {
        AtomicInteger counter;

        public void setCounter(AtomicInteger counter) {
            this.counter = counter;
        }

        @OnInit
        public void testSync() throws InterruptedException {
            counter.incrementAndGet();
            Thread.sleep(SECONDS.toMillis(5));
        }
    }

    @Before
    public void prepare() {
        plugin = new Plugin();
        plugin.setId("someID");
        PluginContext context = new PluginContext();
        context.setClassLoader(getClass().getClassLoader());
        context.setPluginClass(TestPlugin.class.getName());
        context.setInjector(new PluginContextInjectorImpl<TestPlugin>() {
            @Override
            public void inject(TestPlugin pluginObj, PluginContext pluginContext, Exchange exchange) {
                pluginObj.setCounter(count);
            }
        });
        plugin.setContext(context);
    }

    @Test
    public void testPlugin() throws Exception {
        final PluginMethodInvoker invoker = new SynchronizedAnnotatedMethodInvoker<>(hazelcastInstance,
                plugin, OnInit.class, 2, false).process(new FoundMethodProcessor() {
            @Override
            public boolean appliesTo(Method method, Object annotation) {
                return method.getName().equals("testSync");
            }
        });
        new Thread() {
            @Override
            public void run() {
                invoker.invoke();
            }
        }.start();
        invoker.invoke();
        assertEquals("counter must be equal to 1", 1, count.get());
    }
}
