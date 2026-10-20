package dominio;

public class ManejadorMovimientos {

    private long mascaraIzq;
    private long mascaraDer;
    private int movimientosValidos; // XYZ, X -> Izquierda, Y -> Derecha, Z -> Vertical

    public ManejadorMovimientos() {
        movimientosValidos = 0;
        mascaraIzq = 0x7F7F7F7F7F7F7F7FL;
        mascaraDer = 0xFEFEFEFEFEFEFEFEL;
    }

    public void reset() {
        movimientosValidos = 0;
    }

    public void update (int i) {
        movimientosValidos = i;
    }

    public int calcularMovimientosValidos(long origen, long piezasBlancas, long piezasNegras, boolean turno) {
        reset();
         if(puedeAvanzar(piezasBlancas,piezasNegras,origen,turno)) {
                update(movimientosValidos+1);
         }
         if(puedeDiagonalDer(piezasBlancas, piezasNegras, origen, turno)){
                update(movimientosValidos+2);
         }
         if(puedeDiagonalIzq(piezasBlancas, piezasNegras, origen, turno)){
                update(movimientosValidos+4);
         }
        return movimientosValidos;
    }

    public boolean puedeAvanzar(long piezasBlancas, long piezasNegras, long origen, boolean turno) {
        long posicionesVacias = ~(piezasBlancas|piezasNegras);
        long destinosValidos, destinoFinal, origenesValidos;
        if (turno) {
            destinosValidos = posicionesVacias & (piezasBlancas<<8);
            origenesValidos = destinosValidos>>>8;
            destinoFinal = posicionesVacias & (origen<<8);
        }   
        else {
            destinosValidos = posicionesVacias & (piezasNegras>>>8);
            origenesValidos = destinosValidos<<8;
            destinoFinal = posicionesVacias & (origen>>>8);
        }
            
        return destinoFinal != 0 && (origen & origenesValidos) == origen;
    }

    public boolean puedeDiagonalDer(long piezasBlancas, long piezasNegras, long origen, boolean turno){
      
        long destinosValidos, destinoFinal, origenesValidos,posicionesVacias;
        if (turno) {
            posicionesVacias = ~(piezasBlancas);
            destinosValidos = ((piezasBlancas & mascaraDer)<<7) & posicionesVacias;
            origenesValidos = destinosValidos >> 7;
            destinoFinal = posicionesVacias & (origen<<7);
        }   
        else {
            posicionesVacias = ~(piezasNegras);
            destinosValidos = (piezasNegras & mascaraDer)>>>9 & posicionesVacias;
            origenesValidos = destinosValidos<<9;
            destinoFinal = posicionesVacias & (origen>>>9);
        }
            
        return destinoFinal != 0 && (origen & origenesValidos) == origen;
    }

        public boolean puedeDiagonalIzq(long piezasBlancas, long piezasNegras, long origen, boolean turno){
      
        long destinosValidos, destinoFinal, origenesValidos,posicionesVacias;
        if (turno) {
            posicionesVacias = ~(piezasBlancas);
            destinosValidos = ((piezasBlancas & mascaraIzq)<<9) & posicionesVacias;
            origenesValidos = destinosValidos >> 9;
            destinoFinal = posicionesVacias & (origen<<9);
        }   
        else {
            posicionesVacias = ~(piezasNegras);
            destinosValidos = (piezasNegras & mascaraIzq)>>>7 & posicionesVacias;
            origenesValidos = destinosValidos<<7;
            destinoFinal = posicionesVacias & (origen>>>7);
        }
            
        return destinoFinal != 0 && (origen & origenesValidos) == origen;
    }

}


