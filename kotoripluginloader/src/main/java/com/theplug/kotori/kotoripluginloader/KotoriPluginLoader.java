package com.theplug.kotori.kotoripluginloader;

import com.google.gson.*;

import javax.inject.Inject;
import javax.swing.*;

import com.theplug.kotori.kotoripluginloader.json.Project;
import com.theplug.kotori.kotoripluginloader.json.Releases;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ExternalPluginsChanged;
import net.runelite.client.plugins.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@PluginDescriptor(
        name = "Kotori Plugin Loader",
        enabledByDefault = false,
        description = "Plugin loader for Kotori's ported plugins.",
        tags = {"kotori","ported","loader"}
)
@Slf4j
public class KotoriPluginLoader extends Plugin
{
    final private String pluginsJson = "https://github.com/OreoCupcakes/kotori-ported-plugins-hosting/blob/master/plugins.json?raw=true";
    final private String hooksFile = "https://github.com/OreoCupcakes/kotori-ported-plugins-hosting/blob/master/hooks.txt?raw=true";

    @Inject
    private Client client;

    @Inject
    private PluginManager manager;

    @Inject
    private EventBus eventBus;

    private int gameRevisionFromHooks;
    private boolean gameRevisionCheck;
    private Project[] jsonProjects;
    private ArrayList<URL> pluginUrlList = new ArrayList<>();
    private List<Plugin> scannedPlugins;


    @Override
    protected void startUp()
    {
        // runs on plugin startup
        log.info("Plugin started");
        parsePluginsJson();
        getPluginURLs();
        loadExternalPlugins();
    }

    @Override
    protected void shutDown()
    {
        // runs on plugin shutdown
        log.info("Plugin stopped");
    }

    private boolean checkGameRevision()
    {
        try
        {
            URL hooksURL = new URL(hooksFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(hooksURL.openStream()));
            String lineToParse;
            while ((lineToParse = bufferedReader.readLine()) != null)
            {
                String[] splitLineArray = lineToParse.replaceAll("[^[a-zA-Z0-9|\\-|\\:|\\.]]","").split(":|\\.");

                if (splitLineArray[0].contains("rsversion"))
                {
                    gameRevisionFromHooks = Integer.parseInt(splitLineArray[1]);
                    break;
                }
            }
        }
        catch (Exception e)
        {
            log.error("Failed at getting RS revision from hooks file.", e);
            return false;
        }

        if (client.getRevision() == gameRevisionFromHooks)
        {
            return true;
        }

        return false;
    }

    private boolean parsePluginsJson()
    {
        try
        {
            URL pluginsURLs = new URL(pluginsJson);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pluginsURLs.openStream()));
            Gson gson = new Gson();
            jsonProjects = gson.fromJson(bufferedReader, Project[].class);
        }
        catch (Exception e)
        {
            log.error("Unable to parse plugins.json", e);
            return false;
        }

        if (jsonProjects != null)
        {
            return true;
        }

        return false;
    }

    private void getPluginURLs()
    {
        for (Project json : jsonProjects)
        {
            pluginUrlList.add(json.getReleases().get(0).getUrl());
        }
    }

    private void loadExternalPlugins()
    {
        try
        {
            URL[] urlArray = pluginUrlList.toArray(URL[]::new);
            URLClassLoader urlClassLoader = new URLClassLoader(urlArray);
            ArrayList<Class<?>> loadedClasses = new ArrayList<>();
            loadedClasses.add(urlClassLoader.loadClass("com.theplug.kotori.gauntletextended.GauntletExtendedPlugin"));
            loadedClasses.add(urlClassLoader.loadClass("com.theplug.kotori.alchemicalhydra.AlchemicalHydraPlugin"));
            loadedClasses.add(urlClassLoader.loadClass("com.theplug.kotori.cerberushelper.CerberusPlugin"));
            loadedClasses.add(urlClassLoader.loadClass("com.theplug.kotori.vorkath.VorkathPlugin"));
            loadedClasses.add(urlClassLoader.loadClass("com.theplug.kotori.demonicgorillas.DemonicGorillaPlugin"));
            scannedPlugins = manager.loadPlugins(loadedClasses,null);

            SwingUtilities.invokeLater(() -> {
                for (Plugin p : scannedPlugins) {
                    if (p ==null)
                        continue;
                    try {
                        manager.startPlugin(p);
                    } catch (PluginInstantiationException e) {
                        e.printStackTrace();
                    }
                }
            });
            eventBus.post(new ExternalPluginsChanged(new ArrayList<>()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}