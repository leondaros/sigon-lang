package ontologicExperiment;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.json.JSONException;
import org.json.JSONObject;

import agent.AgentLexer;
import agent.AgentParser;
import br.ufsc.ine.agent.Agent;
import br.ufsc.ine.agent.context.ContextService;
import br.ufsc.ine.agent.context.beliefs.BeliefsContextService;
import br.ufsc.ine.agent.context.communication.CommunicationContextService;
import br.ufsc.ine.agent.context.desires.DesiresContextService;
import br.ufsc.ine.agent.context.intentions.IntentionsContextService;
import br.ufsc.ine.agent.context.ontologic.OntologicContextService;
import br.ufsc.ine.agent.context.plans.PlansContextService;
import br.ufsc.ine.parser.AgentWalker;
import br.ufsc.ine.parser.VerboseListener;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import ontologicExperiment.actuators.BuildQuestion;
import ontologicExperiment.sensors.AnswerSensor;
public class Main {
	
	public static void main(String[] args) throws InterruptedException, URISyntaxException {
		// TODO Auto-generated method stub
		startAgent();
		int total = 100;
//		Thread.sleep(500);
//		percept("1");
//		Thread.sleep(500);
//		percept("2");
//		Thread.sleep(500);
//		percept("capital,malta,birkirkara");
//		Thread.sleep(500);
//		percept("capital,malta,valletta,true,1");

		Socket socket = IO.socket("http://127.0.0.1:3001");
		connect(socket);
		
	}
	
	public static void connect(Socket socket) throws URISyntaxException{
		socket.on("answer", new Emitter.Listener() {
			public void call(Object... args) {
				JSONObject obj = (JSONObject)args[0];
				System.out.println(obj);
				try {
					percept(obj.getString("userAnswer"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		socket.connect();
	}

	private static void startAgent() {
		try {

			File agentFile = new File("/home/leon/Documentos/Git/sigon-examples/src/ontologicExperiment/agent.on");
			CharStream stream = CharStreams.fromFileName(agentFile.getAbsolutePath());
			AgentLexer lexer = new AgentLexer(stream);
			CommonTokenStream tokens = new CommonTokenStream(lexer);

			AgentParser parser = new AgentParser(tokens);
			parser.removeErrorListeners();
			parser.addErrorListener(new VerboseListener());

			ParseTree tree = parser.agent();
			ParseTreeWalker walker = new ParseTreeWalker();

			AgentWalker agentWalker = new AgentWalker();
			walker.walk(agentWalker, tree);
			
			OntologicContextService ontologicContextService = OntologicContextService.getInstance();
			ContextService[] cc = new ContextService[] {ontologicContextService};
			
			Agent agent = new Agent();
			agent.run(agentWalker,cc);

		} catch (IOException e) {
			System.out.println("I/O exception.");
		}
	}
	
	private static void percept(String index) {

		AnswerSensor.answerObservable.onNext(""+ index +".");		

		System.out.println("CC " + CommunicationContextService.getInstance().getTheory());
		System.out.println("BC " + BeliefsContextService.getInstance().getTheory().toString());
		System.out.println("DC " + DesiresContextService.getInstance().getTheory());
		System.out.println("PC " + PlansContextService.getInstance().getTheory().toString());
		System.out.println("" + IntentionsContextService.getInstance().getTheory());
		System.out.println("" + OntologicContextService.getInstance().getTheory());

	}
}
