import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Http Request format:
 * <request-line>
 * <general-headers>
 * <request-headers>
 * <entity-headers>
 * <empty-line>
 * [<message-body>]
 * [<message-trailers>]
 */
public class HttpRequest
	extends GenericHttpMessage
{
	private String requestMethod;
	private String requestUrl;
	private String queryString;
	
	private String filename;
	
	private Map<String, String> queryMap = new HashMap<>();
	
	protected HttpRequest(String requestLine)
	{
		startLine = requestLine;
		
		/**
		 * parsing the request line, e.g. "GET /index.html?name=hey HTTP/1.1"
		 */
		String[] pieces = startLine.split(" ");
		
		if (pieces.length == 3)
		{
			requestMethod = pieces[0].trim();
			requestUrl = pieces[1].trim();
			
			if (requestUrl.length() > 1)
				filename = requestUrl.substring(1);
			
			char[] urlChars = requestUrl.toCharArray();
			for (int i = 0; i < urlChars.length; i++)
			{
				if ( urlChars[i] == '?' )
				{
					queryString = filename.substring (i);
					filename = filename.substring (0, i - 1);
					
					for (String k : queryString.split("&"))
					{
						String[] keyvalue = k.split("=");
						
						if (keyvalue.length == 2)
							queryMap.put (keyvalue[0], keyvalue[1]);
						else
							queryMap.put (keyvalue[0], "");
					}
					
					break;
				}
			}
		}
	}
	
	public String getMethod()
	{
		return requestMethod;
	}
	
	public String getUrl()
	{
		return requestUrl;
	}
	
	public String getFilename()
	{
		return filename;
	}
	
	public String getQueryString()
	{
		return queryString;
	}
	
	public String getQuery (String k)
	{
		return queryMap.get(k);
	}
	
	public Map<String, String> getQueryMap()
	{
		return queryMap;
	}
	
	public static HttpRequest parseRequest (InputStream inputStream)
		throws IOException
	{
		HttpRequest httpRequest = null;
		
		HttpRequestReader in = new HttpRequestReader (inputStream);
		
		String line = in.readLine();
		
		if ( ! line.contains(":") )
			httpRequest = new HttpRequest(line);
		else
			httpRequest = new HttpRequest ("HTTP/1.1 GET /");
		
		/**
		 * Read HTTP Headers as TEXT
		 */
		while ( line != null && line.length() > 0 )
		{
			String[] header = line.split(":");
			
			if (header.length == 2)
				httpRequest.setHeader ( header[0].trim(), header[1].trim() );
			
			line = in.readLine();
		}
		
		/**
		 * .. but we read the content as bytes!
		 */
		if ( in.available() > 0 )
		{
			BufferedInputStream dis = new BufferedInputStream (in.getInputStream());
			byte[] b = new byte[128];
			int r;
			while ( (r = dis.read(b, 0, 128)) != -1)
			{
				httpRequest.byteOutputStream.write (b, 0, r);
			}
		}
		
		return httpRequest;
	}
}