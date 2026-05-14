package logica;

public class LineaOrden {

    private Producto producto;
    private double cantidad;
    private double precioUnitario;

    public LineaOrden(Producto producto, double cantidad, double precioUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public Producto getProducto() {
        return producto;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double calcularCosto() {
        return cantidad * precioUnitario;
    }

    @Override
    public String toString() {
        return "LineaOrden{" +
                "producto=" + producto.getNombre() +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", costo=" + calcularCosto() +
                '}';
    }
}