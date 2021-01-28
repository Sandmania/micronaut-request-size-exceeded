package file.size.exceeded;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.ContentLengthExceededException;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.reactivex.Single;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Controller
public class UploadControllerLocalErrorHandler {

    private final UploadService uploadService;
    private static final Logger logger = LoggerFactory.getLogger(UploadControllerLocalErrorHandler.class);

    public UploadControllerLocalErrorHandler(UploadService uploadService) {
        this.uploadService = uploadService;
    }


    @Error(ContentLengthExceededException.class)
    public Single<MutableHttpResponse<JsonError>> contentLengthExceededErrorHandler(Throwable t) {
        return Single.just(
                HttpResponse.status(HttpStatus.REQUEST_ENTITY_TOO_LARGE).body(new JsonError(t.getMessage())));
    }

    @Post(value = "/uploadLocalErrorHandling", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN)
    public Single<String> uploadAttachments(Publisher<CompletedFileUpload> files2) {
        return Single.create(emitter -> {
            files2.subscribe(new Subscriber<CompletedFileUpload>() {
               private Subscription s;

               @Override
               public void onSubscribe(Subscription s) {
                   this.s = s;
                   s.request(10);
               }

               @Override
               public void onNext(CompletedFileUpload completedFileUpload) {
                   try {
                       uploadService.upload(completedFileUpload.getBytes());
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }

               @Override
               public void onError(Throwable t) {
                   logger.error("");
                   emitter.onError(t);
               }

               @Override
               public void onComplete() {
                   emitter.onSuccess("Uploaded");
               }
           });
        });
    }
}
