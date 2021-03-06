package ru.yandex.qatools.camelot.core.impl;

import ru.yandex.qatools.camelot.api.*;
import ru.yandex.qatools.camelot.core.PluginsService;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginsInteropService implements PluginsInterop {
    final PluginsService pluginsService;

    public PluginsInteropService(PluginsService pluginsService) {
        this.pluginsService = pluginsService;
    }

    @Override
    public AggregatorRepository repo(String pluginId) {
        return pluginsService.getPluginContext(pluginId).getRepository();
    }

    @Override
    public Storage storage(String pluginId) {
        return pluginsService.getPluginContext(pluginId).getStorage();
    }

    @Override
    public EventProducer input(String pluginId) {
        return pluginsService.getPluginContext(pluginId).getInput();
    }

    @Override
    public EventProducer output(String pluginId) {
        return pluginsService.getPluginContext(pluginId).getOutput();
    }

    @Override
    public ClientMessageSender client(String pluginId) {
        return pluginsService.getPluginContext(pluginId).getClientSendersProvider().getSender();
    }

    @Override
    public ClientMessageSender client(String pluginId, String topic) {
        return pluginsService.getPluginContext(pluginId).getClientSendersProvider().getSender(topic);
    }

    @Override
    public AggregatorRepository repo(Class pluginClass) {
        return pluginsService.getPluginContext(pluginClass).getRepository();
    }

    @Override
    public Storage storage(Class pluginClass) {
        return pluginsService.getPluginContext(pluginClass).getStorage();
    }

    @Override
    public EventProducer input(Class pluginClass) {
        return pluginsService.getPluginContext(pluginClass).getInput();
    }

    @Override
    public EventProducer output(Class pluginClass) {
        return pluginsService.getPluginContext(pluginClass).getOutput();
    }

    @Override
    public ClientMessageSender client(Class pluginClass) {
        return pluginsService.getPluginContext(pluginClass).getClientSendersProvider().getSender();
    }

    @Override
    public ClientMessageSender client(Class pluginClass, String topic) {
        return pluginsService.getPluginContext(pluginClass).getClientSendersProvider().getSender(topic);
    }

    @Override
    public PluginInterop forPlugin(final Class pluginClass) {
        return new PluginInterop() {
            @Override
            public AggregatorRepository repo() {
                return PluginsInteropService.this.repo(pluginClass);
            }

            @Override
            public Storage storage() {
                return PluginsInteropService.this.storage(pluginClass);
            }

            @Override
            public EventProducer input() {
                return PluginsInteropService.this.input(pluginClass);
            }

            @Override
            public EventProducer output() {
                return PluginsInteropService.this.output(pluginClass);
            }

            @Override
            public ClientMessageSender client() {
                return PluginsInteropService.this.client(pluginClass);
            }

            @Override
            public ClientMessageSender client(String topic) {
                return PluginsInteropService.this.client(pluginClass, topic);
            }
        };
    }

    @Override
    public PluginInterop forPlugin(final String pluginId) {
        return new PluginInterop() {
            @Override
            public AggregatorRepository repo() {
                return PluginsInteropService.this.repo(pluginId);
            }

            @Override
            public Storage storage() {
                return PluginsInteropService.this.storage(pluginId);
            }

            @Override
            public EventProducer input() {
                return PluginsInteropService.this.input(pluginId);
            }

            @Override
            public EventProducer output() {
                return PluginsInteropService.this.output(pluginId);
            }

            @Override
            public ClientMessageSender client() {
                return PluginsInteropService.this.client(pluginId);
            }

            @Override
            public ClientMessageSender client(String topic) {
                return PluginsInteropService.this.client(pluginId, topic);
            }
        };
    }

}
