package Model.Database;

import Model.Entity.PlaylistTrack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PlaylistTrackDAO {

    private Connection connection;

    public PlaylistTrackDAO(){
        this.connection = DBConnector.getInstance().getConnection();
    }

    public void create(PlaylistTrack playlistTrack){
        PreparedStatement statement;

        try{
            statement = connection.prepareStatement("INSERT INTO Playlist_Track VALUES(default,?,?,default)");

            statement.setInt(1,playlistTrack.getPlaylist_id());
            statement.setInt(2,playlistTrack.getTrack_id());

            statement.executeUpdate();

        } catch (SQLException e){
            e.printStackTrace();
        }

        System.out.println("Playlist Track id:" + playlistTrack.getTrack_id() + " afegit a la db.");
    }

    public void update(PlaylistTrack playlistTrack){
        PreparedStatement statement;

        try{
            statement = connection.prepareStatement("UPDATE Playlist_Track AS PT\n" +
                    "JOIN Playlist P ON PT.id_playlist = P.id\n" +
                    "SET rating = ?\n" +
                    "WHERE P.id_user = ? AND PT.id_track = ?;");

            int user_id = findUserId(playlistTrack.getPlaylist_id());

            statement.setFloat(1,playlistTrack.getRating());
            statement.setInt(2,user_id);
            statement.setInt(3,playlistTrack.getTrack_id());
            statement.executeUpdate();

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void updateRating(int track_id){
        PreparedStatement statement;

        try{
            statement = connection.prepareStatement("SELECT AVG(x.rating)\n" +
                    "FROM (\n" +
                    "     SELECT PT.rating FROM Playlist_Track AS PT\n" +
                    "JOIN Playlist P ON PT.id_playlist = P.id\n" +
                    "JOIN User U ON P.id_user = U.id\n" +
                    "WHERE PT.rating <> -1 AND PT.id_track = ?\n" +
                    "GROUP BY U.id\n" +
                    "         )x;");

            statement.setInt(1, track_id);
            ResultSet rs = statement.executeQuery();
            rs.next();
            float avg_rating = rs.getFloat("AVG(x.rating)");
            System.out.println("AVERAGE RATING: " + avg_rating + " ID: " + track_id);

            PreparedStatement statement1;

            statement1 = connection.prepareStatement("UPDATE Track SET rating = ? WHERE id = ?;");

            statement1.setFloat(1,avg_rating);
            statement1.setInt(2,track_id);

            statement1.executeUpdate();

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public int findUserId(int playlist_id){
        PreparedStatement statement;

        try{
            statement = connection.prepareStatement("SELECT DISTINCT U.id FROM User AS U JOIN Playlist AS P ON P.id_user = U.id AND P.id = ?;");
            statement.setInt(1,playlist_id);
            ResultSet rs = statement.executeQuery();
            rs.next();

            return rs.getInt("id");
        } catch (SQLException e){
            e.printStackTrace();
        }
        return -1;
    }

}
