package ru.yandex.qatools.camelot.test.core;

import org.apache.camel.CamelContext;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.builders.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static ru.yandex.qatools.camelot.test.core.TestUtil.pluginMock;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class TestBuildersFactory extends BuildersFactoryImpl {
    final BuildersFactory originalBuildersFactory;
    final ApplicationContext applicationContext;
    final Set<QuartzInitializer> quartzInitializers = new HashSet<>();
    Map<String, Object> mocksStorage = new ConcurrentHashMap<>();

    public TestBuildersFactory(BuildersFactory originalBuildersFactory, ApplicationContext applicationContext) {
        this.originalBuildersFactory = originalBuildersFactory;
        this.applicationContext = applicationContext;
    }

    @Override
    public AggregatorRoutesBuilder newAggregatorPluginRouteBuilder(CamelContext camelContext,
                                                                   Plugin plugin) throws Exception {
        final Class<?> classToMock = plugin.getContext().getClassLoader().loadClass(plugin.getContext().getPluginClass());
        Object mock = pluginMock(plugin);
        mocksStorage.put(plugin.getId(), mock);
        AggregatorRoutesBuilder original = originalBuildersFactory.newAggregatorPluginRouteBuilder(camelContext, plugin);
        return new TestAggregatorPluginRouteBuilder(classToMock, mock, original);
    }

    @Override
    public ProcessorRoutesBuilder newProcessorPluginRouteBuilder(CamelContext camelContext, Plugin plugin) throws Exception {
        final Class<?> classToMock = plugin.getContext().getClassLoader().loadClass(plugin.getProcessor());
        final Object mock = pluginMock(plugin);
        mocksStorage.put(plugin.getId(), mock);

        ProcessorRoutesBuilder original = originalBuildersFactory.newProcessorPluginRouteBuilder(
                camelContext, plugin
        );

        return new TestProcessorPluginRouteBuilder(
                plugin.getContext().getClassLoader(), classToMock, mock, original, plugin
        );
    }

    @Override
    public QuartzInitializer newQuartzInitializer(Scheduler scheduler) throws Exception {
        final QuartzInitializer initializer = originalBuildersFactory.newQuartzInitializer(scheduler);
        quartzInitializers.add(initializer);
        return initializer;
    }

    @Override
    public SchedulerBuildersFactory newSchedulerBuildersFactory(Scheduler scheduler, CamelContext camelContext) {
        return new TestSchedulerBuildersFactory(super.newSchedulerBuildersFactory(scheduler, camelContext),
                camelContext, mocksStorage);
    }

    public Map<String, Object> getMocksStorage() {
        return mocksStorage;
    }

    Set<QuartzInitializer> getQuartzInitializers() {
        return quartzInitializers;
    }
}
