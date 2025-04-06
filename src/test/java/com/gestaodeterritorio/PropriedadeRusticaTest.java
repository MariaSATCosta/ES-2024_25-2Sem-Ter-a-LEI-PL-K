package com.gestaodeterritorio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

/**
 * Author: ${user.name}
 * Date: 2025-04-05T12:00:00   // Adjust the date/time as needed.
 *
 * Cyclomatic Complexity per method in PropriedadeRustica:
 *   - Constructor: 1
 *   - getObjectId: 1
 *   - getParId: 1
 *   - getParNum: 1
 *   - getShapeLength: 1
 *   - getShapeArea: 1
 *   - getGeometry: 1
 *   - getOwner: 1
 *   - getFreguesia: 1
 *   - getMunicipio: 1
 *   - getIlha: 1
 *   - toString: 1
 */
public class PropriedadeRusticaTest {

    // Helper method to set a private field's value via reflection.
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    // ------------------ Constructor Test (CC = 1) ------------------
    @Test
    public void constructor() {
        PropriedadeRustica pr = new PropriedadeRustica();
        assertNotNull(pr, "Error: Constructor should create a non-null instance of PropriedadeRustica"); // Error if pr is null.
    }

    // ------------------ getObjectId Test (CC = 1) ------------------
    @Test
    public void getObjectId() throws Exception {
        PropriedadeRustica pr = new PropriedadeRustica();
        String expected = "123";
        setField(pr, "objectId", expected);
        assertEquals(expected, pr.getObjectId(), "Error: getObjectId should return the value set for objectId"); // Error if not equal.
    }

    // ------------------ getParId Test (CC = 1) ------------------
    @Test
    public void getParId() throws Exception {
        PropriedadeRustica pr = new PropriedadeRustica();
        String expected = "456";
        setField(pr, "parId", expected);
        assertEquals(expected, pr.getParId(), "Error: getParId should return the value set for parId"); // Error if not equal.
    }

    // ------------------ getParNum Test (CC = 1) ------------------
    @Test
    public void getParNum() throws Exception {
        PropriedadeRustica pr = new PropriedadeRustica();
        String expected = "789";
        setField(pr, "parNum", expected);
        assertEquals(expected, pr.getParNum(), "Error: getParNum should return the value set for parNum"); // Error if not equal.
    }

    // ------------------ getShapeLength Test (CC = 1) ------------------
    @Test
    public void getShapeLength() throws Exception {
        PropriedadeRustica pr = new PropriedadeRustica();
        String expected = "10.5";
        setField(pr, "shapeLength", expected);
        assertEquals(expected, pr.getShapeLength(), "Error: getShapeLength should return the value set for shapeLength"); // Error if not equal.
    }

    // ------------------ getShapeArea Test (CC = 1) ------------------
    @Test
    public void getShapeArea() throws Exception {
        PropriedadeRustica pr = new PropriedadeRustica();
        String expected = "200.5";
        setField(pr, "shapeArea", expected);
        assertEquals(expected, pr.getShapeArea(), "Error: getShapeArea should return the value set for shapeArea"); // Error if not equal.
    }

    // ------------------ getGeometry Test (CC = 1) ------------------
    @Test
    public void getGeometry() throws Exception {
        PropriedadeRustica pr = new PropriedadeRustica();
        String expected = "POINT(1 2)";
        setField(pr, "geometry", expected);
        assertEquals(expected, pr.getGeometry(), "Error: getGeometry should return the value set for geometry"); // Error if not equal.
    }

    // ------------------ getOwner Test (CC = 1) ------------------
    @Test
    public void getOwner() throws Exception {
        PropriedadeRustica pr = new PropriedadeRustica();
        String expected = "John Doe";
        setField(pr, "owner", expected);
        assertEquals(expected, pr.getOwner(), "Error: getOwner should return the value set for owner"); // Error if not equal.
    }

    // ------------------ getFreguesia Test (CC = 1) ------------------
    @Test
    public void getFreguesia() throws Exception {
        PropriedadeRustica pr = new PropriedadeRustica();
        String expected = "FreguesiaTest";
        setField(pr, "freguesia", expected);
        assertEquals(expected, pr.getFreguesia(), "Error: getFreguesia should return the value set for freguesia"); // Error if not equal.
    }

    // ------------------ getMunicipio Test (CC = 1) ------------------
    @Test
    public void getMunicipio() throws Exception {
        PropriedadeRustica pr = new PropriedadeRustica();
        String expected = "MunicipioTest";
        setField(pr, "municipio", expected);
        assertEquals(expected, pr.getMunicipio(), "Error: getMunicipio should return the value set for municipio"); // Error if not equal.
    }

    // ------------------ getIlha Test (CC = 1) ------------------
    @Test
    public void getIlha() throws Exception {
        PropriedadeRustica pr = new PropriedadeRustica();
        String expected = "IlhaTest";
        setField(pr, "ilha", expected);
        assertEquals(expected, pr.getIlha(), "Error: getIlha should return the value set for ilha"); // Error if not equal.
    }

    // ------------------ toString Test (CC = 1) ------------------
    @Test
    public void toStringTest() throws Exception {
        PropriedadeRustica pr = new PropriedadeRustica();
        // Set several fields to later verify the toString output.
        String objectId = "1";
        String municipio = "Municipio";
        String freguesia = "Freguesia";
        String parNum = "ParNum";
        String shapeArea = "100";
        String ilha = "Ilha";
        setField(pr, "objectId", objectId);
        setField(pr, "municipio", municipio);
        setField(pr, "freguesia", freguesia);
        setField(pr, "parNum", parNum);
        setField(pr, "shapeArea", shapeArea);
        setField(pr, "ilha", ilha);
        String result = pr.toString();
        // Validate that the result contains all expected substrings.
        assertTrue(result.contains(objectId), "Error: toString should include objectId"); // Error if missing objectId.
        assertTrue(result.contains(municipio), "Error: toString should include municipio"); // Error if missing municipio.
        assertTrue(result.contains(freguesia), "Error: toString should include freguesia"); // Error if missing freguesia.
        assertTrue(result.contains(parNum), "Error: toString should include parNum"); // Error if missing parNum.
        assertTrue(result.contains(shapeArea), "Error: toString should include shapeArea"); // Error if missing shapeArea.
        assertTrue(result.contains(ilha), "Error: toString should include ilha"); // Error if missing ilha.
    }
}
