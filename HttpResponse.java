import java.util.Map;
import java.util.Map.Entry;

/**
 * <status-line>
 * <general-headers>
 * <response-headers>
 * <entity-headers>
 * <empty-line>
 * [<message-body>]
 * [<message-trailers>]
 */
public class HttpResponse
	extends GenericHttpMessage
{
	private String contentType;
	
	public HttpResponse(String statusLine)
	{
		startLine = statusLine;
		contentType = "text/plain";
	}
	
	public String getStatusLine()
	{
		return startLine;
	}
	
	public void setContentType (String k)
	{
		contentType = k;
		setHeader ("Content-Type", contentType);
	}
	
	public void setBody (String k)
	{
		messageBody = k;
		
		setHeader ("Content-Length", Integer.toString(k.getBytes().length));
	}
	
	public String getResponse()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(startLine).append("\r\n");
		
		for (Map.Entry<String, String> entry : headers.entrySet() )
		{
			sb.append (entry.getKey()).append(":").append(entry.getValue()).append("\r\n");
		}
		
		sb.append("\r\n");
		sb.append(messageBody);
		
		return sb.toString();
	}
}