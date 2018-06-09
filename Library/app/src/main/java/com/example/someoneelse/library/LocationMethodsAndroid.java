/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.someoneelse.library;

import android.content.Context;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


import java.util.*;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.InvalidKeySpecException;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;


/* NOTES
 ** Cifrar dentro disto 1x em vez de 3x generateFinalMessage
 ** Multi Uni cast talvez seja a melhor abordagem para grupos - mandar a chave ao outro gajo?? - só há um contacto, as mensagens têm identificador de grupo, vais buscar a chave do gajo x mas do chat y
 ** INTENT 
 */
public final class LocationMethodsAndroid  {

    private static final String ALGORITHM = "AES";
    private final SQLite db ;

    public LocationMethodsAndroid(Context context) {
        db=new SQLite(context);
    }

    /**
     * returns a list of suggested locations based on the input address
     *
     * @param address comma-separated String
     * @return list of suggested locations
     */
    public List<String> getLocationSuggestion(String address) {
        List<String> list = Arrays.asList(address.split(","));
        List<String> result = new ArrayList<>();
        String current = "";
        Collections.reverse(list);
        for(String s : list){
            current=s+current;
            result.add(current);
        }
        return result;
    }

    /**
     * returns a GeoHash of the input coordinates in a Base32 representation
     *
     * @param coords the coordinates to be used in the GeoHashing process
     * @param precision the precision of the GeoHash, from 1 to 12
     * @return Base32 String representation of the GeoHash
     */
    public static String getLocationSuggestion(double[] coords, int precision) {
        return GeoHash.withCharacterPrecision(coords[0], coords[1], precision).toBase32();
    }
    
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Map<String,ArrayList<Double>> getTimeDistanceSuggestions(double[] coords, double speed) throws  ClassNotFoundException{  //how to calculate speed/time in different situations???????
        Map<String,double[]> mp = getAllMeetingCoords();
        Map<String,double[]> anchors = getAllZones();
        Map<String,ArrayList<Double>> results = new HashMap<>();
        for (Map.Entry<String, double[]> entry : mp.entrySet())
        {
            double dist = distanceToCoords(coords,entry.getValue());
            results.put(entry.getKey(), new ArrayList<>(Arrays.asList(dist, timeToCoords(dist, speed))));
        }
        for (Map.Entry<String, double[]> entry : anchors.entrySet())
        {
            double dist = distanceToCoords(coords,entry.getValue());
            results.put(entry.getKey(), new ArrayList<>(Arrays.asList(dist, timeToCoords(dist, speed))));
        }
        return results;
    }

    /**
     * returns a message in JSON format containing a ciphered message and the
     * userId of the sender
     *
     * @param msg
     * @param senderId
     * @return JSON format message
     */
    private static String generateFinalMessage(String msg, int senderId) {
        return "{\"type\":\"final_msg\",\"id\":" + senderId + ",\"msg\":\"" + msg + "\"}"; //cifrar aqui
    }

