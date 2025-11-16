package Junit;
import user.*;
import java.util.*;
import java.beans.Transient;
import java.io.*;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runners.*;
import org.junit.Before;

/**
 * Proves unitàries per a la classe GuestUser.
 * Verifica la correcta inicialització i la gestió dels atributs.
 */
public class TestGuestUser {
    private GuestUser gu;
    String GUEST_ID = "g234";
    String GUEST_NAME = "Invitado";
    String EXPIRATION = "2025-12-23";

    @Before
    public void setup(){
        // Nota: En la implementació, s'ha utilitzat GUEST_ID tant per ID com per displayName en la crida.
        // Adaptació: Es manté el GUEST_ID per ID i s'ha de comprovar el valor assignat.
        gu = new GuestUser(GUEST_ID, GUEST_ID, EXPIRATION);
    }

    /**
     * Comprova que el constructor inicialitza correctament l'ID, el nom visible
     * i la data d'expiració.
     */
    @Test 
    public void ConstructorsAndGettersTest(){
        assertEquals(GUEST_ID,gu.getId());
        assertEquals(GUEST_ID,gu.getDisplayName());
        assertEquals(EXPIRATION,gu.getExpiresAt());
    } 

    /**
     * Comprova que el nom visible (displayName) es pot canviar correctament
     * a través del mètode heretat.
     */
    @Test 
    public void changeDisplayNameTest(){
        String newName = "NouConvidat";
        gu.changeDisplayName(newName);
        assertEquals(newName,gu.getDisplayName());
    }
}