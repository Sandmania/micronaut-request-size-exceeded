package file.size.exceeded;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.exceptions.ContentLengthExceededException;
import io.micronaut.http.hateoas.JsonError;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@Requires(property = "global.errorHandling", value = "true")
public class ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

    @Error(global = true, exception = ContentLengthExceededException.class)
    public Single<MutableHttpResponse<JsonError>> error(Throwable t) {
        logger.error("Global error", t);
        return Single.just(HttpResponse.serverError().body(new JsonError(t.getMessage())));
    }
}
