package cnt5106c.torrent.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cnt5106c.torrent.utils.Utilities;

public class HandshakeMessage extends Message
{
	protected static final long serialVersionUID = 2L;
	public final String HANDSHAKE_MSG_HEADER;
    private int peerID = -1;
	
	public HandshakeMessage(int peerID) throws IOException, InterruptedException
	{
	    this.HANDSHAKE_MSG_HEADER = "CEN5501C2008SPRING";
	    ByteArrayOutputStream baos = Utilities.getStreamHandle();
	    baos.write(HANDSHAKE_MSG_HEADER.getBytes());
	    baos.write(new byte[10]);  //10 bytes zero bits
	    this.peerID = peerID;
	    baos.write(Utilities.getBytes(peerID));
	    super.message = baos.toByteArray();
	    Utilities.returnStreamHandle();
	}

	/**
	 * This will reconstruct the Handshake message from given bytes and set the peerID embedded
	 * in the message
	 */
    public HandshakeMessage(byte[] handshakeMsg)
    {
        this.message = handshakeMsg;
        //read last 4 bytes to read peer ID
        this.peerID  = Utilities.getIntegerFromByteArray(handshakeMsg, 28);
        this.HANDSHAKE_MSG_HEADER = new String(handshakeMsg, 0, 18);
    }

    public int getPeerID()
    {
        return peerID;
    }
}
