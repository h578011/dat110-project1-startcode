package no.hvl.dat110.rpc;

import java.util.HashMap;

import no.hvl.dat110.messaging.Connection;
import no.hvl.dat110.messaging.MessagingServer;
import no.hvl.dat110.messaging.UnboundedMessage;

public class RPCServer {

	private MessagingServer msgserver;
	private Connection connection;

	// hashmap to register RPC methods which are required to implement RPCImpl

	private HashMap<Integer,RPCImpl> services;

	public RPCServer(int port) {

		this.msgserver = new MessagingServer(port);
		this.services = new HashMap<Integer,RPCImpl>();

		// the stop RPC methods is built into the server
		services.put((int)RPCCommon.RPIDSTOP,new RPCServerStopImpl());
	}

	public void run() {

		System.out.println("RPC SERVER RUN - Services: " + services.size());

		connection = msgserver.accept(); 

		System.out.println("RPC SERVER ACCEPTED");

		boolean stop = false;

		while (!stop) {

			int rpcid;

			// - receive message containing RPC request
			UnboundedMessage msg = connection.receive();
			// - find the identifier for the RPC methods to invoke
			rpcid = msg.getData()[0];

			// - lookup the method to be invoked
			byte[] reply = null;
			
			if(services.containsKey(rpcid)) {
				
				// - invoke the method
				reply = services.get(rpcid).invoke(msg.getData());
			}else {
				
				reply = new byte[1];
				reply[0] = 0;
			}


			// - send back message containing RPC reply
			
			connection.send(new UnboundedMessage(reply));
			

			if (rpcid == RPCCommon.RPIDSTOP) {
				stop = true;
			}
		}

	}

	public void register(int rpcid, RPCImpl impl) {
		services.put(rpcid, impl);
	}

	public void stop() {
		connection.close();
		msgserver.stop();

	}
}
