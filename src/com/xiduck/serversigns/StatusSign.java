package com.xiduck.serversigns;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.bukkit.Location;
import org.bukkit.block.Sign;

public class StatusSign {

    private final Location location;
    private final Sign sign;
    private final String name, ip;
    private final int port;

    public StatusSign(Location location, String name, String ip, int port) {
        this.location = location;
        this.sign = (Sign) location.getBlock().getState();
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getIP() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void update() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 1 * 1000);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            out.write(0xFE);

            StringBuilder str = new StringBuilder();

            int b;
            while ((b = in.read()) != -1) {
                if (b != 0 && b > 16 && b != 255 && b != 23 && b != 24) {
                    str.append((char) b);
                }
            }

            
            String[] data = str.toString().split("§");
            String motd = data[0].replace("&", "§");
            int onlinePlayers = Integer.valueOf(data[1]);
            int maxPlayers = Integer.valueOf(data[2]);
            String serverStatus = null;
            String ffaMap = null;
            String tdmMap = null;
            
            if(motd.contains("lobby") || motd.contains("Lobby")){
                serverStatus = "§5Lobby";
            }
            
            if(motd.contains("lobby") && onlinePlayers >= maxPlayers || motd.contains("Lobby") && onlinePlayers >= maxPlayers){
                serverStatus = "§4Full";
            }
            
            if(motd.contains("ingame") || motd.contains("In-Game")){
                serverStatus = "§8In-Game";
            }
            
            if(motd.contains("untildusk") || motd.contains("UntilDusk")){
                tdmMap = "§5UntilDusk";
            }
            
            if(motd.contains("hetake") || motd.contains("Hetake")){
                tdmMap = "§5Hetake";
            }
            
            if(motd.contains("mars") || motd.contains("Mars")){
                tdmMap = "§5Mars";
            }
            
            if(motd.contains("voting") && name.contains("tdm") || motd.contains("Voting") && name.contains("tdm")){
                tdmMap = "§5Voting";
            }
            
            sign.setLine(0, "§8[" + name.toUpperCase() + " - Join]");
            sign.setLine(1, tdmMap);
            sign.setLine(2, serverStatus);
            sign.setLine(3, "§8" +onlinePlayers+"/"+maxPlayers);
            
            if(name.equalsIgnoreCase("ffa")){
                sign.setLine(0, "§8===============");
                sign.setLine(1, "§2[FFA]");
                sign.setLine(2, "§aClick to join!");
                sign.setLine(3, "§8===============");
            }
            
            if(name.equalsIgnoreCase("hub")){
                sign.setLine(0, "§8===============");
                sign.setLine(1, "§9[Hub]");
                sign.setLine(2, "§aClick to join!");
                sign.setLine(3, "§8===============");
            }
        }catch (IOException | NumberFormatException | IndexOutOfBoundsException e) {

            sign.setLine(0, "§4===============");
            sign.setLine(1, "§4Restarting:");
            sign.setLine(2, "§4" + name.toUpperCase());
            sign.setLine(3, "§4===============");
        }

        sign.update();
    }

}