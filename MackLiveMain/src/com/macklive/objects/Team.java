/**
 * Describes a team. Two teams belong to each game.
 */
package com.macklive.objects;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.macklive.exceptions.EntityMismatchException;

/**
 * Describes a team, which has a name, abbreviation, and logo image
 */
public class Team extends AbsBusinessObject implements IBusinessObject {

    private String name;
    private String abbr;
    private transient Blob logo;

    /**
     * Constructor for a team. This does not set the team's logo
     *
     * @param name The name of the team.
     * @param abbr The abbreviation to be used for the team.
     */
    public Team(String name, String abbr) {
        this.name = name;
        this.abbr = abbr;
    }

    /**
     * Constructor for a team. This also sets the team's logo.
     *
     * @param name The name of the team
     * @param abbr The abbreviation to be used for the team
     * @param logo The Blob representing the team's logo as a byte array.
     */
    public Team(String name, String abbr, Blob logo) {
        this(name, abbr);
        this.logo = logo;
    }

    /**
     * Creates a team based on the given Team Entity
     *
     * @param e A team entity used to create the team.
     * @throws EntityMismatchException If the entity is not a Team entity
     */
    public Team(Entity e) throws EntityMismatchException {
        this.loadEntity(e);
    }

    /**
     * @see com.macklive.objects.IBusinessObject#getEntity()
     */
    @Override
    public Entity getEntity() {
        Entity e;
        if (this.key != null) {
            e = new Entity(this.key);
        } else {
            e = new Entity("Team");
        }

        e.setProperty("Name", this.name);
        e.setUnindexedProperty("Abbr", this.abbr);
        e.setUnindexedProperty("Logo", this.logo);

        return e;
    }

    /**
     * Loads a team from an Entity
     *
     * @throws EntityMismatchException if an incorrect entity is passed
     * @see com.macklive.objects.IBusinessObject#loadEntity(com.google.appengine.api.datastore.Entity)
     */
    @Override
    public void loadEntity(Entity e) throws EntityMismatchException {
        if (e.getKind().equals("Team")) {

            this.name = (String) e.getProperty("Name");
            this.abbr = (String) e.getProperty("Abbr");
            this.logo = (Blob) e.getProperty("Logo");
            this.key = e.getKey();

        } else {
            throw new EntityMismatchException("Expected entity of type \"Team\","
                    + "but received \"" + e.getKind());
        }

    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbr() {
        return this.abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public Blob getLogo() {
        return this.logo;
    }

    public void setLogo(Blob logo) {
        this.logo = logo;
    }

    /**
     * Overrides the toString method to return JSON serialization of the team
     */
    @Override
    public String toString() {
        return this.toJSON();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Team team = (Team) o;

        if (!this.name.equals(team.name)) {
            return false;
        }
        if (!this.abbr.equals(team.abbr)) {
            return false;
        }
        return this.logo != null ? this.logo.equals(team.logo) : team.logo == null;

    }

    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + this.abbr.hashCode();
        result = 31 * result + (this.logo != null ? this.logo.hashCode() : 0);
        return result;
    }
}
