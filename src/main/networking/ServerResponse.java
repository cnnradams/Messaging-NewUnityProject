package main.networking;

import java.util.Arrays;

import org.zeromq.ZMQ;

/**
 * Holds information about the server's response to a request
 */
public class ServerResponse {

    /**
     * The result code of the response
     */
    public final int resultCode;
    
    /**
     * The lines of the response
     */
    public final String[] response;
    
    /**
     * Gets the response from the socket and parses it
     * 
     * @param requester The socket to get the reply from
     */
    public ServerResponse(ZMQ.Socket requester) {
        String[] reply = requester.recvStr().split("\\n");
        
        response = Arrays.copyOfRange(reply, 1, reply.length);
        resultCode = Integer.parseInt(reply[0]);
    }
    
}
