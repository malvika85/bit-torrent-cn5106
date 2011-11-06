package cnt5106c.torrent.peer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class owns the functionality of inserting or retrieving chunks for file transfer and organizing it on the 
 * disk.
 * @author arpit@cise.ufl.edu
 *
 */
public class FileHandler
{
    private File myFile;
    private int myFileSize;    
    private int myPieceSize;
    private final int CHUNK_SIZE = 1 << 26; //file written in terms of 64 MB chunks
    
    public FileHandler(String filePath, int fileSize, int pieceSize) throws FileNotFoundException
    {
        this.myFile = new File(filePath);
        this.myFileSize = fileSize;
        this.myPieceSize = pieceSize;
    }
    
    /**
     * This function creates dummy file with the size given in the constructor. Filename is also supplied
     * in the constructor
     * @throws IOException 
     */
    public boolean createDummyFile() throws IOException
    {
        if(myFile.exists())
        {
            if(!myFile.delete())
            {
                //TODO : log error message
                return false;
            }
        }
        if(!myFile.createNewFile())
        {
            return false;
        }
        
        //write CHUNK_SIZE bytes in the file until we cover it's length
        FileOutputStream fos = new FileOutputStream(myFile);
        for(int i = CHUNK_SIZE; i < myFileSize; i += CHUNK_SIZE)
        {
            byte[] tempBuffer = new byte[i];
            fos.write(tempBuffer);
        }
        //now write remaining bytes
        int remainingBytes = myFileSize & (CHUNK_SIZE - 1);     //replacement of modulo
        byte[] tempBuffer = new byte[remainingBytes];
        fos.write(tempBuffer);
        
        //close the output stream as we are done writing, this will also flush the data
        fos.close();
        
        //return true as we have successfully created the dummy file
        return true;
    }
    
    /**
     * Reads the data for a particular piece index from the file and returns to the caller. 
     * This method assumes that data is already present.
     * @param pieceID ID of the piece for which data is required (piece ID starts from 0)
     * @return byte array which contains the asked data
     * @throws IOException If file doesn't exist for if data denoted by pieceID is unreachable
     */
    public byte[] getPieceFromFile(int pieceID) throws IOException
    {
        byte[] buffer = new byte[myPieceSize];
        RandomAccessFile raf = new RandomAccessFile(myFile, "r");
        //find the position which is max(0, just before piece starts)
        raf.seek(Math.max((pieceID - 1)*myPieceSize, 0));
        raf.read(buffer);
        return buffer;
    }

    public static boolean createDirectoryIfNotExists(String someDirectoryPath)
    {
        File directory = new File(someDirectoryPath);
        if(directory.exists())
        {
            return true;
        }
        return directory.mkdir();
    }

    public void writePieceToFile(int pieceID, byte[] pieceData) throws IOException
    {
        RandomAccessFile raf = new RandomAccessFile(this.myFile, "rw");
        raf.seek(Math.max((pieceID -1)*myPieceSize, 0));
        raf.write(pieceData);
    }    
}
