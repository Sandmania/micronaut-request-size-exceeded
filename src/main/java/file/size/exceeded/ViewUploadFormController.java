package file.size.exceeded;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.ContentLengthExceededException;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.views.View;
import io.reactivex.Single;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Controller
public class ViewUploadFormController {

    private final UploadService uploadService;
    private static final Logger logger = LoggerFactory.getLogger(ViewUploadFormController.class);

    public ViewUploadFormController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @Get("/")
    @View("upload")
    public Single<MutableHttpResponse<Object>> uploadView() {
        return Single.just(HttpResponse.ok());
    }
}
