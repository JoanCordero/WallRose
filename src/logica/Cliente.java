package logica;

import java.util.ArrayList;
import java.util.List;

public class Cliente {

    private final Integer codigo;
    private String nombre;
    private String correo;
    private String telefono;
    private List<OrdenCompra> ordenes;

    public Cliente(Integer codigo, String nombre, String correo, String telefono) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.ordenes = new ArrayList<>();
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<OrdenCompra> getOrdenes() {
        return ordenes;
    }

    public void agregarOrden(OrdenCompra orden) {
        if (orden != null) {
            ordenes.add(orden);
        }
    }

    public void borrarOrden(OrdenCompra orden) {
        ordenes.remove(orden);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "codigo=" + codigo +
                ", nombre='" + nombre + '\'' +
                ", correo='" + correo + '\'' +
                ", telefono='" + telefono + '\'' +
                '}';
    }
}