/**
 * 
 */
package com.macklive.objects;

import com.google.appengine.api.datastore.Entity;
import com.macklive.exceptions.EntityMismatchException;

/**
 * Describes a typical object. These objects can be stored in the data store
 *
 */
public interface IBusinessObject {

    /**
     * Gets the entity representation of the current object. This allows the object
     * to be stored in the datastore.
     * @return The Entity representation of the object.
     */
    public Entity getEntity();


    /**
     * Loads an entity into the calling IBusinessObject. This will set all
     * values according to the entity's information
     * @param e The entity to load
     * @throws EntityMismatchException if an unexpected entity is passed
     */
    public void loadEntity(Entity e) throws EntityMismatchException;
}
