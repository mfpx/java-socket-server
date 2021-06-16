package uk.co.stumbra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerMain extends Thread
{
    private ServerSocket serverSocket;
    private final int port;
    private boolean running = false;

    public ServerMain( int port ) {
        this.port = port;
    }

    public void startServer()
    {
        try
        {
            serverSocket = new ServerSocket( port );
            this.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void stopServer()
    {
        running = false;
        this.interrupt();
    }

    @Override
    public void run()
    {
        running = true;
        while( running )
        {
            try
            {
                System.out.println("Listening for a connection" );

                // Call accept() to receive the next connection
                Socket socket = serverSocket.accept();

                // Pass the socket to the RequestHandler thread for processing
                RequestHandler requestHandler = new RequestHandler( socket );
                requestHandler.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main( String[] args ) throws UnknownHostException {
        if( args.length != 2 )
        {
            System.out.println( "Usage: ServerMain <host> <port>" );
            System.exit( 0 );
        }
        int port = Integer.parseInt( args[ 1 ] );
        InetAddress host = InetAddress.getByName(args [ 0 ]);
        System.out.println( "Start server on: " + host + ":" + port );

        ServerMain server = new ServerMain( port );
        server.startServer();

        // Automatically shutdown in 1 minute
        try
        {
            Thread.sleep( 60000 );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

        server.stopServer();
    }
}

class RequestHandler extends Thread
{
    private final Socket socket;
    RequestHandler( Socket socket )
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println( "Received a connection" );

            // Get input and output streams
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            PrintWriter out = new PrintWriter( socket.getOutputStream() );

            // Write out our header to the client
            out.println( "Echo Server 1.0" );
            out.flush();

            // Echo lines back to the client until the client closes the connection or we receive an empty line
            String line = in.readLine();
            while( line != null && line.length() > 0 )
            {
                out.println( "Echo: " + line );
                out.flush();
                line = in.readLine();
            }

            // Close our connection
            in.close();
            out.close();
            socket.close();

            System.out.println( "Connection closed" );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}
