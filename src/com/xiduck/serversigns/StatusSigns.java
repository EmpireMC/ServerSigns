package com.xiduck.serversigns;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class StatusSigns extends JavaPlugin implements Listener {

    private ArrayList<StatusSign> signs;
    private final String prefix = "§e[§5Server§6Signs§e] ";

    @Override
    public void onEnable() {
        this.signs = new ArrayList<>();
        saveDefaultConfig();
        for (String str : getConfig().getKeys(false)) {
            ConfigurationSection s = getConfig().getConfigurationSection(str);

            ConfigurationSection l = s.getConfigurationSection("loc");
            World w = Bukkit.getServer().getWorld(l.getString("world"));
            double x = l.getDouble("x"), y = l.getDouble("y"), z = l.getDouble("z");
            Location loc = new Location(w, x, y, z);

            if (loc.getBlock() == null) {
                getConfig().set(str, null);
            } else {
                signs.add(new StatusSign(loc, s.getString("name"), s.getString("ip"), s.getInt("port")));
            }
        }
        
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (StatusSign s : signs) {
                    s.update();
                }
            }
        }, 0, 20);

        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = e.getClickedBlock();

        if (block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
        } else {
            Sign sign = (Sign) block.getState();
            if (sign.getLine(1).contains("Full")) {

            } else if (sign.getLine(2).contains("Restarting")) {
                e.getPlayer().sendMessage(prefix + "§cThis server's restarting. Try again in a few seconds.");
            } else {
                for (StatusSign s : signs) {
                    if (s.getLocation().equals(block.getLocation())) {
                        try {
                            Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

                            ByteArrayOutputStream b = new ByteArrayOutputStream();
                            DataOutputStream out = new DataOutputStream(b);

                            out.writeUTF("ConnectOther");
                            out.writeUTF(e.getPlayer().getName());
                            out.writeUTF(s.getName());

                            e.getPlayer().sendPluginMessage(this, "BungeeCord", b.toByteArray());
                        } catch (IOException ex) {
                            System.out.println("Countered an exception:");
                            System.out.println(ex);
                        }
                    }
                }

            }
        }

    }

    @Override
    @SuppressWarnings("depreciation")
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender.hasPermission("serversigns.admin")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can create status signs.");
                return true;
            }

            Player p = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("statussigns")) {
                if (args.length < 3) {
                    p.sendMessage(prefix + "§c/statussigns <ip> <port> <name>");
                    return true;
                }

                String ip = args[0];
                int port;
                String name = args[2];

                try {
                    port = Integer.valueOf(args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage(prefix + "§cThat port is not a number!");
                    return true;
                }

                Block block = p.getTargetBlock(null, 10);
                if (block == null) {
                    p.sendMessage(prefix + "§cYou're not looking at a sign!");
                    return true;
                }

                if (block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
                    p.sendMessage(prefix + "§cYou're not looking at a sign!");
                    return true;
                }

                StatusSign statusSign = new StatusSign(block.getLocation(), name, ip, port);
                signs.add(statusSign);
                save(statusSign);
            }
        } else {
            sender.sendMessage("BONK. NO PERMS. BONK.");
        }

        return true;
    }

    private void save(StatusSign sign) {
        int size = getConfig().getKeys(false).size() + 1;

        getConfig().set(size + ".loc.world", sign.getLocation().getWorld().getName());
        getConfig().set(size + ".loc.x", sign.getLocation().getX());
        getConfig().set(size + ".loc.y", sign.getLocation().getY());
        getConfig().set(size + ".loc.z", sign.getLocation().getZ());

        getConfig().set(size + ".name", sign.getName());
        getConfig().set(size + ".ip", sign.getIP());
        getConfig().set(size + ".port", sign.getPort());

        saveConfig();
    }

}
