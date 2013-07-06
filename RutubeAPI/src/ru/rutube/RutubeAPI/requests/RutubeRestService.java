package ru.rutube.RutubeAPI.requests;

import com.foxykeep.datadroid.service.RequestService;
import ru.rutube.RutubeAPI.operations.*;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 03.05.13
 * Time: 21:20
 * To change this template use File | Settings | File Templates.
 */
public class RutubeRestService extends RequestService {

    @Override
    public Operation getOperationForType(int requestType) {
        switch (requestType) {
            case RequestFactory.REQUEST_TRACKINFO:
                return new TrackinfoOperation();
            case RequestFactory.REQUEST_EDITORS:
                return new GetFeedOperation();
            case RequestFactory.REQUEST_TOKEN:
                return new GetTokenOperation();
            case RequestFactory.REQUEST_UPLOAD:
                return new UploadOperation();
            case RequestFactory.REQUEST_UPLOAD_SESSION:
                return new GetUploadSessionOperation();
            case RequestFactory.REQUEST_UPDATE_VIDEO:
                return new UpdateVideoOperation();
            default:
                return null;
        }
    }
}
