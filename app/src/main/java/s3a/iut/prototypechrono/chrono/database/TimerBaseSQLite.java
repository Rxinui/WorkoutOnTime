package s3a.iut.prototypechrono.chrono.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TimerBaseSQLite extends SQLiteOpenHelper {

    private static final String SQL_CREATE_SESSION ="CREATE TABLE Session (nom_session TEXT PRIMARY KEY); ";

    private static final String SQL_CREATE_EXERCICE ="CREATE TABLE Exercice (id_exercice INTEGER PRIMARY KEY AUTOINCREMENT, nom TEXT, nom_session TEXT, " +
            "FOREIGN KEY (nom_session) REFERENCES Session(nom_session) ON DELETE CASCADE ON UPDATE CASCADE); ";

    private static final String SQL_CREATE_SERIE ="CREATE TABLE Serie (id_serie INTEGER PRIMARY KEY AUTOINCREMENT, min INTEGER, sec INTEGER, id_exercice INTEGER," +
            "FOREIGN KEY (id_exercice) REFERENCES Exercice(id_exercice) ON DELETE CASCADE ON UPDATE CASCADE); ";

    private static final String SQL_DELETE_SERIE ="DROP TABLE IF EXISTS Serie;";

    private static final String SQL_DELETE_SESSION ="DROP TABLE IF EXISTS Session;";

    private static final String SQL_DELETE_EXERCICE ="DROP TABLE IF EXISTS Exercice;";

    private static final String SQL_DELETE = SQL_DELETE_SESSION + SQL_DELETE_EXERCICE + SQL_DELETE_SERIE;

    private static final String BDD_NAME = "timer.db";

    private static final int VERSION = 1;

    public TimerBaseSQLite(Context context){
        super(context,BDD_NAME,null,VERSION);
    }


    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SESSION);
        db.execSQL(SQL_CREATE_EXERCICE);
        db.execSQL(SQL_CREATE_SERIE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE);
        onCreate(db);
    }
}
