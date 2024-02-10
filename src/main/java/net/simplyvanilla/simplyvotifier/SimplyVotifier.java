/*
 * Copyright (C) 2012 Vex Software LLC
 * This file is part of Votifier.
 *
 * Votifier is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Votifier is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Votifier.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.simplyvanilla.simplyvotifier;

import com.bencodez.advancedcore.AdvancedCorePlugin;
import com.bencodez.advancedcore.folialib.FoliaLib;
import lombok.Getter;
import lombok.Setter;
import net.simplyvanilla.simplyvotifier.config.Config;
import net.simplyvanilla.simplyvotifier.crypto.RSAIO;
import net.simplyvanilla.simplyvotifier.crypto.RSAKeygen;
import net.simplyvanilla.simplyvotifier.model.Vote;
import net.simplyvanilla.simplyvotifier.model.VotifierEvent;
import net.simplyvanilla.simplyvotifier.net.VoteReceiver;
import org.bukkit.Bukkit;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.KeyPair;

/**
 * The main Votifier plugin class.
 *
 * @author Blake Beaupain
 * @author Kramer Campbell
 */
public class SimplyVotifier extends AdvancedCorePlugin {

    /**
     * The Votifier instance.
     */
    @Getter
    private static SimplyVotifier instance;

    @Getter
    private FoliaLib foliaLib;

    public Config config;

    /**
     * The vote receiver.
     */
    @Getter
    private VoteReceiver voteReceiver;

    /**
     * The RSA key pair.
     */
    @Getter
    @Setter
    private KeyPair keyPair;


    @Override
    public void onPostLoad() {
        this.foliaLib = new FoliaLib(this);

        File rsaDirectory = new File(getDataFolder() + "/rsa");

        /*
         * Create RSA directory and keys if it does not exist; otherwise, read keys.
         */
        try {
            if (!rsaDirectory.exists()) {
                rsaDirectory.mkdir();
                keyPair = RSAKeygen.generate(2048);
                RSAIO.save(rsaDirectory, keyPair);
            } else {
                keyPair = RSAIO.load(rsaDirectory);
            }
        } catch (Exception ex) {
            getLogger().severe("Error reading configuration file or RSA keys");
            gracefulExit();
            return;
        }

        loadVoteReceiver();
    }

    private void loadVoteReceiver() {
        try {
            voteReceiver = new VoteReceiver(config.getHost(), config.getPort()) {

                @Override
                public void logWarning(String warn) {
                    getLogger().warning(warn);
                }

                @Override
                public void logSevere(String msg) {
                    getLogger().severe(msg);
                }

                @Override
                public void log(String msg) {
                    getLogger().info(msg);
                }

                @Override
                public String getVersion() {
                    return getDescription().getVersion();
                }

                @Override
                public KeyPair getKeyPair() {
                    return instance.getKeyPair();
                }

                @Override
                public void debug(Exception e) {
                    instance.debug(e);
                }

                @Override
                public void debug(String debug) {
                    instance.debug(debug);
                }

                @Override
                public void callEvent(Vote vote) {
                    foliaLib.getImpl().runAsync(new Runnable() {
                        public void run() {
                            Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(vote));
                        }
                    });
                }
            };
            voteReceiver.start();

            getLogger().info("Votifier enabled.");
        } catch (Exception ex) {
            gracefulExit();
            return;
        }
    }

    @Override
    public void onDisable() {
        // Interrupt the vote receiver.
        if (voteReceiver != null) {
            voteReceiver.shutdown();
        }
        getLogger().info("Votifier disabled.");
    }

    private void gracefulExit() {
        getLogger().severe("Votifier did not initialize properly!");
    }

    @Override
    public void onPreLoad() {
        instance = this;

        config = new Config(this);
        config.setup();

        if (config.isJustCreated()) {
            int openPort = 8192;
            try {
                ServerSocket s = new ServerSocket();
                s.bind(new InetSocketAddress("0.0.0.0", 0));
                openPort = s.getLocalPort();
                s.close();
            } catch (Exception e) {

            }
            try {
                // First time run - do some initialization.
                getLogger().info("Configuring Votifier for the first time...");
                config.getData().set("port", openPort);
                config.saveData();

                /*
                 * Remind hosted server admins to be sure they have the right port number.
                 */
                getLogger().info("------------------------------------------------------------------------------");
                getLogger().info("Assigning Votifier to listen on an open port " + openPort
                    + ". If you are hosting server on a");
                getLogger().info("shared server please check with your hosting provider to verify that this port");
                getLogger().info("is available for your use. Chances are that your hosting provider will assign");
                getLogger().info("a different port, which you need to specify in config.yml");
                getLogger().info("------------------------------------------------------------------------------");

            } catch (Exception ex) {
                getLogger().severe("Error creating configuration file");
                debug(ex);
            }
        }
        config.loadValues();

        updateAdvancedCoreHook();
    }

    @Override
    public void onUnLoad() {

    }

    @Override
    public void reload() {
        config.reloadData();
        updateAdvancedCoreHook();
        voteReceiver.shutdown();
        loadVoteReceiver();
    }

    @SuppressWarnings("deprecation")
    public void updateAdvancedCoreHook() {
        setConfigData(config.getData());
        setLoadRewards(false);
        setLoadServerData(false);
        setLoadUserData(false);
        setLoadGeyserAPI(false);
        setLoadLuckPerms(false);
    }

}
