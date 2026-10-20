package persistencia;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ManejadorGuardados {
    
    private File estadosGuardados;
    private int n_estadosGuardados;

    public ManejadorGuardados() {
        estadosGuardados= new File("data/estados_guardados.txt");
        n_estadosGuardados = 0;
    }

    public String leerEstado(int index) {
        
        int i = 0;
        String line = null;
        try {
            Scanner scanner = new Scanner(estadosGuardados);
                while (scanner.hasNextLine() && i <= index) {
                    line = scanner.nextLine();
                    i++;
                }
           scanner.close();
        
        } catch (FileNotFoundException e) {
            line = null;
        }
        return line;
    }

    public boolean escribirEstado(int index, String nuevaLinea) {
        ArrayList<String> lineas = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(estadosGuardados);
            while (scanner.hasNextLine()) {
                lineas.add(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            return false;
        }
        lineas.set(index, nuevaLinea);
        try (FileWriter fw = new FileWriter(estadosGuardados, false)) {
            for (String linea : lineas) {
                fw.write(linea + "\r\n");
            }
        } catch(IOException e) {
            return false;
        }

        return true;

    }

}

