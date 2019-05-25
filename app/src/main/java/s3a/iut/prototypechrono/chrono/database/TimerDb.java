package s3a.iut.prototypechrono.chrono.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TimerDb {

    static final String TABLE_SESSION = "Session";

    static final String TABLE_EXERCICE = "Exercice";

    static final String TABLE_SERIE = "Serie";

    private SQLiteDatabase db;

    private TimerBaseSQLite timerDb;

    public TimerDb(Context context){
        this.timerDb = new TimerBaseSQLite(context);
    }

    public void openForWrite(){
        this.db = timerDb.getWritableDatabase();
    }

    public void openForRead(){
        this.db = timerDb.getReadableDatabase();
    }

    public void close(){
        this.db.close();
    }

    public SQLiteDatabase getDb(){
        return this.db;
    }

    /* SESSION */

    public Cursor getSession() {
        openForRead();
        String req = "SELECT * FROM "+TABLE_SESSION+" ORDER BY nom_session ASC";
        Cursor data = db.rawQuery(req,null);
        return data;
    }

    public boolean hasSession(String nom_session) {
        openForRead();
        String req = "SELECT * FROM "+TABLE_SESSION+" WHERE nom_session = '"+nom_session+"'";
        Cursor data = db.rawQuery(req,null);
        return data.getCount()>0;
    }

    public void insertSession(String nom){
        openForWrite();
        db.execSQL("INSERT INTO Session(nom_session) VALUES ('"+nom+"')");
    }

    public void updateSession(String old_nom_session, String new_nom_session){
        openForRead();
        String req = "UPDATE Session SET nom_session = '"+new_nom_session+"' WHERE nom_session = '"+old_nom_session+"'";
        db.execSQL(req);
    }

    public void deleteSession(String nom) {
        openForWrite();
        String req = "DELETE FROM "+TABLE_SESSION+" WHERE nom_session='"+nom+"'";
        db.execSQL(req);
    }

    /* EXERCICE */

    public void insertExercice(String nom, String nom_session ){
        openForWrite();
        nom="'"+nom+"'";
        nom_session="'"+nom_session+"'";
        String req = "INSERT INTO Exercice(nom,nom_session) VALUES ("+nom+","+nom_session+")";
        db.execSQL(req);
    }

    public void updateExercice(String nom, String nom_session){
        openForWrite();
        nom="'"+nom+"'";
        nom_session="'"+nom_session+"'";
        String req = "UPDATE Exercice SET nom = "+nom+" WHERE nom_session = "+nom_session;
        db.execSQL(req);
    }

    public Cursor getExercice(){
        openForRead();
        String req = "SELECT * FROM "+TABLE_EXERCICE;
        Cursor data = db.rawQuery(req,null);
        return data;
    }

    public Cursor getExercice(String nom_session){
        openForRead();
        nom_session="'"+nom_session+"'";
        String req = "SELECT * FROM "+TABLE_EXERCICE+" WHERE nom_session = "+nom_session+" ORDER BY id_exercice ASC";
        Cursor data = db.rawQuery(req,null);
        return data;
    }

    public void deleteExercice(String nom) {
        openForWrite();
        String req = "DELETE FROM "+TABLE_EXERCICE+" WHERE nom_session='"+nom+"'";
        db.execSQL(req);
    }

    /* SERIE */

    public void insertSerie(int min, int sec, int id_exercice){
        openForWrite();
        String req = "INSERT INTO "+TABLE_SERIE+" (min, sec, id_exercice) VALUES ("+min+","+sec+","+id_exercice+")";
        db.execSQL(req);
    }

    public void insertSerie(String min, String sec, int id_exercice){
        insertSerie(Integer.parseInt(min),Integer.parseInt(sec),id_exercice);
    }

    public void updateSerie(int min, int sec, int id_exercice){
        openForWrite();
        String req = "UPDATE Serie SET min ="+min+", sec ="+sec+" WHERE id_exercice ="+id_exercice;
        db.execSQL(req);
    }

    public void updateSerie(String min, String sec, int id_exercice){
        updateSerie(Integer.parseInt(min),Integer.parseInt(sec),id_exercice);
    }

    public Cursor getSerie(){
        openForRead();
        String req = "SELECT * FROM "+TABLE_SERIE;
        Cursor data = db.rawQuery(req,null);
        return data;
    }

    public Cursor getSerie(int id_exercice){
        openForRead();
        String req = "SELECT * FROM "+TABLE_SERIE+" WHERE id_exercice = "+id_exercice;
        Cursor data = db.rawQuery(req,null);
        return data;
    }

    public void deleteSerie(String nom) {
        openForWrite();
        String req = "DELETE FROM "+TABLE_SERIE+" WHERE nom_session='"+nom+"'";
        db.execSQL(req);
    }




}
