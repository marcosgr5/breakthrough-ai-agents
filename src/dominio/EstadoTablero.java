package dominio;

public class EstadoTablero {

	private long piezasBlancas;
	private long piezasNegras;
	private boolean turno; // TRUE -> Blancas, FALSE -> Negras.
	private final long mascaraVictoriaN;
	private final long mascaraVictoriaB;
	
	public EstadoTablero(long pb, long pn, boolean t) {
		piezasBlancas = pb;
		piezasNegras = pn;
		turno = t;
		mascaraVictoriaN = 0x00000000000000FFL;
		mascaraVictoriaB = 0xFF00000000000000L;
	}

	public long getPiezasBlancas() {
		return piezasBlancas;
	}

	public long getPiezasNegras() {
		return piezasNegras;
	}

	public boolean getTurno() {
		return turno;
	}

	public void setPiezasBlancas(long piezasBlancas) {
		this.piezasBlancas = piezasBlancas;
	}

	public void setPiezasNegras(long piezasNegras) {
		this.piezasNegras = piezasNegras;
	}

	public void setTurno(boolean turno) {
		this.turno = turno;
	}

	public boolean esVictoriaNegras() {
		return piezasBlancas == 0 || (piezasNegras&mascaraVictoriaN) != 0;
	}

	public boolean esVictoriaBlancas() {
		return piezasNegras == 0 || (piezasBlancas&mascaraVictoriaB) != 0;
	}
	
	

}
