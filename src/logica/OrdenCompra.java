package logica;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdenCompra {

    private final Integer numero;
    private final LocalDateTime fecha;
    private EstadoOrden estado;
    private List<LineaOrden> lineas;
    private Cliente cliente;

    private static final double IV = 0.13;

    public OrdenCompra(Integer numero, Cliente cliente) {
        this.numero = numero;
        this.cliente = cliente;
        this.fecha = LocalDateTime.now();
        this.estado = EstadoOrden.INICIADA;
        this.lineas = new ArrayList<>();
    }

    public Integer getNumero() {
        return numero;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<LineaOrden> getLineas() {
        return lineas;
    }

    public void agregarLinea(Producto producto, double cantidad) {
        if (producto == null) {
            return;
        }

        if (cantidad <= 0) {
            return;
        }

        if (!producto.hayStock(cantidad)) {
            return;
        }

        LineaOrden nuevaLinea = new LineaOrden(producto, cantidad, producto.getPrecio());
        lineas.add(nuevaLinea);
        producto.reducirExistencias(cantidad);
    }

    public void eliminarLinea(Integer posicion) {
        int indice = posicion - 1;

        if (indice < 0 || indice >= lineas.size()) {
            return;
        }

        LineaOrden linea = lineas.remove(indice);
        Producto producto = linea.getProducto();

        if (producto != null) {
            producto.aumentarExistencias(linea.getCantidad());
        }
    }

    public double calcularSubtotal() {
        double subtotal = 0;

        for (LineaOrden linea : lineas) {
            subtotal += linea.calcularCosto();
        }

        return subtotal;
    }

    public double calcularImpuesto() {
        return calcularSubtotal() * IV;
    }

    public double calcularTotal() {
        return calcularSubtotal() + calcularImpuesto();
    }

    public boolean puedeEditar() {
        return estado == EstadoOrden.INICIADA;
    }

    @Override
    public String toString() {
        return "Orden{" +
                "numero=" + numero +
                ", fecha=" + fecha +
                ", estado=" + estado +
                ", cliente=" + cliente.getNombre() +
                ", subtotal=" + calcularSubtotal() +
                ", impuesto=" + calcularImpuesto() +
                ", total=" + calcularTotal() +
                '}';
    }
}