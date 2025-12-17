

public class TiempoCalcular {


    float tiempoTotal;//tiempo total del pedido

    int tiempoPorProducto = 5*60;//5 minutos

    public TiempoCalcular(int numeroProductos) {
        //Se calcula el tiempo total del pedido
        tiempoTotal=calcularTiempo(numeroProductos);
        //Se muestra el tiempo total del pedido en consola
        System.out.println(tiempoTotal);
        //Se sale del programa
        System.exit(0);
    }


    public static void main(String[] args) {
        //Si no hay argumentos, se sale con código 1 de error 
        if(args[0] == null) {
            System.out.println("No se ha introducido ningun argumento");
            System.exit(1);
            return;
        }

        //Si existe argumentos, se ejecuta el programa,primero se parsea el argumento a int
        int numeroProductos = Integer.parseInt(args[0]);
        //Se crea un objeto TiempoCalcular con el numero de productos
        new TiempoCalcular(numeroProductos);


    }

    //Esta funcion calcula el tiempo total de un pedido y devuelve el resultado en float
    private float calcularTiempo(int numeroProductos) {
       return numeroProductos * this.tiempoPorProducto;
    
    }
}