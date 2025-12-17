package com.mycompany.karbobucks;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.mycompany.karbobucks.ClasesAuxiliares.Producto;

public class TarjetaProducto extends JPanel {

    private String URL_WEB = "http://dam2.colexio-karbo.com:7000/jalban/Examen1/Imagenes/";//Url web de las imagenes

    private PanelCamarero panelCamarero;

    private Producto producto;

    public TarjetaProducto(Producto p, PanelCamarero panelCamarero) {

        this.panelCamarero = panelCamarero;
        this.producto = p;

        SetUp();

    }

    private void SetUp() {//Inicializa la tarjeta de producto
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        setPreferredSize(new Dimension(120, 180));
        setBackground(Color.WHITE);

        // --- 1. Espacio para la Imagen ---
        JLabel imageLabel = new JLabel("Cargando Imagen...", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        imageLabel.setForeground(Color.DARK_GRAY);
        imageLabel.setPreferredSize(new Dimension(100, 100));
        add(imageLabel, BorderLayout.CENTER);

        // --- 2. Información del Producto ---
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel("<html><b>" + producto.Nombre + "</b></html>");
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setForeground(Color.BLACK);

        JLabel priceLabel = new JLabel(String.format("€%.2f", producto.Precio), SwingConstants.CENTER);
        priceLabel.setForeground(new Color(0, 128, 0)); // Verde oscuro para mejor contraste
        priceLabel.setFont(new Font("Arial", Font.BOLD, 12));

        infoPanel.add(nameLabel, BorderLayout.NORTH);
        infoPanel.add(priceLabel, BorderLayout.SOUTH);

        add(infoPanel, BorderLayout.SOUTH);

        // --- 3. Acción al hacer click ---
        // Usamos MouseListener en el JPanel para simular un botón de TPV
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Lógica de añadir el producto
                panelCamarero.listaDatosProductosPedidoActual.addElement(producto);
                panelCamarero.ActualizarTotal();
            }

            // Efecto visual simple al pasar el ratón (opcional)
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(230, 240, 255)); // Color al pasar el ratón
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.WHITE);
            }
        });

        // --- 4. Carga Asíncrona de la Imagen ---
        ProcesamientoImagen(imageLabel);
    }

    private void ProcesamientoImagen(JLabel imageLabel) {//Cargamos la imagen de forma asíncrona
        new Thread(() -> {//nuevo hilo para cargar la imagen
            try {

                String imageUrl = URL_WEB + producto.Foto_1;// Url completa para descargar la imagen

                ImageIcon icon = loadImage(imageUrl);//cargamos la imagen

                SwingUtilities.invokeLater(() -> {//actualizamos la imagen
                    if (icon != null) {
                        imageLabel.setIcon(icon);
                        imageLabel.setText(""); // Oculta "Cargando Imagen..."
                    } else {//Si no hay foto
                        imageLabel.setText("No Foto");
                        imageLabel.setForeground(Color.DARK_GRAY);
                    }
                    this.revalidate();
                    this.repaint();
                });
            } catch (Exception ex) {
                System.err.println("Error al cargar imagen para " + producto.Nombre + ": " + ex.getMessage());
                SwingUtilities.invokeLater(() -> {//actualizamos la imagen
                    imageLabel.setText("Error");
                    imageLabel.setForeground(Color.RED);
                    this.revalidate();
                    this.repaint();
                });
            }
        }).start();
    }

    private ImageIcon loadImage(String urlString) {//cargamos la imagen desde la url
        try {

            URL url = new URL(urlString);//la transformamos de string a url
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();//abrimos la conexión

            // Simular un navegador real para que el servidor no nos bloquee
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");//simulamos un navegador real
            connection.setConnectTimeout(5000);//timeout de 5 segundos
            connection.setReadTimeout(5000);//timeout de 5 segundos
            connection.connect();

            int status = connection.getResponseCode();
            if (status == 200) {

                // Al usar ImageIO.read(InputStream), Java detecta automáticamente
                // TwelveMonkeys para decodificar WebP.
                try (InputStream in = connection.getInputStream()) {
                    BufferedImage originalImage = ImageIO.read(in);//leemos la imagen

                    if (originalImage != null) {
                        // Redimensionar a 100x100
                        Image scaled = originalImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);//redimensionamos la imagen
                        return new ImageIcon(scaled);//devolvemos la imagen redimensionada
                    } else {
                        System.err.println("Fallo: La librería no pudo decodificar la imagen.");//error
                    }
                }
            } else {
                System.err.println("Error HTTP: " + status);
            }
        } catch (Exception e) {
            System.err.println("Error de red o formato: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Devolverá null y saldrá "No Foto" si falla
    }
}
