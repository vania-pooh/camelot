package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.spi.AggregationRepository;
import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.AggregationRepositoryWithLocalKeys;
import ru.yandex.qatools.camelot.core.AggregationRepositoryWithLocks;

import java.io.Serializable;
import java.util.Set;

import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.util.SerializeUtil.deserializeFromBytes;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ReadonlyAggregatorRepository<T extends Serializable> implements AggregatorRepository<T> {

    private final Plugin plugin;
    private final CamelContext camelContext;

    public ReadonlyAggregatorRepository(CamelContext camelContext, Plugin plugin) {
        this.plugin = plugin;
        this.camelContext = camelContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(String key) {
        final AggregationRepository repo = plugin.getContext().getAggregationRepo();

        final Exchange exchange = (repo instanceof AggregationRepositoryWithLocks) ?
                ((AggregationRepositoryWithLocks) repo).getWithoutLock(camelContext, key) : repo.get(camelContext, key);
        ClassLoader realClassLoader = plugin.getContext().getClassLoader();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader thisClassLoader = getClass().getClassLoader();

        if (realClassLoader != thisClassLoader && contextClassLoader != thisClassLoader) {
            realClassLoader = contextClassLoader;
        }

        if (exchange != null) {
            final Object body = exchange.getIn().getBody();
            if (body instanceof byte[]) try {
                final Class<T> bodyClass = (Class<T>) realClassLoader.loadClass((String) exchange.getIn().getHeader(BODY_CLASS));
                return deserializeFromBytes((byte[]) body, realClassLoader, bodyClass);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read the exchange from resource", e);
            }
            return (T) body;
        }
        return null;
    }

    @Override
    public Set<String> keys() {
        return plugin.getContext().getAggregationRepo().getKeys();
    }

    @Override
    public Set<String> localKeys() {
        final AggregationRepository repo = plugin.getContext().getAggregationRepo();
        return (repo instanceof AggregationRepositoryWithLocalKeys) ?
                ((AggregationRepositoryWithLocalKeys) repo).localKeys() : repo.getKeys();
    }
}
