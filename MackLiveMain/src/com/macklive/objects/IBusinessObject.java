/**
 *
 */
package com.macklive.objects;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.macklive.exceptions.EntityMismatchException;

/**
 * Describes a typical object. These objects can be stored in the data store
 */
public interface IBusinessObject {

    /**
     * Gets the entity representation of the current object. This allows the object
     * to be stored in the datastore.
     *
     * @return The Entity representation of the object.
     */
    Entity getEntity();

    /**
     * Sets the key of the object
     *
     * @param k The datastore key
     */
    void setKey(Key k);

    /**
     * Gets the key for the object. May return null if a key is not yet set.
     *
     * @return Key Datastore key identifier.
     */
    Key getKey();


    /**
     * Loads an entity into the calling IBusinessObject. This will set all
     * values according to the entity's information
     *
     * @param e The entity to load
     * @throws EntityMismatchException if an unexpected entity is passed
     */
    void loadEntity(Entity e) throws EntityMismatchException;

    /**
     * Converts the Java object to JSON representation
     *
     * @return JSON representation of the object.
     */
    String toJSON();

    /**
     * @return True if the object has an owner (a User). False otherwise.
     */
    boolean hasOwner();

}
