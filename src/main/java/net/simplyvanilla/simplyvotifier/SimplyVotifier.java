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

import java.io.File;
import java.security.KeyPair;
import lombok.Getter;
import lombok.Setter;
import net.simplyvanilla.simplyvotifier.crypto.RSAIO;
import net.simplyvanilla.simplyvotifier.crypto.RSAKeygen;
import net.simplyvanilla.simplyvotifier.model.Vote;
import net.simplyvanilla.simplyvotifier.model.VotifierEvent;
import net.simplyvanilla.simplyvotifier.net.VoteReceiver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main Votifier plugin class.
 *
 * @author Blake Beaupain
 * @author Kramer Campbell
 */
public class SimplyVotifier extends JavaPlugin {

  /** The Votifier instance. */
  @Getter private static SimplyVotifier instance;

  /** The vote receiver. */
  @Getter private VoteReceiver voteReceiver;

  /** The RSA key pair. */
  @Getter @Setter private KeyPair keyPair;

  @Override
  public void onEnable() {
    this.saveDefaultConfig();
    this.reloadConfig();
    File rsaDirectory = new File(this.getDataFolder() + "/rsa");

    /*
     * Create RSA directory and keys if it does not exist; otherwise, read keys.
     */
    try {
      if (!rsaDirectory.exists()) {
        rsaDirectory.mkdir();
        this.keyPair = RSAKeygen.generate(2048);
        RSAIO.save(rsaDirectory, this.keyPair);
      } else {
        this.keyPair = RSAIO.load(rsaDirectory);
      }
    } catch (Exception ex) {
      this.getLogger().severe("Error reading configuration file or RSA keys");
      return;
    }

    this.loadVoteReceiver();
  }

  private void loadVoteReceiver() {
    try {
      this.voteReceiver =
          new VoteReceiver(this.getConfig().getString("host"), this.getConfig().getInt("port")) {

            @Override
            public void logWarning(String warn) {
              SimplyVotifier.this.getLogger().warning(warn);
            }

            @Override
            public void logSevere(String msg) {
              SimplyVotifier.this.getLogger().severe(msg);
            }

            @Override
            public void log(String msg) {
              SimplyVotifier.this.getLogger().info(msg);
            }

            @Override
            public String getVersion() {
              return SimplyVotifier.this.getDescription().getVersion();
            }

            @Override
            public KeyPair getKeyPair() {
              return instance.getKeyPair();
            }

            @Override
            public void debug(Exception e) {
              SimplyVotifier.this.getSLF4JLogger().error("Error in Votifier", e);
            }

            @Override
            public void debug(String debug) {
              SimplyVotifier.this.getSLF4JLogger().info(debug);
            }

            @Override
            public void callEvent(Vote vote) {
              if (isFolia()) {
                Bukkit.getAsyncScheduler()
                    .runNow(
                        SimplyVotifier.this,
                        scheduledTask -> {
                          Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(vote));
                        });
              } else {
                Bukkit.getScheduler()
                    .runTask(
                        SimplyVotifier.this,
                        () ->
                            Bukkit.getServer()
                                .getPluginManager()
                                .callEvent(new VotifierEvent(vote)));
              }
            }
          };
      this.voteReceiver.start();

      this.getLogger().info("Votifier enabled.");
    } catch (Exception e) {
      this.getSLF4JLogger().error("Error in Votifier", e);
    }
  }

  @Override
  public void onDisable() {
    // Interrupt the vote receiver.
    if (this.voteReceiver != null) {
      this.voteReceiver.shutdown();
    }
    this.getLogger().info("Votifier disabled.");
  }

  public static boolean isFolia() {
    try {
      Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
