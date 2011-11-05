package cnt5106c.torrent.transceiver;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cnt5106c.torrent.peer.TorrentFile;
import cnt5106c.torrent.config.PeerConfig;

public class Transceiver
{
    private Map<Integer, PeerConfig> peerInfoMap;
    private int myPeerID;
    private String myHostName;
    private int myListenerPort;
    private ConcurrentHashMap<Integer, Client> peerConnectionMap;
    private TorrentFile myTorrentFile;

    /**
     * This ctor starts server immediately in the background on the listening port 
     * and host address provided. It also starts the client connections for the all the peers
     * listed in peer Info map before it's own value.
     * @param myHostName Your own host name
     * @param myListenerPort The port on which server should run
     * @param peerMap A Map of <PeerId, PeerInfo> which has all necessary information to connect to peers
     * @throws UnknownHostException
     * @throws IOException
     */
    public Transceiver(Map<Integer, PeerConfig> peerMap, int myPeerID, TorrentFile myTorrentFile) 
    {
        PeerConfig myConfig = peerMap.get(myPeerID);
        this.myHostName = myConfig.getHostName();
        this.myListenerPort = myConfig.getListeningPort();
        this.peerInfoMap = peerMap;
        this.myPeerID = myPeerID;
        this.myTorrentFile = myTorrentFile;
        this.peerConnectionMap = new ConcurrentHashMap<Integer, Client>();
    }
    
    public void takeAction() throws SocketTimeoutException, IOException
    {
        (new Thread(new Server(myHostName, myListenerPort, this))).start();
        this.processPeerInfoMap();
    }
    
    /**
     * Go through Peer Info Map entrires one by one and then create client for peer IDs < myPeerID.
     * Once a client is created, send handshake immediately.
     * @throws IOException If there is an error while connecting with other clients.
     * @throws SocketTimeoutException If server couldn't be contacted with stipulated time.
     */
    private void processPeerInfoMap() throws SocketTimeoutException, IOException
    {
        for(Integer aPeerID : this.peerInfoMap.keySet())
        {
            if(aPeerID < myPeerID)
            {
                //peer has already been started, try to make a connection
                Client newClient = new Client(this.peerInfoMap.get(aPeerID).getHostName(),
                        this.peerInfoMap.get(aPeerID).getListeningPort());
                //now make an EventHandler (algorithm) for this client
                EventManager anEventManager = new EventManager(newClient, this);
                //start event manager before client starts any activity
                (new Thread(anEventManager)).start();
                this.peerConnectionMap.put(aPeerID, newClient);
            }
        }
    }
    
    public void sendMessageToGroup(List<Integer> peerIDList, byte[] data) throws IOException
    {
        for (Integer aPeerID : peerIDList)
        {
            this.peerConnectionMap.get(aPeerID).send(data);
        }
    }

    public TorrentFile getTorrentFile()
    {
        return this.myTorrentFile;
    }

    public int getMyPeerID()
    {
        return this.myPeerID;
    }
}
