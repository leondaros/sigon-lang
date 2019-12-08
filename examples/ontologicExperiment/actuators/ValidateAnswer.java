package ontologicExperiment.actuators;

import java.net.URISyntaxException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.SolveInfo;
import br.ufsc.ine.agent.context.communication.Actuator;
import br.ufsc.ine.agent.context.ontologic.OntologicContextService;
import br.ufsc.ine.utils.PrologEnvironment;
import io.socket.client.IO;
import io.socket.client.Socket;

public class ValidateAnswer extends Actuator {
	
	@Override
	public void act(List<String> args) {
		System.out.println("validaaaaaaaaaaaaaa");
		testKnowledge(args);
	}
	
	@SuppressWarnings("static-access")
	public void testKnowledge(List<String> args){
		PrologEnvironment prologEnvironment = OntologicContextService.getInstance().getPrologEnvironment();
		SolveInfo info = null;
		String predicate = args.get(0);
		String subject = args.get(1);
		String object = args.get(2);
		Boolean answer = Boolean.parseBoolean(args.get(3));
		try {
			info = prologEnvironment.getEngine().solve("knowledge("+predicate+","+subject+","+object+").");
			if(info.isSuccess() != answer){
				info = prologEnvironment.getEngine().solve("knowledge("+predicate+","+subject+",X).");
				System.out.println("Resposta certa: "+info.getBindingVars().get(0).getLink().getTerm());
			}else {
				System.out.println("Resposta Correta");
			}
			connect(info.isSuccess() == answer);
		} catch (MalformedGoalException | URISyntaxException | NoSolutionException e) {
			e.printStackTrace();
		}
	}
	
	public static void connect(Boolean answer) throws URISyntaxException{
		Socket socket = IO.socket("http://127.0.0.1:3001");		
		JSONObject obj = new JSONObject();
		try {
			obj.put("result", answer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		socket.emit("questionResult", obj);
		socket.connect();
	}
}
