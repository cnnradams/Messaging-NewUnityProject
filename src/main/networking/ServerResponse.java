package main.networking;

import java.util.Arrays;

import org.zeromq.ZMQ;

public class ServerResponse {

    public final int resultCode;
    public final String[] response;
    
    public ServerResponse(ZMQ.Socket requester) {
        String[] reply = requester.recvStr().split("\\n");
        
        response = Arrays.copyOfRange(reply, 1, reply.length);
        resultCode = Integer.parseInt(reply[0]);
    }
    
}
