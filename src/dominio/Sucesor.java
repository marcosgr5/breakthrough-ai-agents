package dominio;

public class Sucesor {
    public long origen;
    public EstadoTablero estado;

    public Sucesor(long origen,EstadoTablero estado){
        this.origen = origen;
        this.estado = estado;
    }

    public long getOrigen(){
        return origen;
    }

    public EstadoTablero getEstado(){
        return estado;
    }

    public void setOrigen(long origen){
        this.origen=origen;
    }

    public void setEstado(EstadoTablero estado){
        this.estado=estado;
    }

}
