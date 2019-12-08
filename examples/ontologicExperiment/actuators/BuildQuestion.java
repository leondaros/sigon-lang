package ontologicExperiment.actuators;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import br.ufsc.ine.agent.context.communication.Actuator;
import io.socket.client.IO;
import io.socket.client.Socket;

public class BuildQuestion extends Actuator{
			
	@Override
	public void act(List<String> args) {
		try {
			connect(args);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public static void connect(List<String> args) throws URISyntaxException{
		
		String predicate = args.get(0);
		String subject = args.get(1);
		String object = args.get(2);
		Socket socket = IO.socket("http://127.0.0.1:3001");
		
		// Sending an object
		JSONObject obj = new JSONObject();
		try {
			obj.put("question", "knowledge("+predicate+","+subject+","+object+").");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		socket.emit("sendQuestion", obj);
		socket.connect();
	}	
}
