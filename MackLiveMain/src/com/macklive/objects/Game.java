package com.macklive.objects;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.storage.DataManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Game extends AbsBusinessObject implements IBusinessObject {

    private Date created;
    private Date lastUpdated;
    private Team team1;
    private Team team2;
    private int team1goals;
    private int team2goals;
    private int team1sog;
    private int team2sog;
    private boolean team1pp;
    private boolean team2pp;
    private String time;
    private int period;
    private String name;
    private String link;
    private transient String ownerId;

    /**
     * Constructor
     *
     * @param t1 First team in the game
     * @param t2 Second team in the game
     */
    public Game(Team t1, Team t2) {
        this.team1 = t1;
        this.team2 = t2;
        this.team1goals = 0;
        this.team1sog = 0;
        this.team2goals = 0;
        this.team2sog = 0;
        this.time = "";
        this.period = 1;
        this.created = new Date();
        this.name = this.getName();
        this.lastUpdated = new Date();
        this.ownerId = "";
    }

    /**
     * Constructor from
     *
     * @param e
     * @throws EntityMismatchException
     */
    public Game(Entity e) throws EntityMismatchException {
        this.loadEntity(e);
        this.name = this.getName();
    }

    @Override
    public Entity getEntity() {
        Entity e;
        if (this.key != null) {
            e = new Entity(this.key);
        } else {
            e = new Entity("Game");
        }
        e.setUnindexedProperty("Team1", team1.getKey());
        e.setUnindexedProperty("Team2", team2.getKey());
        e.setUnindexedProperty("T1Score", this.team1goals);
        e.setUnindexedProperty("T2Score", this.team2goals);
        e.setUnindexedProperty("T1SOG", this.team1sog);
        e.setUnindexedProperty("T2SOG", this.team2sog);
        e.setUnindexedProperty("T1PP", this.team1pp);
        e.setUnindexedProperty("T2PP", this.team2pp);
        e.setUnindexedProperty("Time", this.time);
        e.setUnindexedProperty("Period", this.period);
        e.setProperty("Date", this.created);
        e.setProperty("LastUpdated", this.lastUpdated);
        e.setUnindexedProperty("Name", this.getName());
        e.setUnindexedProperty("Link", this.link);
        return e;
    }

    @Override
    public void loadEntity(Entity e) throws EntityMismatchException {
        if (e.getKind().equals("Game")) {
            try {
                DataManager dstore = DataManager.getInstance();
                this.team1 = new Team(dstore.getEntityWithKey((Key) e.getProperty("Team1")));
                this.team2 = new Team(dstore.getEntityWithKey((Key) e.getProperty("Team2")));
                this.team1goals = ((Number) e.getProperty("T1Score")).intValue();
                this.team2goals = ((Number) e.getProperty("T2Score")).intValue();
                this.team1sog = ((Number) e.getProperty("T1SOG")).intValue();
                this.team2sog = ((Number) e.getProperty("T2SOG")).intValue();
                this.team1pp = (boolean) e.getProperty("T1PP");
                this.team2pp = (boolean) e.getProperty("T2PP");
                this.time = (String) e.getProperty("Time");
                this.period = ((Number) e.getProperty("Period")).intValue();
                this.created = (Date) e.getProperty("Date");
                this.lastUpdated = (Date) e.getProperty("LastUpdated");
                this.key = e.getKey();
                this.ownerId = (String) e.getProperty("owner");
                this.link = (String) e.getProperty("Link");

            } catch (EntityNotFoundException e1) {
                System.err.println("Could not find team!");
                e1.printStackTrace();
            }
        } else {
            throw new EntityMismatchException("Expected entity of type \"Team\"," + "but received \"" + e.getKind());
        }
    }

    /**
     * Generates the name of the game based on the date it was created and the
     * teams playing in the game
     *
     * @return A string that represents the name of the game.
     */
    public String getName() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
        String result = sdf.format(this.created);
        result += " - ";
        result += this.team1.getAbbr();
        result += " - ";
        result += this.team2.getAbbr();
        return result;
    }

    /**
     * Overrides the toString method to produce a JSON version of this game
     * object.
     */
    @Override
    public String toString() {
        return this.toJSON();
    }

    public void setTeam1goals(int team1goals) {
        this.lastUpdated = new Date();
        this.team1goals = team1goals;
    }

    public void setTeam2goals(int team2goals) {
        this.lastUpdated = new Date();
        this.team2goals = team2goals;
    }

    public void setTeam1sog(int team1sog) {
        this.lastUpdated = new Date();
        this.team1sog = team1sog;
    }

    public void setTeam2sog(int team2sog) {
        this.lastUpdated = new Date();
        this.team2sog = team2sog;
    }

    public void setTeam1pp(boolean team1pp) {
        this.lastUpdated = new Date();
        this.team1pp = team1pp;
    }

    public void setTeam2pp(boolean team2pp) {
        this.lastUpdated = new Date();
        this.team2pp = team2pp;
    }

    public void setTime(String time) {
        this.lastUpdated = new Date();
        this.time = time;
    }

    public void setPeriod(int period) {
        this.lastUpdated = new Date();
        this.period = period;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public void setTeam1(Team team) {
        this.lastUpdated = new Date();
        this.team1 = team;
    }

    public void setTeam2(Team team) {
        this.lastUpdated = new Date();
        this.team2 = team;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
