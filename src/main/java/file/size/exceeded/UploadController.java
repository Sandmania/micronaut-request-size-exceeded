package file.size.exceeded;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.reactivex.Single;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Controller
public class UploadController {

    private final UploadService uploadService;
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @Post(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN)
    public Single<String> uploadAttachments(Publisher<CompletedFileUpload> files) {
        return Single.create(emitter -> {
           files.subscribe(new Subscriber<CompletedFileUpload>() {
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
