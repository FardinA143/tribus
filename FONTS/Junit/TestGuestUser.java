package Junit;
import user.*;
import java.util.*;
import java.beans.Transient;
import java.io.*;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runners.*;
import org.junit.Before;

public class TestGuestUser {
    private GuestUser gu;
    String GUEST_ID = "g234";
    String GUEST_NAME = "Invitado";
    String EXPIRATION = "2025-12-23";

    @Before
    public void setup(){
        gu = new GuestUser(GUEST_ID, GUEST_ID, EXPIRATION);
    }

    @Test 
    public void ConstructorsAndGettersTest(){
        assertEquals(GUEST_ID,gu.getId());
        assertEquals(GUEST_NAME,gu.getDisplayName());
        assertEquals(EXPIRATION,gu.getExpiresAt());
    } 

    @Test 
    public void changeDisplayNameTest(){
        String newName = "Invitado";
        gu.changeDisplayName(newName);
        assertEquals(newName,gu.getDisplayName());
    }
}



