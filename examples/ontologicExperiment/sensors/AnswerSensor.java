package ontologicExperiment.sensors;

import br.ufsc.ine.agent.context.communication.Sensor;
import rx.subjects.PublishSubject;

public class AnswerSensor extends Sensor {

    public static final PublishSubject<String> answerObservable = PublishSubject.create();
	@Override
	public void run() {
		answerObservable.subscribe(super.publisher);		
	}
	

}
