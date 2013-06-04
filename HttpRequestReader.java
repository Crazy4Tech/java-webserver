import java.io.InputStream;
import java.io.IOException;

public class HttpRequestReader
	implements AutoCloseable
{
	private InputStream inputStream;
	
	public HttpRequestReader (InputStream in)
	{
		inputStream = in;
	}
	
	public InputStream getInputStream()
	{
		return inputStream;
	}
	
	public String readLine()
		throws IOException
	{
		StringBuilder sb = new StringBuilder();
		int bytesRead = 0;
		for (int read = read(); read != -1; read = read())
		{
			bytesRead += read;
			char c = (char)read;
			
			if (c == '\n')
				break;
			if (c == '\r')
			{
				inputStream.mark(bytesRead);
				if (read() == '\n')
					break;
				inputStream.reset();
				break;
			}
			
			sb.append(c);
		}
		
		if (sb.length() > 0)
			return sb.toString();
		return null;
	}
	
	public int available()
		throws IOException
	{
		return inputStream.available();
	}
	
	public int read()
		throws IOException
	{
		return inputStream.read();
	}
	
	public int read (byte[] buf, int offset, int length)
		throws IOException
	{
		return inputStream.read (buf, offset, length);
	}
	
	public void close()
		throws Exception
	{
		inputStream.close();
	}
}