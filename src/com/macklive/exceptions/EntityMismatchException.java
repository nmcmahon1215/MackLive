package com.macklive.exceptions;

/**
 * Describes an error that occurs when an entity of an unexpected kind is
 * encountered.
 */
public class EntityMismatchException extends Exception {
    
    private static final long serialVersionUID = 4611623211080717456L;

    public EntityMismatchException(String message){
        super(message);
    }
}
