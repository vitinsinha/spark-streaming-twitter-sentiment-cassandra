import java.util
import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment._
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.util.logging.RedwoodConfiguration
import scala.collection.JavaConversions._

@SerialVersionUID(100L)
class SentimentAnalyzer extends Serializable {

  def analyze( text: String ) : String = {
    val props = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, parse, sentiment")

    // shut off the annoying initialization messages
    RedwoodConfiguration.empty().capture(System.err).apply();

    val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

    // enable stderr again
    RedwoodConfiguration.current().clear().apply();

    val annotation: Annotation = pipeline.process(text)
    val sentences: util.List[CoreMap] = annotation.get(classOf[SentencesAnnotation])
    var sentiment : String = "";
    for(sentence: CoreMap <- sentences) {
      sentiment = sentence.get(classOf[SentimentCoreAnnotations.SentimentClass]);
    }
    return sentiment;
  }

}
