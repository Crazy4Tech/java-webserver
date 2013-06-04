import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class PHPFiletypeHandler
		implements FiletypeHandler
{
	public void handleFile(HttpRequest httpRequest, HttpResponse httpResponse)
	{
		try
		{
			File file = new File (httpRequest.getFilename());
			
			List<String> cmd = new ArrayList<>(2);
			cmd.add ("php-cgi.exe");
			cmd.add (file.getAbsolutePath());
			
			ProcessBuilder pb = new ProcessBuilder(cmd);
			Map<String, String> env = pb.environment();
			
			env.clear();
			if (httpRequest.getQueryString() != null)
				env.put ("QUERY_STRING", httpRequest.getQueryString());
			env.put ("REQUEST_METHOD", httpRequest.getMethod());
			
			env.put ("SCRIPT_FILENAME", file.getAbsolutePath());
			env.put ("SCRIPT_NAME", httpRequest.getFilename());
			env.put ("REQUEST_URI", httpRequest.getUrl());
			
			env.put ("REDIRECT_STATUS", "200");
			
			pb.directory (file.getParentFile());
			
			Process p = pb.start();
			
			try (InputStream in = p.getInputStream())
			{
				// Parse the output as a http request (since headers are outputted first..)
				HttpRequest req = HttpRequest.parseRequest (in);
				
				for (Map.Entry<String, String> entry : req.getHeaders().entrySet())
				{
					httpResponse.setHeader (entry.getKey(), entry.getValue());
				}
				
				byte[] body = req.getByteArrayOutputStream().toByteArray();
				httpResponse.getByteArrayOutputStream().write (body, 0, req.getByteArrayOutputStream().size());
			}
			catch (IOException e)
			{
				WebServer.Log.warning (e.getMessage());
			}
		}
		catch (IOException e)
		{
			WebServer.Log.warning (e.getMessage());
		}
	}
}