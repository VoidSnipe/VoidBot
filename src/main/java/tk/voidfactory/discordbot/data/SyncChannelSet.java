package tk.voidfactory.discordbot.data;

import net.dv8tion.jda.core.entities.TextChannel;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class SyncChannelSet {
    private static Set<String> list = new HashSet<>();
    private static Connection data;
    public static void init(){
        synchronized (SyncChannelSet.class) {

            try {
                Class.forName("org.postgresql.Driver");
                URI dbUri = new URI(System.getenv("DATABASE_URL"));

                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

                data = DriverManager.getConnection(dbUrl, username, password);
                Statement s;
                s = data.createStatement();
                ResultSet res = s.executeQuery("SELECT id FROM channels;");
                while (res.next()) {
                    list.add(res.getString("id"));
                }
            } catch (ClassNotFoundException | SQLException | URISyntaxException e) {
                e.printStackTrace();
            }

        }
    }

    public static boolean get(TextChannel channel) {
        return list.contains(channel.getId());
    }

    public static void add(TextChannel channel) {
        synchronized (SyncChannelSet.class) {
            if (list.add(channel.getId())) try {
                data.createStatement().execute("INSERT INTO channels VALUES ('"+channel.getId()+"');");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void remove(TextChannel channel) {
        synchronized (SyncChannelSet.class) {
            if (list.remove(channel.getId())) try {
                data.createStatement().execute("DELETE FROM channels WHERE id = '"+channel.getId()+"';");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
