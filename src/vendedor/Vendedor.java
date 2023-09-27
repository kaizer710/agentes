/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vendedor;
import jade.core.Agent;
import java.util.Hashtable;
import jade.core.behaviours.OneShotBehaviour;
import gui.BookSellerGui;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import java.util.HashSet;
/**
 *
 * @author kaize
 */
public class Vendedor extends Agent {
    
    private Hashtable catalogo;
    private BookSellerGui miGUI;
    
    protected void setup()
    {
        System.out.println("Hola, Soy el agente Vendedor: " + getAID().getName());
        catalogo = new Hashtable();
        miGUI = new BookSellerGui(this);
        miGUI.showGui();
        
        addBehaviour(new ServidorOfertas());
        
        addBehaviour(new ServidorCompras());
        }
    
    protected void takeDown()
    {
        System.out.println("Finalizando el agente vendedor: " + getAID().getName());
        miGUI.dispose();
    }
    
    public void updateCatalogue(final String titulo, final int precio)
    {
        addBehaviour(new OneShotBehaviour()
        {
            public void action(){
                catalogo.put(titulo,precio);
                System.out.println(titulo + " ah sido insertado en el catalogo con el precio " + precio);
            }
        }
        );
    }
    
    private class ServidorOfertas extends CyclicBehaviour{
        
        public void action(){
            MessageTemplate mt= MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg= receive(mt);
            
            if(msg != null){
                String titulo = msg.getContent();
                ACLMessage reply = msg.createReply();
                Integer precio = (Integer) catalogo.get(titulo);
                
                if(precio != null){
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(precio.intValue()));
                    
                }
                else{
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("Libro-no-disponible");
                    
                }
                myAgent.send(reply);
            }
            else{
                block();
            }
        }
        
    }
    
    private class ServidorCompras extends CyclicBehaviour{
        public void action(){
            
            MessageTemplate mt= MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = receive (mt);
            
            if(msg != null){
                String titulo = msg.getContent();
                ACLMessage reply = msg.createReply();
                Integer precio = (Integer) catalogo.remove(titulo);
                
                if(precio != null){
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(titulo + " vendido al agente" + msg.getSender().getName());
                                        
                }
                
                else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("libro-no-disponible");
                }
                
                send(reply);
            }
            
            else{
                block();
            }
            
        }
    }

    
}
