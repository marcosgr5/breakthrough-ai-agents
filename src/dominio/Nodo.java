package dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Nodo {

    public int id;
    public EstadoTablero estado;
    public Queue<Sucesor> sucesores;//lo meto para llevar un control en montecarlo, para que no repita los sucesores de un nodo, lo haré eliminando el sucesor de la lista y asi, en el caso de volver a expandir el mismo nodo, que escoja otra sucesor
    public int idPadre;
    public int valor;
    public int nVisitado;
    public List<Integer> idHijos;
    public Sucesor sucesorOrigen;

    public Nodo(int id, EstadoTablero estado, int idPadre, int valor, int nVisitado, MotorJuego juego){
        this.id = id;
        this.estado = estado;
        juego.setEstadoTablero(estado);
        List<List<Sucesor>>sucs = juego.getSucesores();
        sucesores = procesarSucesoresACola(sucs);
        idHijos = new ArrayList<>();//le meto este array debido a que los nodos en montecarlo deben tener un control de los nodos sucesores que tienen, principalmente para hacer posible el calculo de uct de los mismo y asi expandir el mejor
        this.idPadre = idPadre;
        this.valor = valor;
        this.nVisitado = nVisitado;
        this.sucesorOrigen = null;
    }
    /*metodo para simular en la cola la obtencion de un sucesor sin meterle de nuevo el sesgo,
    lo meto en la cola debido a que tengo comprobar en el calculo del arbol de montecarlo, si a el nodo
    seleccionado en ese credito, aun le quedan sucesores sin expandir */
    public Queue<Sucesor> procesarSucesoresACola(List<List<Sucesor>> listaDeListas) {
        List<Sucesor> todosLosSucesores = new ArrayList<>();
        for (List<Sucesor> sublista : listaDeListas) {
            if (sublista != null) {
                todosLosSucesores.addAll(sublista);
            }
        }
        Collections.shuffle(todosLosSucesores);
        return new LinkedList<>(todosLosSucesores);
    }

    public void setId(int id){
        this.id=id;
    }

    public int getId(){
        return id;
    }

    public void setEstado(EstadoTablero estado){
        this.estado = estado;
    }

    public EstadoTablero getEstado(){
        return estado;
    }

    public void setIdpadre(int idPadre){
        this.idPadre = idPadre;
    }

    public int getIdPadre(){
        return idPadre;
    }

    public void setValor(int valor){
        this.valor = valor;
    }

    public int getValor(){
        return valor;
    }

    public void setNVisitado(int nVisitado){
        this.nVisitado = nVisitado;
    }

    public int getNVisitado(){
        return nVisitado;
    }    

}