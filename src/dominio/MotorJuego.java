package dominio;
import java.util.ArrayList;
import java.util.List;
import persistencia.*;


public class MotorJuego {
	
	private EstadoTablero estadoActual;
	private Decoder d;
	private ManejadorMovimientos mMovimientos;
	private ManejadorGuardados mGuardados;
	
	public MotorJuego() {
		estadoActual = new EstadoTablero(0X000000000000FFFFL,0xFFFF000000000000L, true);
		d = new Decoder();
		mMovimientos = new ManejadorMovimientos();
		mGuardados = new ManejadorGuardados();
	}

	public EstadoTablero getEstadoActual() {
		return estadoActual;
	}
	
	public void setEstadoTablero(EstadoTablero estadoActual) {
		this.estadoActual = estadoActual;
	}


	public long boardLocationToLong (String s) {
		return d.decodeKey(s);
	}

	public boolean idValido(String s) {
		return d.idValido(s);
	}

	public boolean enProgreso() {
		return !(estadoActual.esVictoriaBlancas()||estadoActual.esVictoriaNegras());
	}

	public ArrayList<String> calcularDestinosValidos (long origen) {
		Profiler.start("MotorJuego.calcularDestinosValidos");

		ArrayList<String> destinosValidos = new ArrayList<>();
		int movValidos = mMovimientos.calcularMovimientosValidos(origen, estadoActual.getPiezasBlancas(), estadoActual.getPiezasNegras(), estadoActual.getTurno());
		boolean izq,der,cent;
		cent = (movValidos & 0x1L)==1;
		movValidos= movValidos >>>1;
		der = (movValidos & 0x1L)==1;
		movValidos= movValidos >>>1;
		izq = (movValidos & 0x1L)==1;

		if (estadoActual.getTurno()) {
			if(cent) destinosValidos.add(d.decodeLong(origen<<8));
			if(der)  destinosValidos.add(d.decodeLong(origen<<7));
			if(izq)  destinosValidos.add(d.decodeLong(origen<<9));
		} else {
			if(cent) destinosValidos.add(d.decodeLong(origen>>>8));
			if(der)  destinosValidos.add(d.decodeLong(origen>>>9));
			if(izq)  destinosValidos.add(d.decodeLong(origen>>>7));
		}

		Profiler.stop("MotorJuego.calcularDestinosValidos");
		return destinosValidos;
	}

	public void cambiarTurno() {
		estadoActual.setTurno(!estadoActual.getTurno());
	}

	public EstadoTablero ejecutarMovimiento(long origen, long destino, EstadoTablero estado) {
		Profiler.start("MotorJuego.ejecutarMovimiento");

		EstadoTablero t = new EstadoTablero(estado.getPiezasBlancas(),estado.getPiezasNegras(),estado.getTurno());
		long cadenaMovimiento = origen|destino;
		if(estadoActual.getTurno()) {
			t.setPiezasBlancas(cadenaMovimiento^estadoActual.getPiezasBlancas());
			t.setPiezasNegras(~(destino)&estadoActual.getPiezasNegras());
		}
		else {
			t.setPiezasNegras(cadenaMovimiento^estadoActual.getPiezasNegras());
			t.setPiezasBlancas(~(destino)&estadoActual.getPiezasBlancas());
		}

		Profiler.stop("MotorJuego.ejecutarMovimiento");
		return t;
	}

	public boolean determinarGanador() {
        return estadoActual.esVictoriaBlancas();
	}

	public void cargarEstado (int index) {
		String estado = mGuardados.leerEstado(index);
		String [] atributos = estado.split(" ");
		long pBlancas = Long.parseLong(atributos[1]);
		long pNegras = Long.parseLong(atributos[2]);
		boolean t = Boolean.parseBoolean(atributos[3]);
		estadoActual.setPiezasBlancas(pBlancas);
		estadoActual.setPiezasNegras(pNegras);
		estadoActual.setTurno(t);
	}

	public boolean guardarEstado(int index) {
		String estado = index + ": " + estadoActual.getPiezasBlancas() + " " + estadoActual.getPiezasNegras() + " " + estadoActual.getTurno();
		return mGuardados.escribirEstado(index, estado);
	}
	
	/*método para generar los sucesores a partir de un estado, será útil para la implementación del minmax.
	 * Utilizo una lista de listas, para resolver el problema de las probabilidades
	 * al escoger un sucesor aleatorio, ya que unos podrían tener mas acciones, que otros.*/
	public List<List<Sucesor>> getSucesores(){
		Profiler.start("MotorJuego.getSucesores");

		EstadoTablero t = new EstadoTablero(estadoActual.getPiezasBlancas(),estadoActual.getPiezasNegras(),estadoActual.getTurno());
		List<List<Sucesor>> sucesores = new ArrayList<>(16);//lista que recoge las listas de sucesores de un mismo origen
		ArrayList<String> validos = new ArrayList<>();//destinos validos de un origen
		EstadoTablero tAux;
		long p;
		long pB = t.getPiezasBlancas();//copia local de las piezas blancas del estado dado
		long pN = t.getPiezasNegras();//copia negras
		if(t.getTurno()) {//turno blancas
			while(pB!=0) {//en cada itr. vamos a ir escogiendo bit menos sign. e ir sacando los acciones(destinos validos) de dicho orign
				ArrayList<Sucesor> mismoOrign = new ArrayList<>(3);//sucesores de un mismo origen
				p = Long.lowestOneBit(pB);//escoge el bit menos significativo
				validos = calcularDestinosValidos(p);
				for(int i=0;!validos.isEmpty() && i<validos.size();i++) {//pongo que validos sea not null, ya que cuando el turno es de las blancas y vamos iterando por el bit menos significativo,
					tAux = ejecutarMovimiento(p,boardLocationToLong(validos.get(i)),t);
					mismoOrign.add(new Sucesor(p,tAux));
				}
				if(!validos.isEmpty()) {
				sucesores.add(mismoOrign);
				}
				pB=pB^p;//paso al siguiente bit
			}
		}else {
			while(pN!=0) {
				ArrayList<Sucesor> mismoOrign = new ArrayList<>(3);//sucesores de un mismo origen
				p = Long.lowestOneBit(pN);
				validos = calcularDestinosValidos(p);
				for(int i=0;!validos.isEmpty() && i<validos.size();i++) {
					tAux = ejecutarMovimiento(p,boardLocationToLong(validos.get(i)),t);
					mismoOrign.add(new Sucesor(p,tAux));
				}
				if(!validos.isEmpty()) {
				sucesores.add(mismoOrign);
				}
				pN=pN^p;
			}
		}

		Profiler.stop("MotorJuego.getSucesores");
		return sucesores;
	}
}