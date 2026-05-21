package interfaz;

import controladora.TiendaController;
import logica.Cliente;
import logica.EstadoOrden;
import logica.OrdenCompra;
import logica.Producto;
import logica.LineaOrden;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class VentanaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    private TiendaController control;

    private JTabbedPane tabbedPane;

    private JTable tablaClientes;
    private DefaultTableModel modeloClientes;

    private JTable tablaProductos;
    private DefaultTableModel modeloProductos;
    
    private JTable tablaOrdenes;
    private DefaultTableModel modeloOrdenes;
    private JLabel lblTotalPendienteOrdenes;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }

    public VentanaPrincipal() {
        control = TiendaController.getInstancia();

        cargarDatosPrueba();

        setTitle("Sistema de Tienda WallRose");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        inicializarComponentes();

        cargarClientes();
        cargarProductos();
        cargarOrdenes();
    }

    private void inicializarComponentes() {
        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Clientes", crearPanelClientes());
        tabbedPane.addTab("Órdenes", crearPanelOrdenes());
        tabbedPane.addTab("Productos", crearPanelProductos());

        getContentPane().add(tabbedPane, BorderLayout.CENTER);
    }

    // =========================================================
    // PANEL CLIENTES
    // =========================================================

    private JPanel crearPanelClientes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        modeloClientes = new DefaultTableModel(
                new Object[] { "ID", "Nombre", "Email", "Teléfono" }, 0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaClientes = new JTable(modeloClientes);
        JScrollPane scrollClientes = new JScrollPane(tablaClientes);

        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 5, 10));

        JButton btnVer = new JButton("Ver");
        JButton btnAgregar = new JButton("Agregar");
        JButton btnEditar = new JButton("Editar");
        JButton btnBorrar = new JButton("Borrar");

        btnVer.addActionListener(e -> verCliente());
        btnAgregar.addActionListener(e -> agregarCliente());
        btnEditar.addActionListener(e -> editarCliente());
        btnBorrar.addActionListener(e -> borrarCliente());

        panelBotones.add(btnVer);
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnBorrar);

        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.add(panelBotones, BorderLayout.NORTH);

        panel.add(scrollClientes, BorderLayout.CENTER);
        panel.add(panelDerecho, BorderLayout.EAST);

        return panel;
    }

    private void cargarClientes() {
        modeloClientes.setRowCount(0);

        List<Cliente> clientes = control.listarClientes();

        for (Cliente cliente : clientes) {
            Object[] fila = {
                    cliente.getCodigo(),
                    cliente.getNombre(),
                    cliente.getCorreo(),
                    cliente.getTelefono()
            };

            modeloClientes.addRow(fila);
        }
    }

    private void agregarCliente() {
        String[] datos = pedirDatosCliente("Agregar cliente", null);

        if (datos == null) {
            return;
        }

        control.crearCliente(datos[0], datos[1], datos[2]);
        cargarClientes();

        JOptionPane.showMessageDialog(
                this,
                "Cliente agregado correctamente.",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void editarCliente() {
        Integer codigo = obtenerCodigoClienteSeleccionado();

        if (codigo == null) {
            return;
        }

        Cliente cliente = control.obtenerCliente(codigo);

        if (cliente == null) {
            mostrarError("No se encontró el cliente seleccionado.");
            return;
        }

        String[] datos = pedirDatosCliente("Editar cliente", cliente);

        if (datos == null) {
            return;
        }

        boolean actualizado = control.actualizarCliente(codigo, datos[0], datos[1], datos[2]);

        if (!actualizado) {
            mostrarError("No fue posible actualizar el cliente.");
            return;
        }

        cargarClientes();

        JOptionPane.showMessageDialog(
                this,
                "Cliente actualizado correctamente.",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void borrarCliente() {
        Integer codigo = obtenerCodigoClienteSeleccionado();

        if (codigo == null) {
            return;
        }

        Cliente cliente = control.obtenerCliente(codigo);

        if (cliente == null) {
            mostrarError("No se encontró el cliente seleccionado.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea eliminar el cliente " + cliente.getNombre() + "?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        boolean eliminado = control.eliminarCliente(codigo);

        if (!eliminado) {
            mostrarError("No se puede eliminar el cliente porque tiene órdenes asociadas.");
            return;
        }

        cargarClientes();

        JOptionPane.showMessageDialog(
                this,
                "Cliente eliminado correctamente.",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void verCliente() {
        Integer codigo = obtenerCodigoClienteSeleccionado();

        if (codigo == null) {
            return;
        }

        Cliente cliente = control.obtenerCliente(codigo);

        if (cliente == null) {
            mostrarError("No se encontró el cliente seleccionado.");
            return;
        }

        mostrarDetalleCliente(cliente);
    }

    private Integer obtenerCodigoClienteSeleccionado() {
        int filaSeleccionada = tablaClientes.getSelectedRow();

        if (filaSeleccionada == -1) {
            mostrarError("Debe seleccionar un cliente de la tabla.");
            return null;
        }

        int filaModelo = tablaClientes.convertRowIndexToModel(filaSeleccionada);
        Object valor = modeloClientes.getValueAt(filaModelo, 0);

        return Integer.parseInt(valor.toString());
    }

    private String[] pedirDatosCliente(String titulo, Cliente cliente) {
        JTextField txtNombre = new JTextField();
        JTextField txtCorreo = new JTextField();
        JTextField txtTelefono = new JTextField();

        if (cliente != null) {
            txtNombre.setText(cliente.getNombre());
            txtCorreo.setText(cliente.getCorreo());
            txtTelefono.setText(cliente.getTelefono());
        }

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);

        panel.add(new JLabel("Correo:"));
        panel.add(txtCorreo);

        panel.add(new JLabel("Teléfono:"));
        panel.add(txtTelefono);

        while (true) {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    titulo,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (opcion != JOptionPane.OK_OPTION) {
                return null;
            }

            String nombre = txtNombre.getText().trim();
            String correo = txtCorreo.getText().trim();
            String telefono = txtTelefono.getText().trim();

            if (nombre.isEmpty()) {
                mostrarError("El nombre del cliente es obligatorio.");
                continue;
            }

            if (correo.isEmpty()) {
                mostrarError("El correo del cliente es obligatorio.");
                continue;
            }

            if (!correo.contains("@")) {
                mostrarError("El correo debe tener un formato válido.");
                continue;
            }

            if (telefono.isEmpty()) {
                mostrarError("El teléfono del cliente es obligatorio.");
                continue;
            }

            return new String[] { nombre, correo, telefono };
        }
    }

    private void mostrarDetalleCliente(Cliente cliente) {
        JDialog dialog = new JDialog(this, "Detalle cliente", true);

        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel panelDatos = new JPanel(new GridLayout(3, 2, 5, 5));

        panelDatos.add(new JLabel("ID:"));
        panelDatos.add(new JLabel(cliente.getCodigo().toString()));

        panelDatos.add(new JLabel("Nombre:"));
        panelDatos.add(new JLabel(cliente.getNombre()));

        panelDatos.add(new JLabel("Email:"));
        panelDatos.add(new JLabel(cliente.getCorreo()));

        DefaultTableModel modeloOrdenesCliente = new DefaultTableModel(
                new Object[] { "Número", "Fecha", "Estado", "Total" }, 0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        JTable tablaOrdenesCliente = new JTable(modeloOrdenesCliente);
        JScrollPane scrollOrdenes = new JScrollPane(tablaOrdenesCliente);

        JLabel lblTotalPendiente = new JLabel();

        JButton btnTodas = new JButton("Todas");
        JButton btnIniciadas = new JButton("Iniciadas");
        JButton btnPendientes = new JButton("Pendientes");
        JButton btnTerminadas = new JButton("Terminadas");

        btnTodas.addActionListener(
                e -> cargarOrdenesCliente(modeloOrdenesCliente, cliente, null, lblTotalPendiente)
        );

        btnIniciadas.addActionListener(
                e -> cargarOrdenesCliente(modeloOrdenesCliente, cliente, EstadoOrden.INICIADA, lblTotalPendiente)
        );

        btnPendientes.addActionListener(
                e -> cargarOrdenesCliente(modeloOrdenesCliente, cliente, EstadoOrden.PENDIENTE, lblTotalPendiente)
        );

        btnTerminadas.addActionListener(
                e -> cargarOrdenesCliente(modeloOrdenesCliente, cliente, EstadoOrden.TERMINADA, lblTotalPendiente)
        );

        JPanel panelBotonesFiltro = new JPanel(new GridLayout(4, 1, 5, 5));

        panelBotonesFiltro.add(btnTodas);
        panelBotonesFiltro.add(btnIniciadas);
        panelBotonesFiltro.add(btnPendientes);
        panelBotonesFiltro.add(btnTerminadas);

        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));

        panelCentro.add(scrollOrdenes, BorderLayout.CENTER);
        panelCentro.add(panelBotonesFiltro, BorderLayout.EAST);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panelInferior.add(new JLabel("Total pendiente:"));
        panelInferior.add(lblTotalPendiente);

        dialog.add(panelDatos, BorderLayout.NORTH);
        dialog.add(panelCentro, BorderLayout.CENTER);
        dialog.add(panelInferior, BorderLayout.SOUTH);

        cargarOrdenesCliente(modeloOrdenesCliente, cliente, null, lblTotalPendiente);

        dialog.setVisible(true);
    }

    private void cargarOrdenesCliente(
            DefaultTableModel modelo,
            Cliente cliente,
            EstadoOrden filtro,
            JLabel lblTotalPendiente
    ) {
        modelo.setRowCount(0);

        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        double totalPendiente = 0;

        for (OrdenCompra orden : control.listarOrdenesCliente(cliente.getCodigo())) {
            if (orden.getEstado() == EstadoOrden.PENDIENTE) {
                totalPendiente += orden.calcularTotal();
            }

            if (filtro != null && orden.getEstado() != filtro) {
                continue;
            }

            Object[] fila = {
                    orden.getNumero(),
                    orden.getFecha().format(formatoFecha),
                    orden.getEstado(),
                    String.format("%.2f", orden.calcularTotal())
            };

            modelo.addRow(fila);
        }

        lblTotalPendiente.setText(String.format("%.2f", totalPendiente));
    }

    // =========================================================
    // PANEL PRODUCTOS
    // =========================================================

    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        modeloProductos = new DefaultTableModel(
                new Object[] { "Código", "Nombre", "Existencias", "Unidad", "Precio" }, 0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaProductos = new JTable(modeloProductos);
        JScrollPane scrollProductos = new JScrollPane(tablaProductos);

        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 5, 10));

        JButton btnVer = new JButton("Ver");
        JButton btnAgregar = new JButton("Agregar");
        JButton btnEditar = new JButton("Editar");
        JButton btnBorrar = new JButton("Borrar");

        btnVer.addActionListener(e -> verProducto());
        btnAgregar.addActionListener(e -> agregarProducto());
        btnEditar.addActionListener(e -> editarProducto());
        btnBorrar.addActionListener(e -> borrarProducto());

        panelBotones.add(btnVer);
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnBorrar);

        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.add(panelBotones, BorderLayout.NORTH);

        panel.add(scrollProductos, BorderLayout.CENTER);
        panel.add(panelDerecho, BorderLayout.EAST);

        return panel;
    }

    private void cargarProductos() {
        if (modeloProductos == null) {
            return;
        }

        modeloProductos.setRowCount(0);

        List<Producto> productos = control.listarProductos();

        for (Producto producto : productos) {
            Object[] fila = {
                    producto.getCodigo(),
                    producto.getNombre(),
                    String.format("%.2f", producto.getExistencias()),
                    producto.getUnidad(),
                    String.format("%.2f", producto.getPrecio())
            };

            modeloProductos.addRow(fila);
        }
    }

    private void agregarProducto() {
        Object[] datos = pedirDatosProducto("Agregar producto", null);

        if (datos == null) {
            return;
        }

        String nombre = (String) datos[0];
        double existencias = (double) datos[1];
        String unidad = (String) datos[2];
        double precio = (double) datos[3];

        control.crearProducto(nombre, existencias, unidad, precio);

        cargarProductos();

        JOptionPane.showMessageDialog(
                this,
                "Producto agregado correctamente.",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void editarProducto() {
        Integer codigo = obtenerCodigoProductoSeleccionado();

        if (codigo == null) {
            return;
        }

        Producto producto = control.obtenerProducto(codigo);

        if (producto == null) {
            mostrarError("No se encontró el producto seleccionado.");
            return;
        }

        Object[] datos = pedirDatosProducto("Editar producto", producto);

        if (datos == null) {
            return;
        }

        String nombre = (String) datos[0];
        double existencias = (double) datos[1];
        String unidad = (String) datos[2];
        double precio = (double) datos[3];

        boolean actualizado = control.actualizarProducto(
                codigo,
                nombre,
                existencias,
                unidad,
                precio
        );

        if (!actualizado) {
            mostrarError("No fue posible actualizar el producto.");
            return;
        }

        cargarProductos();

        JOptionPane.showMessageDialog(
                this,
                "Producto actualizado correctamente.",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void borrarProducto() {
        Integer codigo = obtenerCodigoProductoSeleccionado();

        if (codigo == null) {
            return;
        }

        Producto producto = control.obtenerProducto(codigo);

        if (producto == null) {
            mostrarError("No se encontró el producto seleccionado.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea eliminar el producto " + producto.getNombre() + "?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        boolean eliminado = control.eliminarProducto(codigo);

        if (!eliminado) {
            mostrarError("No se puede borrar el producto porque está siendo utilizado en una orden de compra.");
            return;
        }

        cargarProductos();

        JOptionPane.showMessageDialog(
                this,
                "Producto eliminado correctamente.",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void verProducto() {
        Integer codigo = obtenerCodigoProductoSeleccionado();

        if (codigo == null) {
            return;
        }

        Producto producto = control.obtenerProducto(codigo);

        if (producto == null) {
            mostrarError("No se encontró el producto seleccionado.");
            return;
        }

        String mensaje = "Código: " + producto.getCodigo()
                + "\nNombre: " + producto.getNombre()
                + "\nExistencias: " + producto.getExistencias()
                + "\nUnidad: " + producto.getUnidad()
                + "\nPrecio: " + producto.getPrecio();

        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Detalle producto",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private Integer obtenerCodigoProductoSeleccionado() {
        int filaSeleccionada = tablaProductos.getSelectedRow();

        if (filaSeleccionada == -1) {
            mostrarError("Debe seleccionar un producto de la tabla.");
            return null;
        }

        int filaModelo = tablaProductos.convertRowIndexToModel(filaSeleccionada);
        Object valor = modeloProductos.getValueAt(filaModelo, 0);

        return Integer.parseInt(valor.toString());
    }

    private Object[] pedirDatosProducto(String titulo, Producto producto) {
        JTextField txtNombre = new JTextField();
        JTextField txtExistencias = new JTextField();

        JComboBox<String> cmbUnidad = new JComboBox<>(
                new String[] { "Unidad", "kg", "g", "m", "cm", "L", "mL" }
        );

        JTextField txtPrecio = new JTextField();

        if (producto != null) {
            txtNombre.setText(producto.getNombre());
            txtExistencias.setText(String.valueOf(producto.getExistencias()));
            cmbUnidad.setSelectedItem(producto.getUnidad());
            txtPrecio.setText(String.valueOf(producto.getPrecio()));
        }

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));

        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);

        panel.add(new JLabel("Existencias:"));
        panel.add(txtExistencias);

        panel.add(new JLabel("Unidad:"));
        panel.add(cmbUnidad);

        panel.add(new JLabel("Precio:"));
        panel.add(txtPrecio);

        while (true) {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    titulo,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (opcion != JOptionPane.OK_OPTION) {
                return null;
            }

            String nombre = txtNombre.getText().trim();

            if (nombre.isEmpty()) {
                mostrarError("El nombre del producto es obligatorio.");
                continue;
            }

            try {
                double existencias = convertirDecimal(txtExistencias.getText());
                double precio = convertirDecimal(txtPrecio.getText());

                if (existencias < 0) {
                    mostrarError("Las existencias no pueden ser negativas.");
                    continue;
                }

                if (precio <= 0) {
                    mostrarError("El precio debe ser mayor que cero.");
                    continue;
                }

                String unidad = cmbUnidad.getSelectedItem().toString();

                return new Object[] { nombre, existencias, unidad, precio };

            } catch (NumberFormatException e) {
                mostrarError("Las existencias y el precio deben ser valores numéricos.");
            }
        }
    }

    // =========================================================
    // PANEL ORDENES
    // =========================================================

    private JPanel crearPanelOrdenes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        modeloOrdenes = new DefaultTableModel(
                new Object[] { "Número", "Fecha", "Cliente", "Estado", "Subtotal", "Impuesto", "Total" }, 0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaOrdenes = new JTable(modeloOrdenes);
        JScrollPane scrollOrdenes = new JScrollPane(tablaOrdenes);

        JPanel panelBotones = new JPanel(new GridLayout(5, 1, 5, 10));

        JButton btnNueva = new JButton("Nueva");
        JButton btnDetalle = new JButton("Detalle");
        JButton btnBorrar = new JButton("Borrar");
        JButton btnPendiente = new JButton("Pendiente");
        JButton btnTerminada = new JButton("Terminada");

        btnNueva.addActionListener(e -> crearOrden());
        btnDetalle.addActionListener(e -> verDetalleOrden());
        btnBorrar.addActionListener(e -> borrarOrden());
        btnPendiente.addActionListener(e -> ponerOrdenPendiente());
        btnTerminada.addActionListener(e -> ponerOrdenTerminada());

        panelBotones.add(btnNueva);
        panelBotones.add(btnDetalle);
        panelBotones.add(btnBorrar);
        panelBotones.add(btnPendiente);
        panelBotones.add(btnTerminada);

        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.add(panelBotones, BorderLayout.NORTH);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInferior.add(new JLabel("Total pendiente:"));

        lblTotalPendienteOrdenes = new JLabel("0.00");
        panelInferior.add(lblTotalPendienteOrdenes);

        panel.add(scrollOrdenes, BorderLayout.CENTER);
        panel.add(panelDerecho, BorderLayout.EAST);
        panel.add(panelInferior, BorderLayout.SOUTH);

        return panel;
    }
    
    private void cargarOrdenes() {
        if (modeloOrdenes == null) {
            return;
        }

        modeloOrdenes.setRowCount(0);

        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (OrdenCompra orden : control.listarOrdenes()) {
            Object[] fila = {
                    orden.getNumero(),
                    orden.getFecha().format(formatoFecha),
                    orden.getCliente().getNombre(),
                    orden.getEstado(),
                    String.format("%.2f", orden.calcularSubtotal()),
                    String.format("%.2f", orden.calcularImpuesto()),
                    String.format("%.2f", orden.calcularTotal())
            };

            modeloOrdenes.addRow(fila);
        }

        lblTotalPendienteOrdenes.setText(
                String.format("%.2f", control.obtenerMontoTotalPendiente())
        );
    }

    private void crearOrden() {
        if (control.listarClientes().isEmpty()) {
            mostrarError("Debe registrar al menos un cliente antes de crear una orden.");
            return;
        }

        Integer codigoCliente = pedirClienteParaOrden();

        if (codigoCliente == null) {
            return;
        }

        OrdenCompra orden = control.crearOrden(codigoCliente);

        if (orden == null) {
            mostrarError("No fue posible crear la orden.");
            return;
        }

        cargarOrdenes();
        cargarClientes();

        JOptionPane.showMessageDialog(
                this,
                "Orden creada correctamente. Número de orden: " + orden.getNumero(),
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );

        mostrarDetalleOrden(orden);
    }

    private Integer pedirClienteParaOrden() {
        List<Cliente> clientes = control.listarClientes();

        JComboBox<String> cmbClientes = new JComboBox<>();

        for (Cliente cliente : clientes) {
            cmbClientes.addItem(cliente.getCodigo() + " - " + cliente.getNombre());
        }

        int opcion = JOptionPane.showConfirmDialog(
                this,
                cmbClientes,
                "Seleccione el cliente",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return null;
        }

        String seleccionado = cmbClientes.getSelectedItem().toString();
        String codigoTexto = seleccionado.split(" - ")[0];

        return Integer.parseInt(codigoTexto);
    }

    private void verDetalleOrden() {
        Integer numeroOrden = obtenerNumeroOrdenSeleccionada();

        if (numeroOrden == null) {
            return;
        }

        OrdenCompra orden = control.obtenerOrden(numeroOrden);

        if (orden == null) {
            mostrarError("No se encontró la orden seleccionada.");
            return;
        }

        mostrarDetalleOrden(orden);
    }

    private void borrarOrden() {
        Integer numeroOrden = obtenerNumeroOrdenSeleccionada();

        if (numeroOrden == null) {
            return;
        }

        OrdenCompra orden = control.obtenerOrden(numeroOrden);

        if (orden == null) {
            mostrarError("No se encontró la orden seleccionada.");
            return;
        }

        if (orden.getEstado() == EstadoOrden.TERMINADA) {
            mostrarError("No se puede borrar una orden terminada.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea borrar la orden número " + orden.getNumero() + "?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        boolean eliminada = control.eliminarOrden(numeroOrden);

        if (!eliminada) {
            mostrarError("No fue posible borrar la orden.");
            return;
        }

        cargarOrdenes();
        cargarProductos();
        cargarClientes();

        JOptionPane.showMessageDialog(
                this,
                "Orden eliminada correctamente.",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void ponerOrdenPendiente() {
        Integer numeroOrden = obtenerNumeroOrdenSeleccionada();

        if (numeroOrden == null) {
            return;
        }

        OrdenCompra orden = control.obtenerOrden(numeroOrden);

        if (orden == null) {
            mostrarError("No se encontró la orden seleccionada.");
            return;
        }

        if (orden.getEstado() != EstadoOrden.INICIADA) {
            mostrarError("Solo las órdenes iniciadas pueden pasar a pendiente.");
            return;
        }

        if (orden.getLineas().isEmpty()) {
            mostrarError("No se puede poner pendiente una orden sin productos.");
            return;
        }

        boolean actualizado = control.ponerOrdenPendiente(numeroOrden);

        if (!actualizado) {
            mostrarError("No fue posible cambiar la orden a pendiente.");
            return;
        }

        cargarOrdenes();
        cargarClientes();

        JOptionPane.showMessageDialog(
                this,
                "La orden pasó a estado pendiente.",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void ponerOrdenTerminada() {
        Integer numeroOrden = obtenerNumeroOrdenSeleccionada();

        if (numeroOrden == null) {
            return;
        }

        OrdenCompra orden = control.obtenerOrden(numeroOrden);

        if (orden == null) {
            mostrarError("No se encontró la orden seleccionada.");
            return;
        }

        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            mostrarError("Solo las órdenes pendientes pueden pasar a terminada.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea terminar la orden número " + orden.getNumero() + "?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        boolean actualizado = control.ponerOrdenTerminada(numeroOrden);

        if (!actualizado) {
            mostrarError("No fue posible terminar la orden.");
            return;
        }

        cargarOrdenes();
        cargarClientes();

        JOptionPane.showMessageDialog(
                this,
                "La orden fue terminada correctamente.",
                "Información",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private Integer obtenerNumeroOrdenSeleccionada() {
        int filaSeleccionada = tablaOrdenes.getSelectedRow();

        if (filaSeleccionada == -1) {
            mostrarError("Debe seleccionar una orden de la tabla.");
            return null;
        }

        int filaModelo = tablaOrdenes.convertRowIndexToModel(filaSeleccionada);
        Object valor = modeloOrdenes.getValueAt(filaModelo, 0);

        return Integer.parseInt(valor.toString());
    }
    
    private void mostrarDetalleOrden(OrdenCompra orden) {
        JDialog dialog = new JDialog(this, "Detalle orden de compra", true);

        dialog.setSize(750, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JLabel lblCliente = new JLabel("Cliente: " + orden.getCliente().getCodigo() + " - " + orden.getCliente().getNombre());
        JLabel lblNumero = new JLabel("Número orden: " + orden.getNumero());
        JLabel lblEstado = new JLabel("Estado: " + orden.getEstado());

        JPanel panelDatos = new JPanel(new GridLayout(3, 1, 5, 5));
        panelDatos.add(lblCliente);
        panelDatos.add(lblNumero);
        panelDatos.add(lblEstado);

        DefaultTableModel modeloLineas = new DefaultTableModel(
                new Object[] { "#", "Código producto", "Producto", "Cantidad", "Precio", "Costo" }, 0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        JTable tablaLineas = new JTable(modeloLineas);
        JScrollPane scrollLineas = new JScrollPane(tablaLineas);

        JLabel lblSubtotal = new JLabel();
        JLabel lblImpuesto = new JLabel();
        JLabel lblTotal = new JLabel();

        JPanel panelTotales = new JPanel(new GridLayout(3, 2, 5, 5));
        panelTotales.add(new JLabel("Costo:"));
        panelTotales.add(lblSubtotal);
        panelTotales.add(new JLabel("Impuesto:"));
        panelTotales.add(lblImpuesto);
        panelTotales.add(new JLabel("Total:"));
        panelTotales.add(lblTotal);

        JButton btnAgregar = new JButton("Agregar");
        JButton btnEditar = new JButton("Editar");
        JButton btnBorrar = new JButton("Borrar");
        JButton btnPendiente = new JButton("Pendiente");
        JButton btnTerminar = new JButton("Terminar");

        boolean puedeEditar = orden.getEstado() == EstadoOrden.INICIADA;

        btnAgregar.setEnabled(puedeEditar);
        btnEditar.setEnabled(puedeEditar);
        btnBorrar.setEnabled(puedeEditar);
        btnPendiente.setEnabled(orden.getEstado() == EstadoOrden.INICIADA);
        btnTerminar.setEnabled(orden.getEstado() == EstadoOrden.PENDIENTE);

        btnAgregar.addActionListener(e -> {
            agregarLineaDesdeDetalle(orden, modeloLineas, lblSubtotal, lblImpuesto, lblTotal);
            lblEstado.setText("Estado: " + orden.getEstado());
        });

        btnEditar.addActionListener(e -> {
            editarLineaDesdeDetalle(orden, tablaLineas, modeloLineas, lblSubtotal, lblImpuesto, lblTotal);
        });

        btnBorrar.addActionListener(e -> {
            borrarLineaDesdeDetalle(orden, tablaLineas, modeloLineas, lblSubtotal, lblImpuesto, lblTotal);
        });

        btnPendiente.addActionListener(e -> {
            if (orden.getLineas().isEmpty()) {
                mostrarError("No se puede poner pendiente una orden sin productos.");
                return;
            }

            boolean actualizado = control.ponerOrdenPendiente(orden.getNumero());

            if (!actualizado) {
                mostrarError("No fue posible cambiar la orden a pendiente.");
                return;
            }

            lblEstado.setText("Estado: " + orden.getEstado());

            btnAgregar.setEnabled(false);
            btnEditar.setEnabled(false);
            btnBorrar.setEnabled(false);
            btnPendiente.setEnabled(false);
            btnTerminar.setEnabled(true);

            cargarOrdenes();
            cargarClientes();

            JOptionPane.showMessageDialog(
                    dialog,
                    "La orden pasó a estado pendiente.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        btnTerminar.addActionListener(e -> {
            boolean actualizado = control.ponerOrdenTerminada(orden.getNumero());

            if (!actualizado) {
                mostrarError("No fue posible terminar la orden.");
                return;
            }

            lblEstado.setText("Estado: " + orden.getEstado());

            btnAgregar.setEnabled(false);
            btnEditar.setEnabled(false);
            btnBorrar.setEnabled(false);
            btnPendiente.setEnabled(false);
            btnTerminar.setEnabled(false);

            cargarOrdenes();
            cargarClientes();

            JOptionPane.showMessageDialog(
                    dialog,
                    "La orden fue terminada correctamente.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        JPanel panelBotones = new JPanel(new GridLayout(5, 1, 5, 10));
        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnBorrar);
        panelBotones.add(btnPendiente);
        panelBotones.add(btnTerminar);

        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.add(panelBotones, BorderLayout.NORTH);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelTotales, BorderLayout.EAST);

        dialog.add(panelDatos, BorderLayout.NORTH);
        dialog.add(scrollLineas, BorderLayout.CENTER);
        dialog.add(panelDerecho, BorderLayout.EAST);
        dialog.add(panelInferior, BorderLayout.SOUTH);

        cargarLineasOrden(orden, modeloLineas, lblSubtotal, lblImpuesto, lblTotal);

        dialog.setVisible(true);
    }

    private void cargarLineasOrden(
            OrdenCompra orden,
            DefaultTableModel modeloLineas,
            JLabel lblSubtotal,
            JLabel lblImpuesto,
            JLabel lblTotal
    ) {
        modeloLineas.setRowCount(0);

        List<LineaOrden> lineas = control.obtenerLineasOrden(orden.getNumero());

        int posicion = 1;

        for (LineaOrden linea : lineas) {
            Producto producto = linea.getProducto();

            Object[] fila = {
                    posicion,
                    producto.getCodigo(),
                    producto.getNombre(),
                    String.format("%.2f", linea.getCantidad()),
                    String.format("%.2f", linea.getPrecioUnitario()),
                    String.format("%.2f", linea.calcularCosto())
            };

            modeloLineas.addRow(fila);
            posicion++;
        }

        lblSubtotal.setText(String.format("%.2f", orden.calcularSubtotal()));
        lblImpuesto.setText(String.format("%.2f", orden.calcularImpuesto()));
        lblTotal.setText(String.format("%.2f", orden.calcularTotal()));

        cargarOrdenes();
        cargarProductos();
    }
    private void agregarLineaDesdeDetalle(
            OrdenCompra orden,
            DefaultTableModel modeloLineas,
            JLabel lblSubtotal,
            JLabel lblImpuesto,
            JLabel lblTotal
    ) {
        if (control.listarProductos().isEmpty()) {
            mostrarError("Debe registrar al menos un producto antes de agregar líneas.");
            return;
        }

        JComboBox<String> cmbProductos = new JComboBox<>();

        for (Producto producto : control.listarProductos()) {
            cmbProductos.addItem(
                    producto.getCodigo()
                            + " - "
                            + producto.getNombre()
                            + " | Existencias: "
                            + String.format("%.2f", producto.getExistencias())
                            + " "
                            + producto.getUnidad()
            );
        }

        JTextField txtCantidad = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Producto:"));
        panel.add(cmbProductos);
        panel.add(new JLabel("Cantidad:"));
        panel.add(txtCantidad);

        while (true) {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    "Agregar línea",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }

            try {
                String seleccionado = cmbProductos.getSelectedItem().toString();
                Integer codigoProducto = Integer.parseInt(seleccionado.split(" - ")[0]);

                double cantidad = convertirDecimal(txtCantidad.getText());

                if (cantidad <= 0) {
                    mostrarError("La cantidad debe ser mayor que cero.");
                    continue;
                }

                boolean agregado = control.agregarLineaOrden(
                        orden.getNumero(),
                        codigoProducto,
                        cantidad
                );

                if (!agregado) {
                    mostrarError("No fue posible agregar la línea. Revise que exista stock suficiente.");
                    continue;
                }

                cargarLineasOrden(orden, modeloLineas, lblSubtotal, lblImpuesto, lblTotal);
                cargarProductos();

                return;

            } catch (NumberFormatException e) {
                mostrarError("La cantidad debe ser un valor numérico.");
            }
        }
    }

    private void editarLineaDesdeDetalle(
            OrdenCompra orden,
            JTable tablaLineas,
            DefaultTableModel modeloLineas,
            JLabel lblSubtotal,
            JLabel lblImpuesto,
            JLabel lblTotal
    ) {
        int filaSeleccionada = tablaLineas.getSelectedRow();

        if (filaSeleccionada == -1) {
            mostrarError("Debe seleccionar una línea de la orden.");
            return;
        }

        int filaModelo = tablaLineas.convertRowIndexToModel(filaSeleccionada);

        Integer posicion = Integer.parseInt(modeloLineas.getValueAt(filaModelo, 0).toString());
        String producto = modeloLineas.getValueAt(filaModelo, 2).toString();
        String cantidadActual = modeloLineas.getValueAt(filaModelo, 3).toString();

        JTextField txtCantidad = new JTextField(cantidadActual);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Producto:"));
        panel.add(new JLabel(producto));
        panel.add(new JLabel("Nueva cantidad:"));
        panel.add(txtCantidad);

        while (true) {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    "Editar línea",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }

            try {
                double nuevaCantidad = convertirDecimal(txtCantidad.getText());

                if (nuevaCantidad <= 0) {
                    mostrarError("La cantidad debe ser mayor que cero.");
                    continue;
                }

                boolean actualizado = control.actualizarLineaOrden(
                        orden.getNumero(),
                        posicion,
                        nuevaCantidad
                );

                if (!actualizado) {
                    mostrarError("No fue posible editar la línea. Revise el stock disponible.");
                    continue;
                }

                cargarLineasOrden(orden, modeloLineas, lblSubtotal, lblImpuesto, lblTotal);
                cargarProductos();

                return;

            } catch (NumberFormatException e) {
                mostrarError("La cantidad debe ser un valor numérico.");
            }
        }
    }

    private void borrarLineaDesdeDetalle(
            OrdenCompra orden,
            JTable tablaLineas,
            DefaultTableModel modeloLineas,
            JLabel lblSubtotal,
            JLabel lblImpuesto,
            JLabel lblTotal
    ) {
        int filaSeleccionada = tablaLineas.getSelectedRow();

        if (filaSeleccionada == -1) {
            mostrarError("Debe seleccionar una línea de la orden.");
            return;
        }

        int filaModelo = tablaLineas.convertRowIndexToModel(filaSeleccionada);

        Integer posicion = Integer.parseInt(modeloLineas.getValueAt(filaModelo, 0).toString());

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea borrar la línea seleccionada?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        boolean eliminado = control.eliminarLineaOrden(orden.getNumero(), posicion);

        if (!eliminado) {
            mostrarError("No fue posible borrar la línea.");
            return;
        }

        cargarLineasOrden(orden, modeloLineas, lblSubtotal, lblImpuesto, lblTotal);
        cargarProductos();
    }

    // =========================================================
    // DATOS DE PRUEBA
    // =========================================================

    private void cargarDatosPrueba() {
        if (!control.listarClientes().isEmpty()) {
            return;
        }

        Cliente cliente1 = control.crearCliente(
                "Pablo Quiroz Saborio",
                "pquiroz22@gmail.com",
                "8457-4589"
        );

        Cliente cliente2 = control.crearCliente(
                "Daniel Ramirez Soto",
                "danielrs@gmail.com",
                "7018-4759"
        );

        control.crearCliente(
                "Manuel Cordero Araya",
                "manu.ara@gmail.com",
                "8845-7123"
        );

        Producto producto1 = control.crearProducto(
                "Teclado",
                20,
                "Unidad",
                8500
        );

        Producto producto2 = control.crearProducto(
                "Mouse",
                30,
                "Unidad",
                4500
        );
        Producto producto3 = control.crearProducto(
                "Smart TV",
                10,
                "Unidad",
                60000
        );

        OrdenCompra orden1 = control.crearOrden(cliente1.getCodigo());
        control.agregarLineaOrden(orden1.getNumero(), producto1.getCodigo(), 1);
        control.ponerOrdenPendiente(orden1.getNumero());

        OrdenCompra orden2 = control.crearOrden(cliente1.getCodigo());
        control.agregarLineaOrden(orden2.getNumero(), producto2.getCodigo(), 2);

        OrdenCompra orden3 = control.crearOrden(cliente2.getCodigo());
        control.agregarLineaOrden(orden3.getNumero(), producto1.getCodigo(), 1);
        control.ponerOrdenPendiente(orden3.getNumero());
        
        OrdenCompra orden4 = control.crearOrden(cliente1.getCodigo());
        control.agregarLineaOrden(orden4.getNumero(), producto3.getCodigo(), 1);
        control.ponerOrdenPendiente(orden4.getNumero());
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private double convertirDecimal(String texto) {
        if (texto == null) {
            throw new NumberFormatException();
        }

        texto = texto.trim().replace(",", ".");

        if (texto.isEmpty()) {
            throw new NumberFormatException();
        }

        double valor = Double.parseDouble(texto);

        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw new NumberFormatException();
        }

        return valor;
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}