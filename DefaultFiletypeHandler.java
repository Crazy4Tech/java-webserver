import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class DefaultFiletypeHandler
		implements FiletypeHandler
{
	public void handleFile(HttpRequest req, HttpResponse httpResponse)
	{
		try (BufferedInputStream fis = new BufferedInputStream (new FileInputStream (new File (req.getFilename()))))
		{
			ByteArrayOutputStream bais = httpResponse.getByteArrayOutputStream();
			
			byte[] readBuffer = new byte[1024];
			int k;
			while ( (k = fis.read(readBuffer, 0, readBuffer.length)) != -1)
			{
				bais.write (readBuffer, 0, k);
			}
		}
		catch  (FileNotFoundException e)
		{
			WebServer.Log.warning (e.getMessage());
		}
		catch (IOException e)
		{
			WebServer.Log.warning (e.getMessage());
		}
	}
}