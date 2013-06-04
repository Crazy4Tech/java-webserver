import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Formatter;
import java.util.logging.SimpleFormatter;

import java.util.Map;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class WebServer
{
	public static void main (String[] args)
	{
		try
		{
			WebServer ws = new WebServer();
			
			System.out.println ("WebServer listening on 127.0.0.1:80.");
			System.out.println ("Type Ctrl-C to shutdown\n");
			
			ws.listen();
		}
		catch (IOException e)
		{
			System.err.println ("Error while listening on port..");
			System.exit(1);
		}
	}
	
	private static final String PUBLIC_DIR = "C:\\Users\\David\\java-webserver";
	
	public static Logger Log = Logger.getLogger(WebServer.class.getName());
	
	static
	{
		try
		{
			Handler handler = new FileHandler ("webserver.log", true);
			handler.setFormatter(new SimpleFormatter());
			handler.setLevel (Level.CONFIG);
			
			Log.addHandler(handler);
			Log.setLevel(Level.CONFIG);
			Log.setUseParentHandlers(false);
			
			Log.info ("Log file loaded.");
		}
		catch (IOException e)
		{
			System.err.println ( e.getMessage());
		}
	}
	
	private class ShutdownHook
		extends Thread
	{
		public void run ()
		{
			shutdown = 1;
			
			try
			{
				Log.info ("Shutdown requested. Closing listen socket ...");
				
				listenSocket.close();
				serverThread.join();
				
				if (Log != null)
					for (Handler h : Log.getHandlers())
					{
						h.flush();
						h.close();
					}
			}
			catch (InterruptedException e)
			{
				System.err.println ( e.getMessage() );
			}
			catch (IOException e)
			{
				System.err.println (e.getMessage());
			}
		}
	}
	
	private class Worker
		implements Runnable
	{
		private Socket socket;
		
		public Worker (Socket s)
		{
			socket = s;
		}
		
		public void run ()
		{
			try (
				InputStream in = socket.getInputStream();
				OutputStream outStream = socket.getOutputStream();
				PrintWriter out = new PrintWriter (outStream, true)
			)
			{
				Log.info ("Reading from client ...");
				
				HttpRequest httpRequest = HttpRequest.parseRequest (in);
				
				Log.info ("Sending reply ...");
				
				HttpResponse httpResponse = new HttpResponse ("HTTP/1.1 200 OK");
				
				String filename = httpRequest.getFilename();
				
				boolean handlerOk = false;
				
				if ( filename != null )
				{
					File file = new File (filename);
					
					if ( file.exists() && file.isFile() && file.canRead() )
					{
						String ext = null;
						int pos = filename.lastIndexOf('.');
						
						if (pos > 0)
							ext = filename.substring (pos + 1);
						
						if (ext != null)
						{
							FiletypeHandler fh = null;
							
							if (ext.equalsIgnoreCase("php"))
							{
								fh = new PHPFiletypeHandler();
							}
							else
							{
								fh = new DefaultFiletypeHandler();
							}
							
							if (fh != null)
							{
								fh.handleFile(httpRequest, httpResponse);
								handlerOk = true;
							}
						}
					}
				}
				
				if ( ! handlerOk )
				{
					StringBuilder message = new StringBuilder();
					message.append ("404? 501? Something other? I dont know.");
					
					httpResponse.setBody (message.toString());
				}
				
				// write headers
				out.print ( httpResponse.getStatusLine() );
				out.print ("\r\n");
				for (Map.Entry<String, String> entry : httpResponse.getHeaders().entrySet())
				{
					out.print(entry.getKey());
					out.print (":");
					out.print (entry.getValue());
					out.print("\r\n");
				}
				
				out.print("\r\n");
				out.flush();
				
				// write body
				ByteArrayOutputStream bais = httpResponse.getByteArrayOutputStream();
				
				if (httpResponse.getBody() == null || httpResponse.getBody().length() == 0)
				{
					bais.writeTo (outStream);
				}
				else
				{
					out.write (httpResponse.getBody());
				}
				
				out.flush();
				
				Log.info ("Reply sent!\n");
				
				socket.close();
			}
			catch (IOException e)
			{
				Log.severe ("IO Error: " + e.getMessage());
			}
		}
	}
	
	private final Thread serverThread = Thread.currentThread();
	
	private ServerSocket listenSocket;
	private volatile int shutdown;
	
	public WebServer()
		throws IOException
	{
		listenSocket = new ServerSocket(80);
		
		Runtime.getRuntime().addShutdownHook (new ShutdownHook());
	}
	
	public void listen()
	{
		while ( shutdown == 0 )
		{
			try
			{
				Log.info ("Waiting for connection ...");
				
				Socket socket = listenSocket.accept();
				
				Log.info (String.format("Connection from: %s:%d -> %s:%d\n", socket.getInetAddress().getHostAddress(), 
					socket.getPort(), socket.getLocalAddress().getHostAddress(), socket.getLocalPort()));
				
				new Thread(new Worker(socket)).start();
			}
			catch (IOException e)
			{
				if (shutdown != 0)
				{
					Log.warning(e.getMessage());
				}
			}
		}
	}
}