import java.util.Map;
import java.util.HashMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Generic message, common for requests and responses:
 * <start-line>
 * <message-headers>
 * <empty-line>
 * [<message-body>]
 * [<message-trailers>]
 */

public abstract class GenericHttpMessage
{
	protected String startLine;
	protected Map<String, String> headers = new HashMap<>();
	protected String messageBody;
	
	protected ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
	
	protected String toProperCase (String str)
	{
		StringBuilder sb = new StringBuilder();
		for (String word : str.toLowerCase().split("-"))
		{
			sb.append ( word.substring (0, 1).toUpperCase() ).append (word.substring (1)).append("-");
		}
		
		str = sb.toString();
		return str.substring (0, str.length() - 1);
	}
	
	public ByteArrayOutputStream getByteArrayOutputStream()
	{
		return byteOutputStream;
	}
	
	public String getBody ()
	{
		return messageBody;
	}
	
	public String getHeader (String k)
	{
		return headers.get(toProperCase(k));
	}
	
	public Map<String, String> getHeaders()
	{
		return headers;
	}
	
	public void setHeader (String k, String v)
	{
		headers.put (toProperCase(k), v);
	}
}