    /**
     * returns a JSON message which contains a ciphered message with the
     * location (String) a user wants to share and the sender's Id
     *
     * @param address String representation of the location to be shared
     * @param senderId ID of the sender
     * @param destId ID of the recipient
     * @return JSON format message
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String generateAddressMessage(String address, int senderId, int destId) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException,  ShortBufferException, ClassNotFoundException {
        String toSend = "{\"type\":\"share_address\",\"address\":\"";

        if (address != null) {
            toSend += address + "\",\"";
        }

        toSend += "user_id\":" + senderId + "}\n";

        SecretKey sk = getSecretKey(destId); 
        String toSendEncrypted = cmiyc.Security.cipher(sk, toSend.getBytes());
        return generateFinalMessage(toSendEncrypted, senderId);
    }

    /**
     * returns a JSON message which contains a ciphered message with the
     * location (GeoHash) a user wants to share and the sender's Id
     *
     * @param hash GeoHash representation of the location to be shared
     * @param senderId ID of the sender
     * @param destId ID of the recipient
     * @return JSON format message
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String generateGeohashMessage(String hash, int senderId, int destId) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,  IOException, ShortBufferException, ClassNotFoundException {
        String toSend = "{\"type\":\"share_geohash\",\"geohash\":\"";

        if (hash != null) {
            toSend += hash + "\",\"";
        }

        toSend += "user_id\":" + senderId + "}\n";

        SecretKey sk = getSecretKey(destId);
        String toSendEncrypted = cmiyc.Security.cipher(sk, toSend.getBytes());
        return generateFinalMessage(toSendEncrypted, senderId);
    }

    /**
     * returns a JSON message which contains a ciphered message with the
     * coordinates of the meeting point and the sender's Id
     *
     * @param dstCoords array with the coordinates: [lat, lon]
     * @param name name of the Meeting Point
     * @param senderId ID of the sender
     * @param destId ID of the recipient
     * @return JSON format message
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String generateMeetingMessage(double[] dstCoords, String name, int senderId, int destId) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IllegalBlockSizeException,  IOException, ShortBufferException, ClassNotFoundException {
        String toSend = "{\"type\":\"share_meeting\",\"lat\":" + dstCoords[0] + ",\"lon\":" + dstCoords[1] + ",\"name\":\"";

        if (name != null) {
            toSend += name + "\",\"";
        }

        toSend += "user_id\":" + senderId + "}\n";

        SecretKey sk = getSecretKey(destId);
        String toSendEncrypted = cmiyc.Security.cipher(sk, toSend.getBytes());
        return generateFinalMessage(toSendEncrypted, senderId);
    }

    /**
     * returns a JSON message which contains a ciphered message with the time a
     * user is from a given location, and the sender's Id
     *
     * @param time time away from given location
     * @param name name of the location
     * @param senderId ID of the sender
     * @param destId ID of the recipient
     * @return JSON format message
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String generateTimeMessage(double time, String name, int senderId, int destId) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IllegalBlockSizeException, IOException, ShortBufferException, ClassNotFoundException {
        String toSend = "{\"type\":\"share_time\",\"time\":" + time + ",\"name\":\"";

        if (name != null) {
            toSend += name + "\",\"";
        }

        toSend += "user_id\":" + senderId + "}\n";

        SecretKey sk = getSecretKey(destId);
        String toSendEncrypted = cmiyc.Security.cipher(sk, toSend.getBytes());
        return generateFinalMessage(toSendEncrypted, senderId);
    }

    /**
     * returns a JSON message which contains a ciphered message with the
     * distance a user is away from a given location and the sender's Id
     *
     * @param dist distance away from given location
     * @param name name of location
     * @param senderId ID of the sender
     * @param destId ID of the recipient
     * @return JSON format message
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String generateDistanceMessage(double dist, String name, int senderId, int destId) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IllegalBlockSizeException,  IOException, ShortBufferException, ClassNotFoundException {
        String toSend = "{\"type\":\"share_dist\",\"dist\":" + dist + ",\"name\":\"";

        if (name != null) {
            toSend += name + "\",\"";
        }

        toSend += "user_id\":" + senderId + "}\n";

        SecretKey sk = getSecretKey(destId);
        String toSendEncrypted = cmiyc.Security.cipher(sk, toSend.getBytes());
        return generateFinalMessage(toSendEncrypted, senderId);
    }

    /**
     * calculate distance from one given point to another
     *
     * @param srcCoords array with the origin coordinates (from): [lat, lon]
     * @param dstCoords array with the destination coordinates (to): [lat, lon]
     * @return distance in metres
     */
    public static double distanceToCoords(double[] srcCoords, double[] dstCoords) {
        int radius = 6371000;

        double srcLat = Math.toRadians(srcCoords[0]);
        double dstLat = Math.toRadians(dstCoords[0]);

        double latDif = Math.toRadians((dstCoords[0] - srcCoords[0]));
        double lonDif = Math.toRadians((dstCoords[1] - srcCoords[1]));

        double a = Math.sin(latDif / 2) * Math.sin(latDif / 2)
                + Math.cos(srcLat) * Math.cos(dstLat) * Math.sin(lonDif / 2) * Math.sin(lonDif / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double dist = radius * c;

        return round(dist,2);
    }

    /**
     * calculate time away from a given location, given the distance to said
     * location and the user's velocity
     *
     * @param distance distance to location
     * @param velocity user's velocity
     * @return time in seconds
     */
    public static double timeToCoords(double distance, double velocity) {
        return round((distance / velocity)/60,2);
    }

    /**
     * verify whether given coordinates are in a given Geohash
     *
     * @param srcCoords user's coordinates: [lat, lon]
     * @param hash Geohash
     * @return true or false
     */
    public static boolean inGeohash(double[] srcCoords, String hash) {
        GeoHash dstArea = GeoHash.fromGeohashString(hash);
        return dstArea.contains(new WGS84Point(srcCoords[0], srcCoords[1]));
    }

    /**
     * verify whether given coordinates are in a user-defined zone
     *
     * @param srcCoords user's coordinates: [lat, lon]
     * @param dstCoords centre coordinates of the user-defined zone: [lat, lon]
     * @param radius distance away from dstCoords which defines the zone
     * @return true or false
     */
    public static boolean inZone(double[] srcCoords, double[] dstCoords, double radius) {
        return distanceToCoords(srcCoords, dstCoords) <= radius;
    }

    /**
     * write coordinates and name of a meeting point
     *
     * @param name name of meeting point
     * @param coords coordinates of meeting point: [lat, lon]
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveMeetingCoords(String name, double[] coords) throws ClassNotFoundException {
        db.insert("meetingPoints", "name,lat,lon", "'"+name+"',"+coords[0]+","+coords[1]);
        System.out.println(name + " @ " + Arrays.toString(coords));
    }

    /**
     * write coordinates, radius and name of user-defined zone
     *
     * @param name name of user-defined zone
     * @param coords coordinates of user-defined zone: [lat, lon]
     * @param radius radius of user-defined zone
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveZone(String name, double[] coords, double radius) throws ClassNotFoundException {
        db.insert("anchors", "name,lat,lon,radius", "'" + name + "'," + coords[0] + "," + coords[1] + "," + radius);
    }

    /**
     * write user's ID and symmetric key
     *
     * @param userId ID of user
     * @param secretKey user's secret key used to cipher communications
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void saveSecretKey(int userId, String secretKey, String name, String number) throws ClassNotFoundException {
        db.insert("contacts", "id,key,name,number","'"+userId+"','"+secretKey+"','"+name+"','"+number+"'");
    }
    
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void deleteSecretKey(String userId) throws ClassNotFoundException {
        db.delete("contacts", "WHERE id='"+userId+"'");
    }
    
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void deleteMeetingCoords(String name) throws ClassNotFoundException {
        db.delete("meetingPoints", "WHERE name=" + name);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void deleteZone(String name) throws ClassNotFoundException {
        db.delete("anchors", "WHERE name=" + name);
    }

    /**
     * parse received messages to JSON and return the deciphered inner message
     *
     * @param msg received message, containing the ciphered inner message
     * @return JSON format string
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String recvAndDecipher(String msg) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,  ClassNotFoundException {
        // recebe a mensagem cifrada e retorna a mesma decifrada
        
        JsonObject cipheredMsg = new JsonParser().parse(msg).getAsJsonObject();
        
        SecretKey secretKey = getSecretKey(cipheredMsg.get("id").getAsInt());
        
        byte[] msg_decrypted = cmiyc.Security.decipher(secretKey, cipheredMsg.get("msg").getAsString());
        return new String(msg_decrypted);
    }

    /**
     * read user's secret key
     *
     * @param id
     * @return Base64 representation of secret key
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private  SecretKey getSecretKey(int id) throws  ClassNotFoundException {
       byte[] decodedKey = Base64.decode(db.selectKey("WHERE id=" + id),Base64.NO_WRAP);
       return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    /**
     * read meeting point coordinates
     *
     * @param name name of the meeting point
     * @return array containing meeting point coordinates: [lat, lon]
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Map<String, double[]> getMeetingCoords(String name) throws  ClassNotFoundException {
        Map<String, double[]> res = db.selectMeetingCoords("name,lat,lon", "meetingPoints", "WHERE name='" + name + "'");
        return res;
    }
    
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Map<String, double[]> getAllMeetingCoords() throws  ClassNotFoundException {
    	Map<String, double[]> res = db.selectAllMeetingCoords();
    	return res;
    }

    /**
     * read user-defined zone coordinates and radius
     *
     * @param name name of the user-defined zone
     * @return array with user-defined zone coordinates and radius: [lat, lon,
     * radius]
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Map<String, double[]> getZone(String name) throws  ClassNotFoundException {
    	Map<String, double[]> res = db.selectZone("WHERE name='" + name + "'");
        return res;
    }
    
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Map<String, double[]> getAllZones() throws  ClassNotFoundException {
    	Map<String, double[]> res = db.selectAllZones();
        return res;
    }
    
    
     // esta funcao é so pra ser chamada do lado do android para criar os contactos -- temporario
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void createConcts() throws ClassNotFoundException, InvalidKeySpecException, InvalidAlgorithmParameterException{

        cmiyc.KeyExchange alice = new cmiyc.KeyExchange("Alice");
        cmiyc.KeyExchange bob = new cmiyc.KeyExchange("Bob");

        try {
            alice.generateDhKeyPair();
            alice.init();

            String encodedAlicePk = alice.getEncodedPublicKey();

            bob.generateDhPublicKeyFromOther(encodedAlicePk);
            bob.generateDhKeyPairFromOther();
            bob.init();

            String encodedBobPk = bob.getEncodedPublicKey();

            alice.generateDhPublicKeyFromOther(encodedBobPk);
            alice.firstPhase();

            bob.firstPhase();

            alice.generateSecret();

            bob.generateSecret();

            alice.generateSecretKey(alice.getSharedSecret());

            bob.generateSecretKey(bob.getSharedSecret());

            saveSecretKey(0, Base64.encodeToString(alice.getSecretKey().getEncoded(),Base64.NO_WRAP),"alice","932283938");
            saveSecretKey(1, Base64.encodeToString(bob.getSecretKey().getEncoded(),Base64.NO_WRAP),"bob","924066253");

            
        } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
        } catch (InvalidKeyException e) {
                e.printStackTrace();
        } catch (IllegalStateException e) {
                e.printStackTrace();
        }
    }
    
    private static final class SQLite extends SQLiteOpenHelper {
        // Database Version
        private static final int DATABASE_VERSION = 1;

        // Database Name
        private static final String DATABASE_NAME = "cmiyc_db";
        
        public SQLite(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }



        // Creating Tables
        @Override
        public void onCreate(SQLiteDatabase db) {

            // create notes table
            createTables(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }



        private void createTables(SQLiteDatabase db) {
            // SQL statement for creating a new table
            String sql1 = "CREATE TABLE IF NOT EXISTS meetingPoints (\n"
                    + "	name text PRIMARY KEY,\n"
                    + "	lat real NOT NULL,\n"
                    + "	lon real NOT NULL\n"
                    + ");";
            String sql2 = "CREATE TABLE IF NOT EXISTS anchors (\n"
                    + "	name text PRIMARY KEY,\n"
                    + "	lat real NOT NULL,\n"
                    + "	lon real NOT NULL,\n"
                    + "	radius real NOT NULL\n"
                    + ");";
            String sql3 = "CREATE TABLE IF NOT EXISTS contacts (\n"
                    + "	id text PRIMARY KEY,\n"
                    + "	name text NOT NULL,\n"
                    + "	number text NOT NULL,\n"
                    + " key text NOT NULL\n"
                    + ");";

            // create a new table
           db.execSQL(sql1);
           db.execSQL(sql2);
           db.execSQL(sql3);
           
        }

        /**
         * Insert a new row into the warehouses table
         *
         */
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void insert(String table, String columns, String row) {
            String sql = "INSERT INTO " + table + "(" + columns + ") VALUES(" + row + ")";

            try ( // get writable database as we want to write data
                    SQLiteDatabase db = this.getWritableDatabase()) {
                db.execSQL(sql);
            }
        }

        /**
         * select all rows in the warehouses table
         */
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public Map<String, double[]> selectMeetingCoords(String columns, String table, String whereClause) {

            String sql = "SELECT " + columns + " FROM " + table + " " + whereClause;
            HashMap<String, double[]> meetingPoints = new HashMap<>();
            Cursor cursor = null;
            try ( // get writable database as we want to write data
                  SQLiteDatabase db = this.getWritableDatabase()) {
                cursor = db.rawQuery(sql, null);
                // looping through all rows and adding to list
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    meetingPoints.put(cursor.getString(cursor.getColumnIndex("name")), new double[]{ cursor.getDouble(cursor.getColumnIndex("lat")), cursor.getDouble(cursor.getColumnIndex("lon"))});
                    cursor.moveToNext();
                }
                
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            cursor.close();

            return meetingPoints;

        }
        
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public Map<String, double[]> selectAllMeetingCoords()  {
            String sql = "SELECT * FROM meetingPoints";
            HashMap<String, double[]> meetingPoints = new HashMap<>();
            Cursor cursor = null;

            try (SQLiteDatabase db = this.getWritableDatabase()){
                cursor = db.rawQuery(sql, null);
                while(cursor.moveToNext()){
                    meetingPoints.put(cursor.getString(cursor.getColumnIndex("name")), new double[] {cursor.getDouble(cursor.getColumnIndex("lat")), cursor.getDouble(cursor.getColumnIndex("lon"))});
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            cursor.close();
            return meetingPoints;
        }
        
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public Map<String, double[]> selectZone(String whereClause)  {
            String sql = "SELECT * FROM anchors " + whereClause;
            HashMap<String, double[]> meetingPoints = new HashMap<>();
            Cursor cursor;
            try (SQLiteDatabase db = this.getWritableDatabase()){
                cursor = db.rawQuery(sql, null);
                while(cursor.moveToNext()){
                	meetingPoints.put(cursor.getString(cursor.getColumnIndex("name")), new double[] {cursor.getDouble(cursor.getColumnIndex("lat")), cursor.getDouble(cursor.getColumnIndex("lon")), cursor.getDouble(cursor.getColumnIndex("radius"))});
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return meetingPoints;
        }
        
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public String selectKey(String whereClause)  {
            String sql = "SELECT * FROM contacts " + whereClause;
            Cursor cursor;
            String key = "";
            try (SQLiteDatabase db = this.getWritableDatabase()){
                cursor = db.rawQuery(sql, null);
                while(cursor.moveToNext()){
                    key = cursor.getString(cursor.getColumnIndex("key"));
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return key;
        }
        
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public Map<String, double[]> selectAllZones() {
            String sql = "SELECT * FROM anchors ";
            HashMap<String, double[]> meetingPoints = new HashMap<>();
            Cursor cursor;
            try (SQLiteDatabase db = this.getWritableDatabase()){
                cursor = db.rawQuery(sql, null);
                while(cursor.moveToNext()){
                    meetingPoints.put(cursor.getString(cursor.getColumnIndex("name")), new double[] {cursor.getDouble(cursor.getColumnIndex("lat")), cursor.getDouble(cursor.getColumnIndex("lon")), cursor.getDouble(cursor.getColumnIndex("radius"))});

                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return meetingPoints;
        }
        
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void delete(String table, String whereClause)  {
            String sql = "DELETE FROM" + table + " " + whereClause;
            try(SQLiteDatabase db = this.getWritableDatabase()) {
                db.execSQL(sql);
            } catch (Exception e) {
                    System.out.println(e.getMessage());
            }
        }
    }
    
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal db = new BigDecimal(value);
        db = db.setScale(places, RoundingMode.HALF_UP);
        return db.doubleValue();
    }
    
   
    
    /**
     *
     * @param args
     */
    /*
    public static void main(String[] args) throws Exception, IOException{
        saveMeetingCoords("DETI", new double[] {40.633091, -8.659547});
        saveMeetingCoords("Casa", new double[] {40.637652, -8.656053});
//        
        saveZone("UA", new double [] {40.633001, -8.658980}, 100);
        ////////////////////////////////////////////////
        KeyExchange alice = new KeyExchange("Alice");
        KeyExchange bob = new KeyExchange("Bob");

        try {
            alice.generateDhKeyPair();
            alice.init();

            String encodedAlicePk = alice.getEncodedPublicKey();

            bob.generateDhPublicKeyFromOther(encodedAlicePk);
            bob.generateDhKeyPairFromOther();
            bob.init();

            String encodedBobPk = bob.getEncodedPublicKey();

            alice.generateDhPublicKeyFromOther(encodedBobPk);
            alice.firstPhase();

            bob.firstPhase();

            alice.generateSecret();

            bob.generateSecret();

            alice.generateSecretKey(alice.getSharedSecret());

            bob.generateSecretKey(bob.getSharedSecret());

            saveSecretKey(0, Base64.encodeToString(alice.getSecretKey().getEncoded(),Base64.NO_WRAP),"alice","932283938");
            saveSecretKey(1, Base64.encodeToString(bob.getSecretKey().getEncoded(),Base64.NO_WRAP),"bob","924066253");

            
        } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
        } catch (InvalidKeyException e) {
                e.printStackTrace();
        } catch (IllegalStateException e) {
                e.printStackTrace();
        }
        
        ////////////////////////////////////////////////
        
        System.out.println("/////////////////////////////////////////////////////");
        System.out.println("Sugestões de enedereço para a localização atual:");
        for(String address : new LocationMethods().getLocationSuggestion("Universidade de Aveiro, 3810-193 Aveiro, Portugal")){
            System.out.println(address);
        }
        System.out.println("--GeoHash--");
        System.out.println(getLocationSuggestion(new double[] {40.633091, -8.659547},5));
        System.out.println("");
        System.out.println("Distancia e tempo para todos os pontos de encontro e ancoras(Casa do Simões):");
        Map<String,ArrayList<Double>> mapa = getTimeDistanceSuggestions(new double[] {40.626968, -8.651703}, 1);
        for (String s : mapa.keySet()){
            System.out.println(s+"\t"+mapa.get(s).toString());
        }
        System.out.println("");
        System.out.println("Partilha de endereço:");
        String outMessage = generateAddressMessage("Universidade de Aveiro 3810-193 Aveiro",0,0);
        System.out.println("Mensagem enviada: "+outMessage);
        String inMessage = recvAndDecipher(outMessage);
        System.out.println("Mensagem Recebida: "+inMessage);
        System.out.println("");
        System.out.println("Partilha de localização (geohash): ");
        String outMessage1 = generateGeohashMessage("ez1z4",0,0);
        System.out.println("Mensagem enviada: "+outMessage1);
        String inMessage1 = recvAndDecipher(outMessage1);
        System.out.println("Mensagem Recebida: "+inMessage1);
        System.out.println("");
        System.out.println("Partilha de ponto de encontro: ");
        String outMessage2 = generateMeetingMessage(new double[] {40.642279, -8.655324}, "Praça do Peixe", 0, 0);
        System.out.println("Mensagem enviada: "+outMessage2);
        String inMessage2 = recvAndDecipher(outMessage2);
        System.out.println("Mensagem Recebida: "+inMessage2);
        System.out.println("");
        System.out.println("Partilha de tempo até local: ");
        String outMessage3 = generateTimeMessage(5, "Praça do Peixe", 0, 0);
        System.out.println("Mensagem enviada: "+outMessage3);
        String inMessage3= recvAndDecipher(outMessage3);
        System.out.println("Mensagem Recebida: "+inMessage3);
        System.out.println("");
        System.out.println("Partilha de distancia até ao local: ");
        String outMessage4 = generateDistanceMessage(1000, "Praça do Peixe", 0, 0);
        System.out.println("Mensagem enviada: "+outMessage4);
        String inMessage4= recvAndDecipher(outMessage4);
        System.out.println("Mensagem Recebida: "+inMessage4);
        System.out.println("");
        System.out.println("Praça está no GeoHash da UA: "+inGeohash(new double [] {40.642279, -8.655324}, "ez1z4"));
        System.out.println("");
        System.out.println("Praça está na zona UA: "+inZone(new double [] {40.642279, -8.655324},new double [] {40.633001, -8.658980}, 1000));
        
    }*/
}
