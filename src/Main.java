
import com.sun.jmx.snmp.SnmpString;
import com.sun.org.apache.regexp.internal.RE;
import javafx.util.converter.TimeStringConverter;

import javax.smartcardio.ResponseAPDU;
import java.sql.*;
import java.util.Calendar;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private final String url = "jdbc:postgresql://localhost/postgres";
    private final String user = "postgres";
    private final String password = "Sa13466431100";
    private static Connection conn = null;
    private static String activeUser= null;


    /*ACCESS*/
    public Timestamp getLastseen(String phone){
        String SQL = "SELECT * FROM \"user\" WHERE phone = ?";
        Timestamp lastseen = null;
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)){
            pstmt.setString(1, phone);

            ResultSet rs = pstmt.executeQuery();
            rs.next();
            lastseen = rs.getTimestamp("lastseen");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return lastseen;
    }

    public String getChannelInfo(int id, String attribute){
        String SQL = "SELECT * FROM channel WHERE id = ?";
        String result = null;
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)){
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            rs.next();
            result = rs.getString(attribute);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return result;
    }

    public String getGroupInfo(int id, String attribute){
        String SQL = "SELECT * FROM conversation WHERE id = ?";
        String result = null;
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)){
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            rs.next();
            result = rs.getString(attribute);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return result;
    }

    public boolean hasCC(String phone, int id){
        String SQL = "SELECT conversation_id AS cc_id FROM participant WHERE user_id = ?"
                + "UNION "
                + "SELECT channel_id AS cc_id FROM member WHERE user_id = ?";
        boolean result = false;
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)){
            pstmt.setString(1, phone);
            pstmt.setString(2, phone);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                if(id == rs.getInt("cc_id"))
                    result = true;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return result;
    }

    public boolean userExists(String phone){
        String SQL = "SELECT * FROM \"user\" WHERE phone = ?";
        boolean result = false;
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)){
            pstmt.setString(1, phone);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                result = true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return result;
    }

    public boolean gpChExists(int id, String cg){
        String SQL = null;
        if(cg.equals("channel"))
            SQL = "SELECT id FROM channel WHERE id = ?";
        else if(cg.equals("group"))
            SQL = "SELECT id FROM conversation WHERE id = ?";
        boolean result = false;
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)){
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                result = true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return result;
    }

    public boolean linkExists(String link){
        String SQL = "SELECT id FROM conversation WHERE link = ?" +
                " UNION" +
                " SELECT id FROM channel WHERE link = ?";
        boolean result = false;
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)){
            pstmt.setString(1, link);
            pstmt.setString(2, link);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                result = true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return result;
    }

    public int generateId(){

        String SQL="SELECT max(con.id) as id" +
                "   FROM conversation con";
        int id=1;
        try(Statement stmt=conn.createStatement()) {
            ResultSet rs=stmt.executeQuery(SQL);
            if(rs.next())
                id=rs.getInt("id")+1;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }

    public Integer getConversationId(String user1,String user2){

        String SQL="SELECT con.id" +
                "   FROM conversation con,participant p1,participant p2" +
                "   WHERE p1.user_id=? AND p2.user_id=?" +
                "   AND p1.conversation_id=p2.conversation_id" +
                "   AND p1.conversation_id=con.id AND con.type='single'";

        Integer conId = null;
        try(PreparedStatement pstmt=conn.prepareStatement(SQL)){
            pstmt.setString(1,user1);
            pstmt.setString(2,user2);
            ResultSet rs=pstmt.executeQuery();
            if(rs.next())
                conId=rs.getInt("id");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conId;
    }


    /*USER*/
    public void createUser(String phone) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String SQL = "INSERT INTO \"user\"(phone, name, lastseen, isactive, verfication_code, password, bio, self_destruct)"
                + " VALUES (?, '', ?, FALSE, ?, '', '', null)";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)){
            pstmt.setString(1, phone);
            pstmt.setTimestamp(2, timestamp);
            Random rand = new Random();
            pstmt.setString(3, String.format("%04d", rand.nextInt(10000)));
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void setName(String phone, String name) {
        String SQL = "UPDATE \"user\" "
                + "SET name = ? "
                + "WHERE phone = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)){
            pstmt.setString(1, name);
            pstmt.setString(2, phone);

            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void setBio(String phone, String bio) {
        String SQL = "UPDATE \"user\" "
                + "SET bio = ? "
                + "WHERE phone = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)){
            pstmt.setString(1, bio);
            pstmt.setString(2, phone);

            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void setSelfDestruct(String phone, int sd) {
        String SQL = "UPDATE \"user\" "
                + "SET self_destruct = ? "
                + "WHERE phone = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)){
            Timestamp lastseen = getLastseen(phone);
            Calendar cal = Calendar.getInstance();
            cal.setTime(lastseen);

            //Set Self Destruct Timestamp
            switch(sd){
                case 0:
                    pstmt.setString(1, null);
                    break;
                case 1:
                    cal.add(Calendar.MONTH, 1);
                    pstmt.setTimestamp(1, new Timestamp(cal.getTime().getTime()));
                    break;
                case 2:
                    cal.add(Calendar.MONTH, 3);
                    pstmt.setTimestamp(1, new Timestamp(cal.getTime().getTime()));
                    break;
                case 3:
                    cal.add(Calendar.MONTH, 6);
                    pstmt.setTimestamp(1, new Timestamp(cal.getTime().getTime()));
            }

            //Set Logged In Uer ID
            pstmt.setString(2, phone);

            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void blockUser(String blocker, String blocked){
        Timestamp time=new Timestamp(System.currentTimeMillis());
        String SQL = "INSERT INTO block_list(blocker_id, blocked_id, date)"
                + "VALUES(?, ?, ?)";
        try(PreparedStatement pstmt=conn.prepareStatement(SQL)){
            pstmt.setString(1, blocker);
            pstmt.setString(2, blocked);
            pstmt.setTimestamp(3, time);
            pstmt.executeUpdate();
        }catch (SQLException ex){
            System.out.println(ex.getMessage());
        }
    }

    public void unblockUser(String blocker, String blocked){
        Timestamp time=new Timestamp(System.currentTimeMillis());
        String SQL="SELECT * FROM block_list WHERE blocker_id = ? AND blocked_id = ?";
        try(PreparedStatement pstmt=conn.prepareStatement(SQL)){
            pstmt.setString(1, blocker);
            pstmt.setString(2, blocked);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                String SQL2 = "DELETE FROM block_list WHERE blocker_id=? AND blocked_id = ?";
                try(PreparedStatement pstmt2=conn.prepareStatement(SQL2)){
                    pstmt2.setString(1, blocker);
                    pstmt2.setString(2, blocked);

                } catch (SQLException ex){
                    System.out.println(ex.getMessage());
                }
            }
            else {
                System.out.println("ID is not found.");
            }
        } catch (SQLException ex){
            System.out.println(ex.getMessage());
        }
    }

    public void viewUserProfile(String phone){
        if(userExists(phone)) {
            String SQL = "SELECT u.name,u.bio" +
                    "   FROM \"user\" u " +
                    "   WHERE u.phone=?";

            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(SQL)) {
                pstmt.setString(1, phone);
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                StringBuilder sb = new StringBuilder();
                if(rs.getString("name").equals(""))
                    sb.append("Name:NULL ");
                else
                    sb.append("Name:\"" + rs.getString("name") + "\"");
                if(rs.getString("bio").equals(""))
                    sb.append(" Bio:NULL");
                else
                    sb.append(" Bio:\"" + rs.getString("bio") + "\"");
                System.out.println(sb.toString());
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        else {
            System.out.println("ID is not found.");
        }
    }

    public void setPassword(String phone, String pass){
        String SQL = "UPDATE \"user\" "
                + "SET password = ? "
                + "WHERE phone = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)){
            pstmt.setString(1, pass);
            pstmt.setString(2, phone);

            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void sendMessage(String sender, String phoneNumber,String message){

        Timestamp time=new Timestamp(System.currentTimeMillis());
        Integer conId = getConversationId(sender, phoneNumber);
        int conversationId = 0;
        if(conId == null){
            String SQL="INSERT into \"conversation\"(title,id,link, creator_id, type, updated_at)  values"+
                    "(null,?,null,?,'single',?)";
            int newID=generateId();
            try(PreparedStatement pstmt=conn.prepareStatement(SQL)){
                pstmt.setInt(1,newID);
                pstmt.setString(2, sender);
                pstmt.setTimestamp(3,time);
                pstmt.executeUpdate();
            }catch (SQLException ex){
                System.out.println(ex.getMessage());
            }
            String SQL1="INSERT into \"participant\"(user_id, conversation_id, lastvisit) values"+
                    "(?,?,?)";
            String SQL2="INSERT into \"participant\"(user_id, conversation_id, lastvisit) values"+
                    "(?,?,null)";
            try(PreparedStatement pstmt=conn.prepareStatement(SQL1);
                PreparedStatement pstmt1=conn.prepareStatement(SQL2)){
                pstmt.setString(1,sender);
                pstmt.setInt(2,newID);
                pstmt.setTimestamp(3,time);
                pstmt1.setString(1,phoneNumber);
                pstmt1.setInt(2,newID);
                pstmt.executeUpdate();
                pstmt1.executeUpdate();
            }catch (SQLException ex){
                System.out.println(ex.getMessage());
            }
        }
        else {
            String SQL = "UPDATE participant SET lastvisit = ? WHERE user_id = ? AND conversation_id = ?";
            String SQL1="SELECT con.id" +
                    "   FROM conversation con,participant p1,participant p2" +
                    "   WHERE p1.user_id=? AND p2.user_id=?" +
                    "   AND p1.conversation_id=p2.conversation_id" +
                    "   AND p1.conversation_id=con.id AND con.type='single'";
            try(PreparedStatement pstmt1=conn.prepareStatement(SQL1);
                PreparedStatement pstmt=conn.prepareStatement(SQL)){
                pstmt1.setString(1, sender);
                pstmt1.setString(2, phoneNumber);
                ResultSet rs = pstmt1.executeQuery();
                rs.next();
                conversationId = rs.getInt(1);
                pstmt.setTimestamp(1, time);
                pstmt.setString(2, sender);
                pstmt.setInt(3, conversationId);
                pstmt.executeUpdate();
            }
            catch (SQLException e){
                System.out.println(e.getMessage());
            }
        }

        String SQL="INSERT INTO \"message\"(sender_id, conversation_id, body, created_at) "+
                "values (?,?,?,?)";

        try(PreparedStatement pstmt=conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS)){
            pstmt.setString(1,sender);
            pstmt.setInt(2,conversationId);
            pstmt.setString(3,message);
            pstmt.setTimestamp(4,time);
            pstmt.executeUpdate();
        }catch (SQLException ex){
            System.out.println(ex.getMessage());
        }
    }

    public void viewChat(String phone, String user){
        Integer conId = getConversationId(phone, user);
        if(conId != null){
            String SQL = "SELECT u.phone AS phone, u.name AS sender,m.body AS body,m.created_at AS created_at " +
                    "     FROM \"user\" u, message m " +
                    "     WHERE m.sender_id=u.phone AND m.conversation_id = ? " +
                    "     ORDER BY m.created_at";
            try(PreparedStatement pstmt = conn.prepareStatement(SQL)){
                pstmt.setInt(1, conId);
                ResultSet rs = pstmt.executeQuery();
                int counter = 0;
                while(rs.next() && counter<20){
                    StringBuilder sb = new StringBuilder();
                    if (rs.getString("phone").equals(phone))
                        sb.append("Sender:Me");
                    else if (!rs.getString("sender").equals(""))
                        sb.append("Sender:" + rs.getString("sender"));
                    else
                        sb.append("Sender:\"" + rs.getString("phone") + "\"");
                    sb.append(" Time:" + rs.getString("created_at") +
                            " Message:\"" + rs.getString("body") + "\"");
                    System.out.println(sb.toString());
                }
            }
            catch (SQLException e){
                System.out.println(e.getMessage());
            }
        }
        else {
            System.out.println("Access denied.");
        }
    }




    /*GROUP*/
    public void createGroup(String phone, int id, String title){
        if(!gpChExists(id, "group") && !gpChExists(id, "channel")) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String SQL = "INSERT INTO conversation(id, link, creator_id, type, updated_at, title)"
                    + " VALUES (?, null, ?, 'group', ?, ?)";
            try {
                PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, id);
                pstmt.setString(2, phone);
                pstmt.setTimestamp(3, timestamp);
                pstmt.setString(4, title);
                pstmt.executeUpdate();

                addViaId("-1", "group", id);
                addViaId(phone, "group", id);
                sendMessageToGroup("-1", id, "group created");
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        else {
            System.out.println("Access denied.");
        }
    }

    public void sendMessageToGroup(String phone, int id, String body){
        if(gpChExists(id, "group")) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String SQL = "INSERT INTO message(sender_id, conversation_id, body, created_at)"
                    + " VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
                if (hasCC(phone, id)) {
                    pstmt.setString(1, phone);
                    pstmt.setInt(2, id);
                    pstmt.setString(3, body);
                    pstmt.setTimestamp(4, timestamp);
                    pstmt.executeUpdate();
                } else {
                    System.out.println("Access denied.");
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }

            //Update lastvisit
            SQL = "UPDATE participant "
                    + "SET lastvisit = ? "
                    + "WHERE user_id = ? AND conversation_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(SQL)){
                pstmt.setTimestamp(1, timestamp);
                pstmt.setString(2, phone);
                pstmt.setInt(3, id);

                pstmt.executeUpdate();
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        else {
            System.out.println("ID is not found.");
        }
    }

    public void setGroupLink(String phone, int id, String link){
        if(gpChExists(id, "group")) {
            if(!linkExists(link)) {
                String SQL = "UPDATE conversation "
                        + "SET link = ? "
                        + "WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
                    if (phone.equals(getGroupInfo(id, "creator_id"))) {
                        pstmt.setString(1, link);
                        pstmt.setInt(2, id);
                    } else {
                        System.out.println("Access denied.");
                    }

                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            else {
                System.out.println("Access denied.");
            }
        }
        else {
            System.out.println("ID is not found.");
        }
    }

    public void viewGroupProfile(String phone, int id){
        if(gpChExists(id, "group")) {
            if(hasCC(phone, id)) {
                String viewProfile = "SELECT u.name,gp.title,gp.link,gp.creator_id,count(p.user_id) AS MembersCount" +
                        " FROM conversation gp,participant p, \"user\" u" +
                        " WHERE gp.id=? AND p.conversation_id=? AND u.phone=gp.creator_id" +
                        " GROUP BY gp.title, u.name, gp.link, gp.creator_id";

                String viewMembers = "SELECT u.name,u.phone, u.isactive" +
                        " FROM \"user\" u,participant p" +
                        " WHERE p.user_id=u.phone AND p.conversation_id=?" +
                        " ORDER BY u.lastseen DESC";


                try (PreparedStatement pstmt = conn.prepareStatement(viewProfile);
                     PreparedStatement pstmt1 = conn.prepareStatement(viewMembers)) {
                    pstmt.setInt(1, id);
                    pstmt.setInt(2, id);
                    pstmt1.setInt(1, id);
                    ResultSet rs = pstmt.executeQuery();
                    ResultSet rs1 = pstmt1.executeQuery();
                    rs.next();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Name:\"" + rs.getString("title") + "\"\tMembersCount:\"" +
                            Integer.toString(rs.getInt("MembersCount")-1) + "\"\tLink:\"" + rs.getString("link")
                            + "\"\tCreator:\"");
                    if (rs.getString("name").equals(""))
                        sb.append(rs.getString("creator_id") + "\"");
                    else
                        sb.append(rs.getString("name") + "\" ");

                    System.out.println(sb.toString());
                    sb.setLength(0);
                    sb.append("Members:");
                    while (rs1.next()) {
                        if(!rs1.getString("name").equals("System")) {
                            String name = rs1.getString("name");
                            if (!name.equals(""))
                                sb.append("\"" + rs1.getString("name") + "\", ");
                            else if (name.equals(""))
                                sb.append("\"" + rs1.getString("phone") + "\", ");
                        }
                    }
                    sb.delete(sb.length()-2, sb.length());

                    System.out.println(sb.toString());
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            else {
                System.out.println("Access denied.");
            }
        }
        else {
            System.out.println("ID is not found.");
        }
    }



    /*CHANNEL*/
    public void createChannel(String phone, int id,String name){
        if(!gpChExists(id, "channel") && !gpChExists(id, "group")) {
            Timestamp time = new Timestamp(System.currentTimeMillis());
            String SQL = "INSERT INTO channel(id, name, creator_id, link, updated_at)" +
                    "values(?, ?, ?, null, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, phone);
                pstmt.setTimestamp(4, time);
                pstmt.executeUpdate();

                addViaId("-1", "channel", id);
                addViaId(phone, "channel", id);
                sendMessageToChannel("-1", id, "channel created");
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        else {
            System.out.println("Access denied.");
        }
    }

    public void setCHannelLink(String phone, int id, String link){
        if(gpChExists(id, "channel")) {
            if(!linkExists(link)) {
                String SQL = "UPDATE channel "
                        + "SET link = ? "
                        + "WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
                    if (phone.equals(getChannelInfo(id, "creator_id"))) {
                        pstmt.setString(1, link);
                        pstmt.setInt(2, id);
                    } else {
                        System.out.println("Access denied.");
                    }

                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            else {
                System.out.println("Access denied.");
            }
        }
        else {
            System.out.println("ID is not found.");
        }
    }

    public void sendMessageToChannel(String sender, int id, String body){
        if(gpChExists(id, "channel")) {
            if (sender.equals(getChannelInfo(id, "creator_id")) || sender.equals("-1")) {
                Timestamp time = new Timestamp(System.currentTimeMillis());
                String SQL = "INSERT into broadcast(sender_id, channel_id, body, created_at) " +
                        "values(?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, sender);
                    pstmt.setInt(2, id);
                    pstmt.setString(3, body);
                    pstmt.setTimestamp(4, time);
                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            else {
                System.out.println("Access denied.");
            }
        }
        else {
            System.out.println("ID is not found.");
        }
    }

    public void viewChannelProfile(String phone, int id){
        if(gpChExists(id, "channel")) {
            if(hasCC(phone, id)) {
                String viewChannelProfile = "SELECT ch.name AS chName,ch.link,ch.creator_id,u.name,count(m.user_id) AS MembersCount" +
                        " FROM channel ch,member m, \"user\" u" +
                        " WHERE ch.id=? AND m.channel_id=? AND u.phone=ch.creator_id" +
                        " GROUP BY ch.id, u.name";

                String viewMembers = "SELECT u.name,u.phone, u.isactive" +
                        " FROM \"user\" u,member m" +
                        " WHERE m.user_id=u.phone AND m.channel_id=?" +
                        " ORDER BY u.lastseen DESC";

                try (PreparedStatement pstmt = conn.prepareStatement(viewChannelProfile)) {
                    pstmt.setInt(1, id);
                    pstmt.setInt(2, id);
                    ResultSet rs = pstmt.executeQuery();
                    rs.next();
                    PreparedStatement pstmt2 = conn.prepareStatement(viewMembers);
                    pstmt2.setInt(1, id);
                    ResultSet rs2 = pstmt2.executeQuery();
                    StringBuilder sb = new StringBuilder();
                    sb.append("Name:\"" + rs.getString("chName") + "\"\tMembersCount:\"" +
                            Integer.toString(rs.getInt("MembersCount") - 1) + "\"\tLink:");
                    if (rs.getString("link") == null)
                        sb.append(rs.getString("link"));
                    else
                        sb.append("\"" + rs.getString("link") + "\"");
                    System.out.println(sb.toString());
                    sb.setLength(0);
                    if (getChannelInfo(id, "creator_id").equals(phone)) {
                        sb.append("Creator:");
                        if (rs.getString("name").equals(""))
                            sb.append("\"" + rs.getString("creator_id") + "\"");
                        else
                            sb.append(rs.getString("name"));
                        System.out.println(sb.toString());
                        sb.setLength(0);
                        sb.append("Members:");
                        while (rs2.next()){
                            if (!rs2.getString("name").equals("System")) {
                                if (!rs2.getString("name").equals(""))
                                    sb.append("\"" + rs2.getString("name") + "\", ");
                                else if (rs2.getString("name").equalsIgnoreCase(""))
                                    sb.append("\"" + rs2.getString("phone") + "\", ");
                            }
                        }
                        sb.delete(sb.length() - 2, sb.length());
                        System.out.println(sb.toString());
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
            else {
                System.out.println("Access denied.");
            }
        }
        else {
            System.out.println("ID is not found.");
        }
    }




    /*SYSTEM*/
    public void login (String phone){
        Timestamp time = new Timestamp(System.currentTimeMillis());
        if(userExists(phone)) {
            String SQL = "SELECT password FROM \"user\" WHERE phone = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
                pstmt.setString(1, phone);
                ResultSet rs = pstmt.executeQuery();
                boolean flag = true;
                rs.next();
                if(!rs.getString("password").equals("")) {
                    System.out.println("Enter your password:");
                    Scanner scanner = new Scanner(System.in);
                    String pass = scanner.nextLine();
                    if(!pass.equals(rs.getString("password"))){
                        System.out.println("Access denied.");
                        flag = false;
                    }
                }
                if(flag) {
                    activeUser = phone;
                    SQL = "UPDATE \"user\" "
                            + "SET isactive = TRUE "
                            + "WHERE phone = ?";
                    String SQL1 = "UPDATE \"user\" SET lastseen = ? WHERE phone = ?";
                    try (PreparedStatement pstmt2 = conn.prepareStatement(SQL);
                         PreparedStatement pstmt3 = conn.prepareStatement(SQL1)) {
                        pstmt2.setString(1, phone);
                        pstmt2.executeUpdate();
                        pstmt3.setTimestamp(1, time);
                        pstmt3.setString(2, phone);
                        pstmt3.executeUpdate();
                    } catch (SQLException ex) {
                        System.out.println(ex.getMessage());
                    }
                }

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        else {
            System.out.println("ID is not found.");
        }
    }

    public void logout(String phone){
        Timestamp time = new Timestamp(System.currentTimeMillis());
        activeUser = null;
        String SQL = "UPDATE \"user\" "
                + "SET isactive = FALSE "
                + "WHERE phone = ?";
        String SQL1 = "UPDATE \"user\" SET lastseen = ? WHERE phone = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL);
             PreparedStatement pstmt2 = conn.prepareStatement(SQL1)){
            pstmt.setString(1, phone);
            pstmt.executeUpdate();
            pstmt2.setTimestamp(1, time);
            pstmt2.setString(2, phone);
            pstmt2.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void addViaId(String phone, String cg, int id){
        if(gpChExists(id, cg)) {
            if(!hasCC(phone, id)) {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String SQL = null;
                if (cg.equals("channel"))
                    SQL = "INSERT INTO member(user_id, channel_id, lastvisit)"
                            + " VALUES (?, ?, NULL)";
                else if (cg.equals("group"))
                    SQL = "INSERT INTO participant(user_id, conversation_id, lastvisit)"
                            + " VALUES (?, ?, NULL)";
                try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
                    pstmt.setString(1, phone);
                    pstmt.setInt(2, id);
                    pstmt.executeUpdate();
                    if(cg.equals("group")) {
                        String SQL2 = "SELECT u.name FROM \"user\" u, conversation c  WHERE phone = ? AND c.id=? " +
                                "AND c.creator_id<>?";
                        PreparedStatement pstmt2 = conn.prepareStatement(SQL2);
                        pstmt2.setString(1, phone);
                        pstmt2.setInt(2, id);
                        pstmt2.setString(3, phone);
                        ResultSet rs = pstmt2.executeQuery();
                        if(rs.next()) {
                            if (!rs.getString("name").equals("") && !phone.equals("-1"))
                                sendMessageToGroup("-1", id, rs.getString("name") + " joined group");
                            else if (!phone.equals("-1"))
                                sendMessageToGroup("-1", id, phone + " joined group");
                        }
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            else {
                System.out.println("Access denied.");
            }
        }
        else {
            System.out.println("ID is not found.");
        }
    }

    public void addViaLink(String phone, String link){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String SQL = new String();
        String SQL2 = new String();
        SQL = "SELECT id, creator_id FROM conversation WHERE link = ?";
        try(PreparedStatement pstmt=conn.prepareStatement(SQL)){
            pstmt.setString(1, link);
            ResultSet rs=pstmt.executeQuery();
            if(rs.next()){
                addViaId(phone, "group", rs.getInt("id"));

                String SQL3 = "SELECT u.name FROM \"user\" u, conversation c  WHERE phone = ? AND c.id=? " +
                        "AND c.creator_id<>?";;
                PreparedStatement pstmt2 = conn.prepareStatement(SQL3);
                pstmt2.setString(1, phone);
                pstmt2.setInt(2, rs.getInt("id"));
                pstmt2.setString(3, rs.getString("creator_id"));
                ResultSet rs2 = pstmt2.executeQuery();
                if(rs.next()) {
                    if (!rs.getString("name").equals("")&& !phone.equals("-1"))
                        sendMessageToGroup("-1", rs.getInt("id"), rs2.getString("name") + " joined group");
                    else if (!phone.equals("-1"))
                        sendMessageToGroup("-1", rs.getInt("id"), phone + " joined group");
                }
            }
            else {
                SQL = "SELECT id FROM channel WHERE link = ?";
                PreparedStatement pstmt2=conn.prepareStatement(SQL);
                pstmt2.setString(1, link);
                ResultSet rs2=pstmt2.executeQuery();
                if(rs2.next()){
                    addViaId(phone, "channel", rs2.getInt("id"));
                }
                else {
                    System.out.println("Access denied.");
                }
            }
        } catch (SQLException ex){
            System.out.println(ex.getMessage());
        }
    }

    public void remove(String phone, String cg, int id){
        if(gpChExists(id, cg)) {
            String SQL = null;
            if(hasCC(phone, id)) {
                if (cg.equals("channel"))
                    SQL = "DELETE FROM member WHERE user_id = ? AND channel_id = ?";
                else if (cg.equals("group"))
                    SQL = "DELETE FROM participant WHERE user_id = ? AND conversation_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
                    pstmt.setString(1, phone);
                    pstmt.setInt(2, id);
                    pstmt.executeUpdate();

                    String SQL2 = "SELECT u.name FROM \"user\" u WHERE phone = ?";
                    PreparedStatement pstmt2 = conn.prepareStatement(SQL2);
                    pstmt2.setString(1, phone);
                    ResultSet rs = pstmt2.executeQuery();
                    rs.next();
                    if (!rs.getString("name").equals("") && !phone.equals("-1"))
                        sendMessageToGroup("-1", id, rs.getString("name") + " left group");
                    else if (!phone.equals("-1"))
                        sendMessageToGroup("-1", id, phone + " left group");
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            else {
                System.out.println("Access denied.");
            }
        }
        else {
            System.out.println("ID is not found.");
        }
    }

    public void viewCG(String phone, int id, String cg){
        if(hasCC(phone, id)) {
            String SQL = new String();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            if (cg.equals("group"))
                SQL = "SELECT u.phone AS phone, u.name AS sender,m.body AS body,m.created_at AS created_at " +
                        "FROM \"user\" u, message m " +
                        "WHERE m.sender_id=u.phone AND m.conversation_id = ? " +
                        "ORDER BY m.created_at";
            else if (cg.equals("channel"))
                SQL = "SELECT u.phone AS phone, u.name AS sender,b.body AS body,b.created_at AS created_at " +
                        "FROM \"user\" u,broadcast b " +
                        "WHERE b.sender_id=u.phone AND b.channel_id = ? " +
                        "ORDER BY b.created_at";

            try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                int counter = 0;
                while (rs.next() && counter < 20) {
                    StringBuilder sb = new StringBuilder();
                    if (rs.getString("phone").equals(phone))
                        sb.append("Sender:Me");
                    else if (!rs.getString("sender").equals(""))
                        sb.append("Sender:" + rs.getString("sender"));
                    else
                        sb.append("Sender:\"" + rs.getString("phone") + "\"");
                    sb.append(" Time:" + rs.getString("created_at") +
                            " Message:\"" + rs.getString("body") + "\"");
                    System.out.println(sb.toString());
                }

                SQL = "UPDATE participant SET lastvisit = ? WHERE user_id = ?";
                PreparedStatement pstmt2 = conn.prepareStatement(SQL);
                pstmt2.setTimestamp(1, timestamp);
                pstmt2.setString(2, phone);
                pstmt2.executeUpdate();
                SQL = "UPDATE member SET lastvisit = ? WHERE user_id = ?";
                pstmt2 = conn.prepareStatement(SQL);
                pstmt2.setTimestamp(1, timestamp);
                pstmt2.setString(2, phone);
                pstmt2.executeUpdate();
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        else{
            System.out.println("Access denied.");
        }
    }

    public void countUnread(String phone){

        String SQL="SELECT COUNT(messages.id) AS unread" +
                "   FROM (SELECT msg.id AS id" +
                "         FROM message msg,participant p" +
                "         WHERE (msg.created_at>p.lastvisit OR p.lastvisit IS NULL) AND msg.conversation_id=p.conversation_id" +
                "         AND p.user_id=?) AS messages";

        try(PreparedStatement pstmt=conn.prepareStatement(SQL)) {
            pstmt.setString(1,phone);
            ResultSet rs=pstmt.executeQuery();
            rs.next();
            System.out.println(rs.getString("unread"));
        }catch (SQLException ex){
            System.out.println(ex.getMessage());
        }
    }

    public String addToHome(ResultSet rs,String chatType){
        StringBuilder sb=new StringBuilder();

        if(chatType.equals("single")){
            sb.append("chat with ");
            try {
                if (!rs.getString("name").equalsIgnoreCase(""))
                    sb.append(rs.getString("name"));
                else
                    sb.append(rs.getString("id"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else if(chatType.equals("group")){
            try {
                sb.append("group " + rs.getString("title"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else if(chatType.equals("channel")){
            try {
                sb.append("channel " + rs.getString("title"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            sb.append(", unread_count="+rs.getInt("notif")+"\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return String.valueOf(sb);

    }

    public void home(String phone) {

        String SQL = "SELECT con.title as title,con.id,con.updated_at as date, (SELECT count(msg.id)" +
                "                                               FROM message msg" +
                "                                               WHERE msg.conversation_id=con.id" +
                "                                               AND (msg.created_at>p.lastvisit " +
                "                                               OR p.lastvisit IS NULL)) AS notif" +
                "   FROM conversation con, participant p" +
                "   WHERE p.user_id=?" +
                "   AND con.type='group' " +
                "   AND p.conversation_id=con.id" +
                "   GROUP BY con.id, p.lastvisit" +
                "   UNION" +
                "   SELECT ch.name as title,ch.id,ch.updated_at as date, (SELECT count(b.id)" +
                "                                        FROM broadcast b" +
                "                                        WHERE b.channel_id=ch.id" +
                "                                        AND (b.created_at>m.lastvisit" +
                "                                        OR m.lastvisit IS NULL )) AS notif" +
                "   FROM channel ch, member m" +
                "   WHERE m.user_id=?" +
                "   AND m.channel_id=ch.id" +
                "   group by ch.id, m.lastvisit" +
                "   ORDER BY date DESC";

        String SQL1= "SELECT u.name,p.user_id as id,con.updated_at as date,(SELECT count(msg.id)" +
                "                                            FROM message msg" +
                "                                            WHERE msg.conversation_id=con.id" +
                "                                            AND (msg.created_at>p1.lastvisit" +
                "                                            OR p1.lastvisit IS NULL)) AS notif" +
                "   FROM conversation con,participant p,participant p1,\"user\" u" +
                "   WHERE p1.user_id=? AND p1.conversation_id=con.id" +
                "   AND p.conversation_id=con.id AND u.phone=p.user_id" +
                "   AND con.type='single' AND p1.user_id<>p.user_id" +
                "   GROUP BY u.name,p.user_id,con.id,p1.lastvisit" +
                "   ORDER BY date DESC";


        try (PreparedStatement pstmt = conn.prepareStatement(SQL);
             PreparedStatement pstmt1=conn.prepareStatement(SQL1)) {
            pstmt.setString(1, phone);
            pstmt.setString(2, phone);
            pstmt1.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            ResultSet rs1=pstmt1.executeQuery();

            StringBuilder sb = new StringBuilder();
            sb.append("");
            boolean hasnextuser;
            boolean hasnextChGp;
            int counter=0;
            while (counter<8){
                hasnextuser=rs1.next();
                hasnextChGp=rs.next();
                if(hasnextChGp || hasnextuser) {
                    if (hasnextuser && hasnextChGp) {
                        if (rs1.getTimestamp("date").after(rs.getTimestamp("date"))) {
                            sb.append(addToHome(rs1, "single"));
                            counter++;
                            if (gpChExists(rs.getInt("id"), "group"))
                                sb.append(addToHome(rs,"group"));
                            else if (gpChExists(rs.getInt("id"), "channel"))
                                sb.append(addToHome(rs,"channel"));

                            counter++;
                            continue;

                        } else{
                            if (gpChExists(rs.getInt("id"), "group"))
                                sb.append(addToHome(rs,"group"));
                            else if (gpChExists(rs.getInt("id"), "channel"))
                                sb.append(addToHome(rs,"channel"));

                            counter++;
                            sb.append(addToHome(rs1, "single"));
                            counter++;
                            continue;
                        }
                    } else if(hasnextuser){
                        sb.append(addToHome(rs1, "single"));
                        counter++;
                        continue;

                    }else if(hasnextChGp){

                        if (gpChExists(rs.getInt("id"), "group"))
                            sb.append(addToHome(rs,"group"));
                        else if (gpChExists(rs.getInt("id"), "channel"))
                            sb.append(addToHome(rs,"channel"));
                        counter++;
                        continue;
                    }
                }else
                    break;
            }
            if(sb.toString().equals("")) {
                System.out.println("access denied");
            }
            else {
                System.out.println(sb);
            }
        }catch(SQLException ex){
            System.out.println(ex.getMessage());
        }
    }


    /*MAIN*/
    public static void main(String[] args) {
        Main main = new Main();
        try {
            conn = main.connect();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        Scanner scanner = new Scanner(System.in);
        String SQL = "SELECT count(u.phone) AS user_count FROM \"user\" u";
        try(PreparedStatement pstmt = conn.prepareStatement(SQL)){
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            if(rs.getInt("user_count") == 0){
                main.createUser("-1");
                main.login("-1");
                main.setName("-1", "System");
                main.setPassword("-1", "admin");
                main.logout("-1");
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        while(true){
            String[] input = scanner.nextLine().trim().split(" ");
            StringBuilder sb = new StringBuilder();
            switch (input[0]) {
                case "login":
                    main.login(input[1]);
                    break;
                case "logout":
                    main.logout(activeUser);
                    break;
                case "set_bio":
                    if (activeUser != null) {
                        for (int i = 1; i < input.length; i++)
                            sb.append(input[i] + " ");
                        sb.delete(sb.length()-1, sb.length());
                        main.setBio(activeUser, sb.toString());
                    } else
                        System.out.println("Access denied.");
                    break;
                case "set_self_destruct":
                    if (activeUser != null)
                        main.setSelfDestruct(activeUser, Integer.parseInt(input[1]));
                    else
                        System.out.println("Access denied.");
                    break;
                case "set_password":
                    if (activeUser != null) {
                        for (int i = 1; i < input.length; i++)
                            sb.append(input[i] + " ");
                        sb.delete(sb.length()-1, sb.length());
                        main.setPassword(activeUser, sb.toString());
                    }
                    else
                        System.out.println("Access denied.");
                    break;
                case "block_user":
                    if (activeUser != null)
                        main.blockUser(activeUser, input[1]);
                    else
                        System.out.println("Access denied.");
                    break;
                case "unblock_user":
                    if (activeUser != null)
                        main.unblockUser(activeUser, input[1]);
                    else
                        System.out.println("Access denied.");
                    break;
                case "create_user":
                    main.createUser(input[1]);
                    break;
                case "set_name":
                    if (activeUser != null) {
                        for (int i = 1; i < input.length; i++)
                            sb.append(input[i] + " ");
                        sb.delete(sb.length()-1, sb.length());
                        main.setName(activeUser, sb.toString());
                    }
                    else
                        System.out.println("Access denied.");
                    break;
                case "send_message":
                    if(activeUser!=null) {
                        for (int i = 2; i < input.length; i++)
                            sb.append(input[i] + " ");
                        sb.delete(sb.length()-1, sb.length());
                        main.sendMessage(activeUser, input[1], sb.toString());
                    }
                    break;
                case "create_channel":
                    if(activeUser != null)
                        main.createChannel(activeUser, Integer.parseInt(input[1]), input[2]);
                    else
                        System.out.println("Access denied.");
                    break;
                case "send_message_channel":
                    if(activeUser != null) {
                        for (int i = 2; i < input.length; i++)
                            sb.append(input[i] + " ");
                        sb.delete(sb.length()-1, sb.length());
                        main.sendMessageToChannel(activeUser, Integer.parseInt(input[1]), sb.toString());
                    }
                    else
                        System.out.println("Access denied.");
                    break;
                case "create_group":
                    if(activeUser != null)
                        main.createGroup(activeUser, Integer.parseInt(input[1]), input[2]);
                    else
                        System.out.println("Access denied.");
                    break;
                case "send_message_group":
                    if(activeUser != null) {
                        for (int i = 2; i < input.length; i++)
                            sb.append(input[i] + " ");
                        sb.delete(sb.length()-1, sb.length());
                        main.sendMessageToGroup(activeUser, Integer.parseInt(input[1]), sb.toString());
                    }
                    else
                        System.out.println("Access denied.");
                    break;
                case "set_channel_link":
                    if(activeUser != null)
                        main.setCHannelLink(activeUser, Integer.parseInt(input[1]), input[2]);
                    else
                        System.out.println("Access denied.");
                    break;
                case "set_group_link":
                    if(activeUser != null)
                        main.setGroupLink(activeUser, Integer.parseInt(input[1]), input[2]);
                    else
                        System.out.println("Access denied.");
                    break;
                case "join_channel":
                    if(activeUser != null)
                        main.addViaId(activeUser, "channel", Integer.parseInt(input[1]));
                    else
                        System.out.println("Access denied.");
                    break;
                case "join_link":
                    if(activeUser != null)
                        main.addViaLink(activeUser, input[1]);
                    else
                        System.out.println("Access denied.");
                    break;
                case "leave_channel":
                    if(activeUser != null)
                        main.remove(activeUser, "channel", Integer.parseInt(input[1]));
                    else
                        System.out.println("Access denied.");
                    break;
                case "leave_group":
                    if(activeUser != null)
                        main.remove(activeUser, "group", Integer.parseInt(input[1]));
                    else
                        System.out.println("Access denied.");
                    break;
                case "home":
                    if(activeUser!=null)
                        main.home(activeUser);
                    break;
                case "view_chat":
                    if(activeUser!=null)
                        main.viewChat(activeUser, input[1]);
                    break;
                case "view_channel":
                    if(activeUser != null)
                        main.viewCG(activeUser, Integer.parseInt(input[1]), "channel");
                    else
                        System.out.println("Access denied.");
                    break;
                case "view_group":
                    if(activeUser != null)
                        main.viewCG(activeUser, Integer.parseInt(input[1]), "group");
                    else
                        System.out.println("Access denied.");
                    break;
                case "view_user_profile":
                    main.viewUserProfile(input[1]);
                    break;
                case "view_channel_profile":
                    if(activeUser != null)
                        main.viewChannelProfile(activeUser, Integer.parseInt(input[1]));
                    else
                        System.out.println("Access denied.");
                    break;
                case "view_group_profile":
                    if(activeUser != null)
                        main.viewGroupProfile(activeUser, Integer.parseInt(input[1]));
                    else
                        System.out.println("Access denied.");
                    break;
                case "count_unread":
                    if(activeUser != null)
                        main.countUnread(activeUser);
                    else
                        System.out.println("Access denied.");
                    break;
                default:
                    System.out.println("Access denied.");

            }

        }
    }
}

