package cmiyc;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Service implements Runnable {
    private int period;
    private int functionId;
    
    public Service(int period, int functionId){
        this.period=period;
        this.functionId=functionId;
    }
    
    @Override
    public void run() {
        try {
            System.out.println("Start:"+new Date());
            Thread.sleep(this.period);
            switch(functionId){
                case 1:  sendPeriodic();
                break;
                case 2:  notifyAutomatically();
                break;
            }
            System.out.println("End:"+new Date());
        } catch (InterruptedException ex) {
            Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void sendPeriodic() {
        System.out.println("A enviar informação periódica...");
    }
    
    private void notifyAutomatically(){
        //se as coordenadas estiverem no sitio certo ele alerta automaticamente de proximidade, ancoras, whatever
        System.out.println("Notificando automaticamente...");
    }
    
    public static void main(String args[]){
        Thread t = new Thread(new Service(30000,1));
        t.start();
    }
}
