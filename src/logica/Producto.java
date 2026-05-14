package logica;

public class Producto {

    private final Integer codigo;
    private String nombre;
    private double existencias;
    private String unidad;
    private double precio;

    public Producto(Integer codigo, String nombre, double existencias, String unidad, double precio) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.existencias = existencias;
        this.unidad = unidad;
        this.precio = precio;
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

    public double getExistencias() {
        return existencias;
    }

    public void setExistencias(double existencias) {
        this.existencias = existencias;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean hayStock(double cantidad) {
        return cantidad > 0 && existencias >= cantidad;
    }

    public void reducirExistencias(double cantidad) {
        if (hayStock(cantidad)) {
            existencias -= cantidad;
        }
    }

    public void aumentarExistencias(double cantidad) {
        if (cantidad > 0) {
            existencias += cantidad;
        }
    }

    public void actualizar(String nombre, double existencias, String unidad, double precio) {
        this.nombre = nombre;
        this.existencias = existencias;
        this.unidad = unidad;
        this.precio = precio;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "codigo=" + codigo +
                ", nombre='" + nombre + '\'' +
                ", existencias=" + existencias +
                ", unidad='" + unidad + '\'' +
                ", precio=" + precio +
                '}';
    }
}