/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comprador;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;

/**
 *
 * @author kaize
 */
public class Comprador extends Agent
{
    private String titulo;
    
    protected void setup(){
        System.out.println("Hola, soy el agente comprador: " + getAID().getName());
        
       
        
        Object[] args = getArguments();
        if(args != null && args.length > 0){
            titulo = (String)args[0];
            System.out.println("Vamos a intentar comprar el libro " + titulo);
            addBehaviour(new TickerBehaviour (this, 10000){
                
                protected void onTick(){
                        System.out.println("Enviando petición a posibles clientes");
                        myAgent.addBehaviour(new RequestPerformer());
                                }
            }
            );
        }
        else {
            System.out.println("No se ha especificado un titulo que comprar ");
            doDelete();
        }
        
        
           
    }
    protected void takeDown(){
        System.out.println("Finalizando agente comprador: " + getAID().getName());
    }
    
   private class RequestPerformer extends Behaviour {
       private int mejorPrecio;
       private AID mejorVendedor;
       private int numRespuestas = 0;
       private int etapa = 0;
       private MessageTemplate mt;
       
       
       /*ACLMessage cfp=new ACLMessage(ACLMessage.CFP);
       cfp.addReceiver(new AID("Vendedor-1",AID.ISLOCALNAME));
       cfp.addReceiver(new AID("Vendedor-2",AID.ISLOCALNAME));
       cfp.setContent(titulo);
       cfp.setConvertationID("Compra-venta-libros");
       cfp.setReplyWith("cfp"+System.currentTimeMillis());
       myAgent.send(cfp);*/
       public void action(){
           
           switch(etapa){
               case 0:
                   ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                   cfp.addReceiver(new AID("vendedor-1",AID.ISLOCALNAME));
                   cfp.addReceiver(new AID("vendedor-2",AID.ISLOCALNAME));
                   cfp.setContent(titulo);
                   cfp.setReplyWith("cfp" + System.currentTimeMillis());
                   myAgent.send(cfp);
                   
                   //plantilla para la siguiente etapa
                   mt = MessageTemplate.and(MessageTemplate.MatchConversationId("compra-venta-libros"),
                           MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                   etapa = 1;
                 break;
               case 1: //recibiendo propuestas de los vendedores
               ACLMessage reply = receive(mt);
               
               if (reply != null){
                   
                   if (reply.getPerformative() == ACLMessage.PROPOSE){
                       int precio = Integer.parseInt(reply.getContent());
                       if (mejorVendedor == null || precio < mejorPrecio){
                           mejorPrecio = precio;
                           mejorVendedor = reply.getSender();
                       }
                   }
                   numRespuestas++;
                   if (numRespuestas >= 2) { // numero de agentes son 2 por ahora
                       etapa = 2;
                   }
               } else {
                   block();
               }
               break;
               case 2: // envio de la orden compra al mejor vendedor
                   ACLMessage orden = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                   orden.addReceiver(mejorVendedor);
                   orden.setContent(titulo);
                   orden.setConversationId("compra-venta-libros");
                   orden.setReplyWith("orden" + System.currentTimeMillis());
                   myAgent.send(orden);
                   
                   //siguiente etapa
                   
                   mt = MessageTemplate.and(MessageTemplate.MatchConversationId("compra-venta-libros"),
                           MessageTemplate.MatchInReplyTo(orden.getReplyWith()));
                   etapa = 3;
                           
                break;
                
               case 3: //recibiendo informaciòn
                   
                   ACLMessage informe = receive (mt);
                   
                   if (informe != null){
                       if(informe.getPerformative() == ACLMessage.INFORM){
                           System.out.println(titulo + " ah sido comprado por un precio de: " + mejorPrecio);
                           myAgent.doDelete();
                       }
                       etapa = 4;
                   } else {
                       block();
                   }
                break;
           }
           
               
           
       }
       public boolean done(){
           return ((etapa == 2 && mejorVendedor == null) || etapa == 4);
           
       }
      
   }
}
