package controladora;

import logica.Cliente;
import logica.EstadoOrden;
import logica.LineaOrden;
import logica.OrdenCompra;
import logica.Producto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TiendaController {

    private static TiendaController instance;

    private int nextCodigoCliente;
    private int nextCodigoProducto;
    private int nextNumeroOrden;

    private Map<Integer, Cliente> clientes;
    private Map<Integer, Producto> productos;
    private Map<Integer, OrdenCompra> ordenes;

    private TiendaController() {
        this.nextCodigoCliente = 1;
        this.nextCodigoProducto = 1;
        this.nextNumeroOrden = 1;

        this.clientes = new LinkedHashMap<>();
        this.productos = new LinkedHashMap<>();
        this.ordenes = new LinkedHashMap<>();
    }

    public static TiendaController getInstancia() {
        if (instance == null) {
            instance = new TiendaController();
        }

        return instance;
    }

    // =========================
    // CLIENTES
    // =========================

    public Cliente crearCliente(String nombre, String correo, String telefono) {
        Cliente cliente = new Cliente(nextCodigoCliente, nombre, correo, telefono);

        clientes.put(nextCodigoCliente, cliente);
        nextCodigoCliente++;

        return cliente;
    }

    public Cliente obtenerCliente(Integer codigo) {
        return clientes.get(codigo);
    }

    public List<Cliente> listarClientes() {
        return new ArrayList<>(clientes.values());
    }

    public boolean actualizarCliente(Integer codigo, String nombre, String correo, String telefono) {
        Cliente cliente = obtenerCliente(codigo);

        if (cliente == null) {
            return false;
        }

        cliente.setNombre(nombre);
        cliente.setCorreo(correo);
        cliente.setTelefono(telefono);

        return true;
    }

    public boolean eliminarCliente(Integer codigo) {
        Cliente cliente = obtenerCliente(codigo);

        if (cliente == null) {
            return false;
        }

        if (!cliente.getOrdenes().isEmpty()) {
            return false;
        }

        clientes.remove(codigo);
        return true;
    }

    public List<OrdenCompra> listarOrdenesCliente(Integer codigoCliente) {
        Cliente cliente = obtenerCliente(codigoCliente);

        if (cliente == null) {
            return new ArrayList<>();
        }

        return cliente.getOrdenes();
    }

    // =========================
    // PRODUCTOS
    // =========================

    public Producto crearProducto(String nombre, double existencias, String unidad, double precio) {
        Producto producto = new Producto(nextCodigoProducto, nombre, existencias, unidad, precio);

        productos.put(nextCodigoProducto, producto);
        nextCodigoProducto++;

        return producto;
    }

    public Producto obtenerProducto(Integer codigo) {
        return productos.get(codigo);
    }

    public List<Producto> listarProductos() {
        return new ArrayList<>(productos.values());
    }

    public boolean actualizarProducto(Integer codigo, String nombre, double existencias, String unidad, double precio) {
        Producto producto = obtenerProducto(codigo);

        if (producto == null) {
            return false;
        }

        producto.actualizar(nombre, existencias, unidad, precio);
        return true;
    }

    public boolean eliminarProducto(Integer codigo) {
        Producto producto = obtenerProducto(codigo);

        if (producto == null) {
            return false;
        }

        if (productoTieneOrdenes(codigo)) {
            return false;
        }

        productos.remove(codigo);
        return true;
    }

    private boolean productoTieneOrdenes(Integer codigoProducto) {
        for (OrdenCompra orden : ordenes.values()) {
            for (LineaOrden linea : orden.getLineas()) {
                if (linea.getProducto().getCodigo().equals(codigoProducto)) {
                    return true;
                }
            }
        }

        return false;
    }

    // =========================
    // ORDENES
    // =========================

    public OrdenCompra crearOrden(Integer codigoCliente) {
        Cliente cliente = obtenerCliente(codigoCliente);

        if (cliente == null) {
            return null;
        }

        OrdenCompra orden = new OrdenCompra(nextNumeroOrden, cliente);

        ordenes.put(nextNumeroOrden, orden);
        cliente.agregarOrden(orden);

        nextNumeroOrden++;

        return orden;
    }

    public OrdenCompra obtenerOrden(Integer numeroOrden) {
        return ordenes.get(numeroOrden);
    }

    public List<OrdenCompra> listarOrdenes() {
        return new ArrayList<>(ordenes.values());
    }

    public boolean cambiarEstadoOrden(Integer numeroOrden, String estado) {
        OrdenCompra orden = obtenerOrden(numeroOrden);

        if (orden == null) {
            return false;
        }

        try {
            EstadoOrden nuevoEstado = EstadoOrden.valueOf(estado.toUpperCase());
            orden.setEstado(nuevoEstado);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean ponerOrdenPendiente(Integer numeroOrden) {
        OrdenCompra orden = obtenerOrden(numeroOrden);

        if (orden == null) {
            return false;
        }

        if (orden.getLineas().isEmpty()) {
            return false;
        }

        orden.setEstado(EstadoOrden.PENDIENTE);
        return true;
    }

    public boolean ponerOrdenTerminada(Integer numeroOrden) {
        OrdenCompra orden = obtenerOrden(numeroOrden);

        if (orden == null) {
            return false;
        }

        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            return false;
        }

        orden.setEstado(EstadoOrden.TERMINADA);
        return true;
    }

    public boolean agregarLineaOrden(Integer numeroOrden, Integer codigoProducto, double cantidad) {
        OrdenCompra orden = obtenerOrden(numeroOrden);
        Producto producto = obtenerProducto(codigoProducto);

        if (orden == null || producto == null) {
            return false;
        }

        if (!orden.puedeEditar()) {
            return false;
        }

        if (cantidad <= 0) {
            return false;
        }

        if (!producto.hayStock(cantidad)) {
            return false;
        }

        orden.agregarLinea(producto, cantidad);
        return true;
    }

    public boolean eliminarLineaOrden(Integer numeroOrden, Integer posicion) {
        OrdenCompra orden = obtenerOrden(numeroOrden);

        if (orden == null) {
            return false;
        }

        if (!orden.puedeEditar()) {
            return false;
        }

        if (posicion < 1 || posicion > orden.getLineas().size()) {
            return false;
        }

        orden.eliminarLinea(posicion);
        return true;
    }

    public List<LineaOrden> obtenerLineasOrden(Integer numeroOrden) {
        OrdenCompra orden = obtenerOrden(numeroOrden);

        if (orden == null) {
            return new ArrayList<>();
        }

        return orden.getLineas();
    }

    public double obtenerMontoTotalPendiente() {
        double total = 0;

        for (OrdenCompra orden : ordenes.values()) {
            if (orden.getEstado() == EstadoOrden.PENDIENTE) {
                total += orden.calcularTotal();
            }
        }

        return total;
    }
}