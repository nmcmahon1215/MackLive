package com.macklive.objects;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.macklive.exceptions.EntityMismatchException;
import com.macklive.storage.DataManager;

public class Game implements IBusinessObject{

    private Date created;
    private Team team1;
    private Team team2;
    private int team1goals;
    private int team2goals;
    private int team1sog;
    private int team2sog;
    private boolean team1penalty;
    private boolean team2penalty;
    private String time;
    private int period;
    
    /**
     * Constructor
     * @param t1 First team in the game
     * @param t2 Second team in the game
     */
    public Game (Team t1, Team t2){
        this.team1 = t1;
        this.team2 = t2;
        this.team1goals = 0;
        this.team1sog = 0;
        this.team2goals = 0;
        this.team2sog = 0;
        this.time = "";
        this.period = 1;
        this.created = new Date();
    }
    
    /**
     * Constructor from 
     * @param e
     * @throws EntityMismatchException
     */
    public Game (Entity e) throws EntityMismatchException{
        this.loadEntity(e);
    }
    
    @Override
    public Entity getEntity() {
        Entity e = new Entity("Game");
        e.setProperty("Team1", team1.getKey());
        e.setProperty("Team2", team2.getKey());
        e.setProperty("T1Score", this.team1goals);
        e.setProperty("T2Score", this.team2goals);
        e.setProperty("T1SOG", this.team1sog);
        e.setProperty("T2SOG", this.team2sog);
        e.setProperty("T1Penalty", this.team1penalty);
        e.setProperty("T2Penalty", this.team2penalty);
        e.setProperty("Time", this.time);
        e.setProperty("Period", this.period);
        e.setProperty("Date", this.created);
        return e;
    }

    @Override
    public void loadEntity(Entity e) throws EntityMismatchException {
        if (e.getKind().equals("Game")){
            try {
                DataManager dstore = DataManager.getInstance();
                this.team1 = new Team(dstore.getEntityWithKey((Key)e.getProperty("Team1")));
                this.team2 = new Team(dstore.getEntityWithKey((Key)e.getProperty("Team2")));
                this.team1goals = ((Long) e.getProperty("T1Score")).intValue();
                this.team2goals = ((Long) e.getProperty("T2Score")).intValue();
                this.team1sog = ((Long) e.getProperty("T1SOG")).intValue();
                this.team2sog = ((Long) e.getProperty("T2SOG")).intValue();
                this.team1penalty = (boolean) e.getProperty("T1Penalty");
                this.team2penalty = (boolean) e.getProperty("T2Penalty");
                this.time = (String) e.getProperty("Time");
                this.period = ((Long) e.getProperty("Period")).intValue();
                this.created = (Date) e.getProperty("Date");
            } catch (EntityNotFoundException e1) {
                System.err.println("Could not find team!");
                e1.printStackTrace();
            }
        } else {
            throw new EntityMismatchException("Expected entity of type \"Team\","
                    + "but received \"" + e.getKind());
        }
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
        String result = sdf.format(this.created);
        result += " - ";
        result += this.team1.getAbbr();
        result += " - ";
        result += this.team2.getAbbr();
        return result;
    }

    public int getTeam1goals() {
        return team1goals;
    }



    public void setTeam1goals(int team1goals) {
        this.team1goals = team1goals;
    }



    public int getTeam2goals() {
        return team2goals;
    }



    public void setTeam2goals(int team2goals) {
        this.team2goals = team2goals;
    }



    public int getTeam1sog() {
        return team1sog;
    }



    public void setTeam1sog(int team1sog) {
        this.team1sog = team1sog;
    }



    public int getTeam2sog() {
        return team2sog;
    }



    public void setTeam2sog(int team2sog) {
        this.team2sog = team2sog;
    }



    public boolean isTeam1penalty() {
        return team1penalty;
    }



    public void setTeam1penalty(boolean team1penalty) {
        this.team1penalty = team1penalty;
    }



    public boolean isTeam2penalty() {
        return team2penalty;
    }



    public void setTeam2penalty(boolean team2penalty) {
        this.team2penalty = team2penalty;
    }



    public String getTime() {
        return time;
    }



    public void setTime(String time) {
        this.time = time;
    }



    public int getPeriod() {
        return period;
    }



    public void setPeriod(int period) {
        this.period = period;
    }



    public Team getTeam1() {
        return team1;
    }



    public Team getTeam2() {
        return team2;
    }

}
